package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Service
@Slf4j
@AllArgsConstructor
public class TcpListener {
    private final HashCalculator hashCalculator;
    private final RestService restService;
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
                            nodeStructure.setPreviousNode(nodeStructure.getCurrentHash());
                            nodeStructure.setNextNode(nodeStructure.getCurrentHash());
                        }
                        else {
                            NextAndPreviousNode nextAndPreviousNode = restService.getNextAndPrevious(nodeStructure.getCurrentHash());
                            nodeStructure.setNextNode(nextAndPreviousNode.getIdNext());
                            nodeStructure.setPreviousNode(nextAndPreviousNode.getIdPrevious());
                        }
                        inputStream.close();
                        clientSocket.close();
                    } catch (Exception e) {
                        log.error("Error while receiving unicast from naming server");
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
                        int newNextNode = inputStream.readInt();
                        nodeStructure.setNextNode(newNextNode);
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
