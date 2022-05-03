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
public class TcpListener {
    private final HashCalculator hashCalculator;
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
                            nodeStructure.setPreviousNode(hashCalculator.calculateHash(networkConfig.getHostName()));
                            nodeStructure.setNextNode(hashCalculator.calculateHash(networkConfig.getHostName()));
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

    @Async
    public void listenForUpdateNext() {
        try (ServerSocket serverSocket = new ServerSocket(networkConfig.getUpdateNextSocketPort())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int newPreviousNode = inputStream.readInt(); // the receiving body is actually this node's previous node
                        nodeStructure.setPreviousNode(newPreviousNode);
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

    @Async
    public void listenForUpdatePrevious() {
        try (ServerSocket serverSocket = new ServerSocket(networkConfig.getUpdatePreviousSocketPort())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int newNextNode = inputStream.readInt();
                        nodeStructure.setNextNode(newNextNode);
                        inputStream.close();
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
