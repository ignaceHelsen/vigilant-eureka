package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import be.uantwerpen.fti.nodeone.domain.LogStructure;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
     *
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
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/replicationDestination/%d",
                namingServerConfig.getAddress(), namingServerConfig.getPort(), hashCalculator.calculateHash(filename)), String.class);
        return response.getBody();
    }

    /**
     * Replication of a file.
     * @param path: The path to the file.
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
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Boolean> response = restTemplate.postForEntity(serverUrl, requestEntity, Boolean.class);

        if (Boolean.TRUE.equals(response.getBody())) log.info("Succesfully replicated file {}", path);
        else log.warn("Failed replicating file {}", path);
    }

    private Resource getFile(String path) {
        Path file = Paths.get(path);
        return new FileSystemResource(file.toFile());
    }

    /**
     * Takes care of the storage of a file.
     * If it's a file destined for local storage, besides storing, we also replicate it immediately.
     * If it's a replication file, we just store it in /replication.
     * @param file: The path to the file.
     * @param action: Local or Replica file.
     * @return If storing didn't result in an error.
     */
    public boolean storeFile(MultipartFile file, MultipartFile logFile, Action action) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
            String filePath;
            String logPath = replicationComponent.createLogPath(file.getOriginalFilename());
            if (action == Action.LOCAL) {
                filePath = replicationComponent.createFilePath(file.getOriginalFilename());

                log.info("Saving to {}", filePath);
                // Also replicate it
                FileStructure fileStruct = new FileStructure(filePath, false, new LogStructure(logPath));
                replicationComponent.addLocalFile(fileStruct);
                replicationComponent.saveLog(fileStruct.getLogFile(), file.getOriginalFilename());
                // Since the new file is stored locally, we can already replicate it
                try {
                    String destination = getDestination(filePath);

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
                } catch (Exception e) {
                    log.error("Unable to connect to naming server");
                    e.printStackTrace();
                    return false;
                }
            } else {
                filePath = replicationComponent.createFilePath(file.getOriginalFilename());
                File outputFile = new File(logPath);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(bytes);
                }
                FileStructure fileStructure = new FileStructure(filePath, true,
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void shutdown() {
        // Find previous node -> Naming server
        int previousNode = nodeStructure.getPreviousNode();//restService.getPreviousNode(nodeStructure.getCurrentHash());
        String previousIp = restService.requestNodeIpWithHashValue(previousNode);
        //replicate(?, restService.requestNodeIpWithHashValue(previousNode)));
        // Edge case -> Previous previous
        int secondPreviousNode = restService.getPreviousNode(nodeStructure.getPreviousNode());
        String secondPreviouIp = restService.requestNodeIpWithHashValue(secondPreviousNode);

        //replicate.(?,  restService.requestNodeIpWithHashValue(secondPreviousNode));
        // Send all replicated files
        // Except not downloaded files
        replicationComponent.getReplicatedFiles().forEach(file -> {
            try {
                replicate(file.getPath(), previousIp, file.getLogFile().getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Send owner of own locals files warning

    }
}