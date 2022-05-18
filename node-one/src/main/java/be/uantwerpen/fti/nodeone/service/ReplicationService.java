package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import be.uantwerpen.fti.nodeone.domain.LogStructure;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import com.google.gson.Gson;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableScheduling
@AllArgsConstructor
public class ReplicationService {
    private final ReplicationComponent replicationComponent; // TODO persistence?
    private final NamingServerConfig namingServerConfig;
    private final RestTemplate restTemplate;
    private final HashCalculator hashCalculator;
    private final ReplicationConfig replicationConfig;
    private final NodeStructure nodeStructure;
    private final RestService restService;
    private final Gson gson;
    private final NetworkConfig networkConfig;
    private final RestService restService;
    private final NetworkService networkService;

    public void initializeReplication() {
        replicationComponent.initialize();
    }

    @Scheduled(initialDelay = 10 * 1000, fixedRate = 10 * 1000) // check for new files that have been added manually
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
     * Check if all needed directories for replication are present.
     * Create when not present.
     * <p>
     * Directories to be checked:
     * <ul>
     *     <li>/storage</li>
     *     <ul>
     *         <li>/local</li>
     *         <li>/replica</li>
     *         <li>/log</li>
     *     </ul>
     * </ul>
     */
    public void precheck() {
        File localDir = new File("src/resources/storage/local");
        File replicaDir = new File("src/resources/storage/replica");
        File logDir = new File("src/resources/storage/log");

        if (!localDir.exists()) {
            log.info("Created local directory.");
            try {
                boolean created = localDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", localDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/local directory. Security Exception");
            }
        }

        if (!replicaDir.exists()) {
            log.info("Created replica directory.");
            try {
                boolean created = replicaDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", replicaDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/replica directory. Security Exception");
            }
        }

        if (!logDir.exists()) {
            log.info("Created log directory.");
            try {
                boolean created = logDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", logDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/log directory. Security Exception");
            }
        }
    }

    private String getDestination(String path) throws RestClientException {
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

        }
    }

    /**
     * Replication of a file.
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
        body.add("log", getFile(logPath));

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
        byte[] bytes;
        bytes = file.getBytes();
        String filePath;
        String logPath = replicationComponent.createLogPath(logFile.getOriginalFilename());
        if (action == Action.LOCAL) {
            filePath = replicationComponent.createFilePath(file.getOriginalFilename());
            log.info("Saving to {}", filePath);
            // Also replicate it
            FileStructure fileStruct = new FileStructure(filePath, file.getOriginalFilename(), false, new LogStructure(logPath));
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
                    log.error("Error occured while trying to replicate file {}", filePath);
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
            File outputFile = new File(logPath);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(logFile.getBytes());
            }
            FileStructure fileStructure = new FileStructure(filePath, file.getOriginalFilename(), true,
                    replicationComponent.loadLog(file.getOriginalFilename()).orElse(new LogStructure(replicationComponent.createLogPath(file.getOriginalFilename()))));
            replicationComponent.addReplicatedFile(fileStructure);
            replicationComponent.saveLog(fileStructure.getLogFile(), file.getOriginalFilename());

            log.info("Replicating to {}", filePath);
        }

        File outputFile = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(bytes);
        }

        return true;
    }

    public boolean storeFiles(List<MultipartFile> files, Action action) throws IOException {
        for (MultipartFile file : files) {
            boolean success = storeFile(file, action);
        }

        return true;
    }

    /**
     * Will search for replica files that should belong to another node.
     *
     * @param nodeAddress: The other node requesting its files.
     */
    public void transferAndDeleteFiles(String nodeAddress) {
        MultiValueMap<String, Object> files = new LinkedMultiValueMap<>();

        File dir = new File(replicationConfig.getReplica());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().equalsIgnoreCase(".gitkeep"))).collect(Collectors.toList())) {
                // search for replicas
                // go to naming server to ask which node should have this file, if the response is the same as the node, it should be added to the list
                String destination = getDestination(child.getPath());
                if (destination != null && destination.equalsIgnoreCase(nodeAddress)) {
                    // add file to list
                    files.add(child.getName(), getFile(child.getPath()));
                }
            }
        }

        // transfer files to node
        if (!files.isEmpty()) {
            try {
                // async method:
                uploadMultipleFilesToNodeAndDelete(nodeAddress, files);
            } catch (IOException e) {
                log.error("Unable to transfer file to node ({})", nodeAddress);
                e.printStackTrace();
            }
    }

    @Async
    public void uploadMultipleFilesToNodeAndDelete(String nodeAddress, MultiValueMap<String, Object> files) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(files, headers);
        String serverUrl = String.format("http://%s:%s/api/replication/transfer/", nodeAddress, namingServerConfig.getPort()); // the namingserverconfig getPort is the same as our controller's port
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Boolean> response = restTemplate.postForEntity(serverUrl, requestEntity, Boolean.class);

        if (Boolean.TRUE.equals(response.getBody()))
            log.info("Succesfully transfered files {}", new ArrayList<>(files.toSingleValueMap().keySet()));
        else log.warn("Failed replicating files {}", new ArrayList<>(files.toSingleValueMap().keySet()));

        // now delete the file
        for (Object file : files.values()) {
            try {
                ((Resource) file).getFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Unable to delete file after transfer. File: {}", ((Resource) file).getFilename());
                throw e;
            }
        }
    }

    /**
     * Will look for files belonging to this node which are stored somewhere else.
     * The node will send a request to its own neighbours (next and previous) as well their respective next and previous nodes because those might still store replicas belonging to this node
     */
    public void lookForFilesAtNeighbouringNodes() {
        NextAndPreviousNode nodes = restService.getNextAndPrevious(networkService.getCurrentHash());
        ResponseEntity<String> response;

        if (nodes.getIdNext() != networkService.getCurrentHash()) {
            // ask next node for files that should belong to us
            response = restTemplate.getForEntity(String.format("http://%s:%s/api/replication/move/%s",
                    nodes.getIpNext(), namingServerConfig.getPort(), networkConfig.getHostName()), String.class);

            // now do the same for the next-next neighbour because they might still store replicas that belong to us
            NextAndPreviousNode neighboursOfNextNode = restService.getNextAndPrevious(nodes.getIdNext());
            String ipAddressNextNeighbour = neighboursOfNextNode.getIpNext();
            if (ipAddressNextNeighbour == null || neighboursOfNextNode.getIdNext() == networkService.getCurrentHash() || neighboursOfNextNode.getIdNext() == nodes.getIdNext()) // only send when the next node of my next node is not myself, and don't go to the same node again...
                log.warn("Transfer: Could not retrieve ip address of next-next neighbour.");
            else response = restTemplate.getForEntity(String.format("http://%s:%s/api/replication/move/%s",
                    ipAddressNextNeighbour, namingServerConfig.getPort(), networkConfig.getHostName()), String.class);
        } else log.info("No other nodes to transfer files from.");
    }
        if (nodes.getIdPrevious() != networkService.getCurrentHash()) {
            // ask previous node for files that should belong to us
            response = restTemplate.getForEntity(String.format("http://%s:%s/api/replication/move/%s",
                    nodes.getIpPrevious(), namingServerConfig.getPort(), networkConfig.getHostName()), String.class);

            // now do the same for the next-next neighbour because they might still store replicas that belong to us
            NextAndPreviousNode neighboursOfPreviousNode = restService.getNextAndPrevious(nodes.getIdNext());
            String ipAddressPreviousNeighbour = neighboursOfPreviousNode.getIpPrevious();
            if (ipAddressPreviousNeighbour == null || neighboursOfPreviousNode.getIdPrevious() == networkService.getCurrentHash() || neighboursOfPreviousNode.getIdPrevious() == nodes.getIdPrevious()) // only send when the previous node of my previous node is not myself. and don't go to the same node again...
                log.warn("Transfer: Could not retrieve ip address of next-next neighbour.");
            else response = restTemplate.getForEntity(String.format("http://%s:%s/api/replication/move/%s",
                    ipAddressPreviousNeighbour, namingServerConfig.getPort(), networkConfig.getHostName()), String.class);
        } else log.info("No other nodes to transfer files from.");

    public void shutdown() {
        // Find previous node -> Naming server
        int previousNode = nodeStructure.getPreviousNode();//restService.getPreviousNode(nodeStructure.getCurrentHash());
        String previousIp = restService.requestNodeIpWithHashValue(previousNode);
        //replicate(?, restService.requestNodeIpWithHashValue(previousNode)));
        // Edge case -> Previous previous
        int secondPreviousNode = restService.getPreviousNode(nodeStructure.getPreviousNode());
        String secondPreviousIp = restService.requestNodeIpWithHashValue(secondPreviousNode);

        //replicate.(?,  restService.requestNodeIpWithHashValue(secondPreviousNode));
        // Send all replicated files
        // Except not downloaded files
        replicationComponent.getReplicatedFiles().forEach(file -> {
            try {
                if (previousNode != file.getLogFile().getOwner(0).getHashValue()){
                    replicate(file.getPath(), previousIp, file.getLogFile().getPath());
                }
                else if (secondPreviousNode != nodeStructure.getCurrentHash()){
                    replicate(file.getPath(), secondPreviousIp, file.getLogFile().getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Send owner of own locals files warning
        replicationComponent.getReplicatedLocalFiles().forEach(file -> {
            String serverUrl = String.format("http://%s:%s/api/replication/warnDeletedFiles/%s", getDestination(file.getFileName()), namingServerConfig.getPort(), file.getFileName()); // the namingserverconfig getPort is the same as our controller's port
            restTemplate.put(serverUrl, Boolean.class);
        });
    }
}
}