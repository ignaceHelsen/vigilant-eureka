package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Service
public class TcpListener {
    private final HashCalculator hashCalculator;
    private final NodeStructure nodeStructure;
    private final NetworkConfig networkConfig;
    private final int unicastResponsePort;
    private final int unicastNextNodePort;
    private final int unicastPreviousNodePort;


    public TcpListener(HashCalculator hashCalculator, NodeStructure nodeStructure, NetworkConfig networkConfig) {
        this.hashCalculator = hashCalculator;
        this.nodeStructure = nodeStructure;
        this.networkConfig = networkConfig;

        unicastResponsePort = networkConfig.getSocketPort();
        // listenForUpdateNext runs on port 5000, we just add one so we don't get an PortAlreadyInUse error.
        unicastNextNodePort = networkConfig.getSocketPort() + 1;
        unicastPreviousNodePort = networkConfig.getSocketPort() + 2;
    }

    @Async
    public void listenUnicastResponse() {
        try (ServerSocket serverSocket = new ServerSocket(unicastResponsePort)) {
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
        try (ServerSocket serverSocket = new ServerSocket(unicastNextNodePort)) {
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
        try (ServerSocket serverSocket = new ServerSocket(unicastPreviousNodePort)) {
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
