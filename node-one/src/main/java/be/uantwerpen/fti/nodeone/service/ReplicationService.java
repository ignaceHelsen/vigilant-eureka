package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.domain.ReplicationStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@AllArgsConstructor
public class ReplicationService {
    private final ReplicationStructure replicationStructure; // TODO persistence?
    private final NamingServerConfig namingServerConfig;
    private final RestTemplate restTemplate;

    public void startReplication() {
        replicationStructure.initialize();

        // check only for local files
        replicationStructure.getLocalFiles().forEach(file -> {
            // replicate every file which has not yet been replicated
            if (!file.isReplicated()) {
                try {
                    String destination = getDestination(file.getPath());
                    replicate(file.getPath(), destination);
                } catch (IOException e) {
                    log.info("Replicating file {}", file.getPath());
                    e.printStackTrace();
                } catch (RestClientException e) {
                    log.info("Could not connect to naming server at {}", namingServerConfig.getAddress());
                }
            }
        });
    }

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
        // ask naming server where we should replicate the file to
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/replicationDestination/%s",
                namingServerConfig.getAddress(), namingServerConfig.getPort(), path), String.class);
        return response.getBody();
    }


    private void replicate(String path, String destination) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", getFile(path));
        // body.add("files", getFile());
        //body.add("files", getFile());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String serverUrl = String.format("http://%s:%s/api/replication/replicate/", destination, namingServerConfig.getPort()); // the namingserverconfig getPort is the same as our controller's port
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
        log.info("Response code: " + response.getStatusCode());
    }

    private Resource getFile(String path) {
        Path file = Paths.get(path);
        return new FileSystemResource(file.toFile());
    }
}