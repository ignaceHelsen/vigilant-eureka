package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Service
@AllArgsConstructor
public class TcpService {
    private final HashService hashService;
    private final NodeStructure nodeStructure;
    private final NetworkConfig networkConfig;

    @Async
    public void listenUnicastResponse() {
        try (ServerSocket serverSocket = new ServerSocket(networkConfig.getSocketPort())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int mapSize = inputStream.readInt();
                        if (mapSize == 1) {
                            nodeStructure.setPreviousNode(hashService.calculateHash(networkConfig.getHostName()));
                            nodeStructure.setNextNode(hashService.calculateHash(networkConfig.getHostName()));
                        }
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
