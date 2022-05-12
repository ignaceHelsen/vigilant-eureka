package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                        replicate(file.getPath(), destination);
                        file.setReplicated(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (RestClientException e) {
                        log.info("Could not connect to server at {}", destination);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    log.error("Unable to connect to naming server");
                    e.printStackTrace();
                }
            }
        });
    }

    // TODO when a file has been added, replicate it immediately


    /**
     * Check if all needed directories for replication are present.
     */
    public void precheck() {
        File localDir = new File("src/resources/storage/local");
        File replicaDir = new File("src/resources/storage/replica");
        File logDir = new File("src/resources/storage/log");

        if (!localDir.exists()) {
            log.info("Created local directory.");
            try {
                localDir.mkdirs();
            } catch (SecurityException e) {
                log.error("Unable to create storage/local directory. Security Exception");
            }
        }

        if (!replicaDir.exists()) {
            log.info("Created replica directory.");
            try {
                replicaDir.mkdirs();
            } catch (SecurityException e) {
                log.error("Unable to create storage/replica directory. Security Exception");
            }
        }

        if (!logDir.exists()) {
            log.info("Created log directory.");
            try {
                logDir.mkdirs();
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
    private void replicate(String path, String destination) throws IOException {
        log.info("Replicating file {}", path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        if (path == null) log.warn("Path of file has returned null");
        body.add("file", getFile(path));

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
    public boolean storeFile(MultipartFile file, Action action) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
            String path;
            if (action == Action.LOCAL) {
                path = String.format("%s/%s", replicationConfig.getLocal(), file.getOriginalFilename());
                log.info("Saving to {}", path);
                // Also replicate it
                FileStructure fileStruct = new FileStructure(path, false);
                replicationComponent.addLocalFile(fileStruct);

                // Since the new file is stored locally, we can already replicate it
                try {
                    String destination = getDestination(path);

                    try {
                        replicate(path, destination);
                        fileStruct.setReplicated(true);
                    } catch (IOException e) {
                        log.error("Error occured while trying to replicate file {}", path);
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
                path = String.format("%s/%s", replicationConfig.getReplica(), file.getOriginalFilename());
                log.info("Replicating to {}", path);
            }

            File outputFile = new File(path);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(bytes);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}