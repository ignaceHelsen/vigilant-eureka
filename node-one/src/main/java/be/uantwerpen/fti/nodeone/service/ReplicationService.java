package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.config.component.HashCalculator;
import be.uantwerpen.fti.nodeone.config.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.domain.*;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@EnableScheduling
@AllArgsConstructor
public class ReplicationService {
    private final ReplicationComponent replicationComponent; // TODO persistence?
    private final NamingServerConfig namingServerConfig;
    private final RestTemplate restTemplate;
    private final FileService fileService;
    private final HashCalculator hashCalculator;
    private final NodeStructure nodeStructure;
    private final RestService restService;
    private final NetworkConfig networkConfig;
    private final NetworkService networkService;
    private final WebClient webClient;

    public void initializeReplication() {
        replicationComponent.initialize();
    }

    @Scheduled(initialDelay = 30 * 1000, fixedRate = 10 * 1000) // check for new files that have been added manually
    public void startReplication() {
        replicationComponent.lookForNewFiles();

        // check only for local files
        replicationComponent.getLocalFiles().forEach(file -> {
            // replicate every file which has not yet been replicated
            if (!file.isReplicated()) {
                try {
                    String destination = getDestination(file.getPath());
                    if (destination == null) {
                        return;
                    }
                    try {
                        replicate(file.getPath(), destination, file.getLogFile().getPath());
                        file.setReplicated(true);
                        replicationComponent.getReplicatedLocalFiles().add(file);
                    } catch (IOException e) {
                        log.warn("Could not replicate file {}", file.getPath());
                        e.printStackTrace();
                    } catch (RestClientException e) {
                        log.warn("Could not connect to server at {}", destination);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    log.error("Unable to connect to naming server");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Will request the destination where a file should be replicated.
     *
     * @param path: The path of the file
     * @return ip address of the location
     */
    private String getDestination(String path) {
        String filename = Paths.get(path).getFileName().toString();
        // Ask naming server where we should replicate the file to
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/replicationDestination/%d/%d",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), hashCalculator.calculateHash(filename), hashCalculator.calculateHash(networkConfig.getHostName())), String.class);

            return response.getBody();
        } catch (HttpClientErrorException.NotFound notFound) {
            log.warn("Naming server did not return node-response for replication of file: {}. This may indicate that only one node is running.", path);
            log.warn("Body: {}", notFound.getResponseBodyAsString());
            return null;
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.warn("Unable to access naming server.");
            return null;
        }
    }

    /**
     * Will replicate a file to a destination node.
     *
     * @param path:        The path to the file.
     * @param destination: The destination node to replicate it to.
     * @throws IOException: When error.
     */
    private void replicate(String path, String destination, String logPath) throws IOException {
        log.info("Replicating file {}", path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        if (path == null) log.warn("Path of file has returned null");
        body.add("file", getFile(path));
        body.add("logFile", getFile(logPath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String serverUrl = String.format("http://%s:%s/api/replication/replicate/", destination, namingServerConfig.getPort()); // the namingserverconfig getPort is the same as our controller's port

        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(serverUrl, requestEntity, Boolean.class);
            if (Boolean.TRUE.equals(response.getBody())) log.info("Succesfully replicated file {}", path);
            else log.warn("Failed replicating file {}", path);
        } catch (ResourceAccessException e) {
            log.error("Could not connect to host ({})", destination);
        }
    }

    private Resource getFile(String path) {
        Path file = Paths.get(path);
        return new FileSystemResource(file.toFile());
    }

    /**
     * Takes care of the storage of a file.
     * If it's a file destined for local storage, besides storing, we also replicate it immediately.
     * If it's a replication file, we just store it in /replication.
     *
     * @param file:   The path to the file.
     * @param action: Local or Replica file.
     * @return If storing didn't result in an error.
     */
    public boolean storeFile(MultipartFile file, MultipartFile logFile, Action action) throws IOException {
        String logPath = replicationComponent.createLogPath(file.getOriginalFilename());
        String filePath;
        if (action == Action.LOCAL) {
            filePath = replicationComponent.createFilePath(file.getOriginalFilename());
            log.info("Saving to {}", filePath);
            // Also replicate it
            FileStructure fileStruct = new FileStructure(filePath, file.getOriginalFilename(), false, new LogStructure(logPath));
            replicationComponent.saveLog(fileStruct.getLogFile(), file.getOriginalFilename());
            fileStruct.getLogFile().registerOwner(nodeStructure.getCurrentHash());
            replicationComponent.addLocalFile(fileStruct);

            // Since the new file is stored locally, we can already replicate it
            try {
                String destination = getDestination(filePath);
                if (destination != null) {
                    try {
                        replicate(filePath, destination, logPath);
                        fileStruct.setReplicated(true);
                        replicationComponent.addReplicatedLocalFile(fileStruct);
                    } catch (IOException e) {
                        log.error("Error occurred while trying to replicate file {}", filePath);
                        e.printStackTrace();
                        return false;
                    } catch (RestClientException e) {
                        log.warn("Could not connect to server at {}", destination);
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (Exception e) {
                log.error("Unable to connect to naming server");
                e.printStackTrace();
                return false;
            }
        } else {
            filePath = replicationComponent.createFilePath(file.getOriginalFilename());
            log.info("Replicating to {}", filePath);

            FileStructure fileStructure = new FileStructure(filePath, file.getOriginalFilename(), true,
                    replicationComponent.loadLog(file.getOriginalFilename()).orElse(new LogStructure(replicationComponent.createLogPath(file.getOriginalFilename()))));
            replicationComponent.addReplicatedFile(fileStructure);
            fileStructure.getLogFile().registerOwner(nodeStructure.getCurrentHash());
            replicationComponent.saveLog(fileStructure.getLogFile(), file.getOriginalFilename());
        }

        try {
            fileService.saveFile(file, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while saving file");
        }

        return true;
    }

    /**
     * @param files:    The files to store locally
     * @param logFiles: The logs that belong to this file
     * @param action:   To replicate or not to replicate
     * @return True if all files have been successfully stored or false if one ore more have failed.
     */

    public boolean storeFiles(List<MultipartFile> files, List<MultipartFile> logFiles, Action action) {
        if (files.size() != logFiles.size()) {
            throw new IllegalArgumentException("Files and logFiles have a different size");
        }
        for (int i = 0; i < files.size(); i++) {
            try {
                boolean success = storeFile(files.get(i), logFiles.get(i), action);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Will search for replica files that should belong to another node.
     *
     * @param nodeAddress: The other node requesting its files.
     */
    public boolean transferAndDeleteFiles(String nodeAddress) {
        log.info("Looking for files to transfer to node({})", nodeAddress);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        List<FileStructure> filesToDelete = new ArrayList<>();

        replicationComponent.getReplicatedFiles().forEach(file -> {
            String destination = getDestination(file.getPath());
            if (destination != null && destination.equalsIgnoreCase(nodeAddress)) {
                // add file to list
                body.add("files", getFile(file.getPath()));
                body.add("logFiles", getFile(file.getLogFile().getPath()));
                filesToDelete.add(file);
            }
        });

        // transfer files to node
        if (!body.isEmpty()) {
            try {
                // async method:
                uploadMultipleFilesToNode(nodeAddress, body);
                fileService.deletefiles(filesToDelete);
            } catch (IOException | InterruptedException | SecurityException e) {
                e.printStackTrace();
                log.error("Unable to transfer file to node ({})", nodeAddress);
                return false;
            }
        }

        return true;
    }

    public void uploadMultipleFilesToNode(String nodeAddress, MultiValueMap<String, Object> body) throws IOException, InterruptedException {
        String serverUrl = String.format("http://%s:%s/api/replication/transfer", nodeAddress, namingServerConfig.getPort()); // the namingserverconfig getPort is the same as our controller's port

        boolean success = Boolean.TRUE.equals(webClient.post()
                .uri(serverUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());

        if (success)
            log.info("Succesfully transfered files {}", new ArrayList<>(body.toSingleValueMap().keySet()));
        else log.warn("Failed replicating files {}", new ArrayList<>(body.toSingleValueMap().keySet()));

    }

    /**
     * Will look for files belonging to this node which are stored somewhere else.
     * The node will send a request to its own neighbours (next and previous) as well their respective next and previous nodes because those might still store replicas belonging to this node
     */
    @Async
    public void lookForFilesAtNeighbouringNodes() {
        NextAndPreviousNode nodes = restService.getNextAndPrevious(networkService.getCurrentHash());

        if (nodes != null && nodes.getIdPrevious() != networkService.getCurrentHash()) {
            // ask previous node for files that should belong to us
            try {
                CompletableFuture.supplyAsync(() -> restTemplate.getForObject(String.format("http://%s:%s/api/replication/move/%s",
                        nodes.getIpPrevious(), namingServerConfig.getPort(), networkConfig.getHostName()), String.class)).get();

            } catch (RestClientException | InterruptedException | ExecutionException rce) {
                rce.printStackTrace();
                log.warn("Connection refused to node {}", nodes.getIpPrevious());
            }

            // now do the same for the next-next neighbour because they might still store replicas that belong to us
            NextAndPreviousNode neighboursOfPreviousNode = restService.getNextAndPrevious(nodes.getIdPrevious());
            String ipAddressPreviousNeighbour = neighboursOfPreviousNode.getIpPrevious();

            if (ipAddressPreviousNeighbour == null || neighboursOfPreviousNode.getIdPrevious() == networkService.getCurrentHash() || neighboursOfPreviousNode.getIdPrevious() == nodes.getIdPrevious()) // only send when the previous node of my previous node is not myself. and don't go to the same node again...
                log.info("Transfer: no previous-previous node found, not transferring files.");
            else {
                try {
                    CompletableFuture.supplyAsync(() -> restTemplate.getForObject(String.format("http://%s:%s/api/replication/move/%s",
                            ipAddressPreviousNeighbour, namingServerConfig.getPort(), networkConfig.getHostName()), String.class)).get();

                } catch (RestClientException | InterruptedException | ExecutionException rce) {
                    rce.printStackTrace();
                    log.warn("Connection refused to node {}", ipAddressPreviousNeighbour);
                }
            }
        } else log.info("No other nodes to transfer files from.");
    }

    public void shutdown() {
        log.info("Transferring replicated files before shutdown");
        NextAndPreviousNode nodes = restService.getNextAndPrevious(networkService.getCurrentHash());
        NextAndPreviousNode neighboursOfPreviousNode = restService.getNextAndPrevious(nodes.getIdNext());

        // Send all replicated files
        replicationComponent.getReplicatedFiles().forEach(file -> {
            try {
                if (nodes.getIdPrevious() != file.getLogFile().getOwner(0).getHashValue()) {
                    replicate(file.getPath(), nodes.getIpPrevious(), file.getLogFile().getPath());
                } else if (neighboursOfPreviousNode.getIdPrevious() != nodeStructure.getCurrentHash()) {
                    replicate(file.getPath(), neighboursOfPreviousNode.getIpPrevious(), file.getLogFile().getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Send owner of own locals files warning
        log.info("Warning file owners that the local file is deleted");
        if (replicationComponent.getReplicatedLocalFiles() != null) {
            replicationComponent.getReplicatedLocalFiles().forEach(file -> {
                String serverUrl = String.format("http://%s:%s/api/replication/warnDeletedFiles/%s", getDestination(file.getPath()), namingServerConfig.getPort(), file.getFileName()); // the namingserverconfig getPort is the same as our controller's port
                restTemplate.put(serverUrl, Boolean.class);
            });
        }
        log.info("Deleting stored files");
        fileService.deletefiles(new ArrayList<>(replicationComponent.getLocalFiles()));
        fileService.deletefiles(new ArrayList<>(replicationComponent.getReplicatedFiles()));
    }
}
