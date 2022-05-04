package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.ReplicationStructure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

@Service
@Slf4j
public class ReplicationService {
    private final ReplicationStructure replicationStructure; // TODO persistence?
    private final NetworkConfig networkConfig;
    private final NamingServerConfig namingServerConfig;
    private final RestTemplate restTemplate;

    public ReplicationService(NetworkConfig networkConfig, NamingServerConfig namingServerConfig, RestTemplate restTemplate) {
        this.networkConfig = networkConfig;
        this.namingServerConfig = namingServerConfig;
        this.restTemplate = restTemplate;

        // TODO load json into replicationStructure
        replicationStructure = new ReplicationStructure();
    }

    public void startReplication() {
        // check only for local files
        replicationStructure.getFiles().forEach(file -> {
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
        try (Socket socket = new Socket(destination, networkConfig.getReplicationSocketPort())) { // port 5003
            // Read file
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            int bytes;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            // send file size
            dataOutputStream.writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            fileInputStream.close();

            dataOutputStream.close();
        }
    }

    /**
     * Listens for incoming replications
     */
    @Async
    public void startReplicationListener() {
        try (ServerSocket serverSocket = new ServerSocket(networkConfig.getReplicationSocketPort())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int bytes;
                        // store to resources/storage/replica
                        FileOutputStream fileOutputStream = new FileOutputStream(String.format("resources/storage/replica/newfile_%d", new Random().nextInt()));

                        long size = inputStream.readLong();     // read file size
                        byte[] buffer = new byte[1024];
                        while (size > 0 && (bytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            fileOutputStream.write(buffer, 0, bytes);
                            size -= bytes;      // read upto file size
                        }
                        fileOutputStream.close();


                        inputStream.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}