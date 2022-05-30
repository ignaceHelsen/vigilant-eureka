package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.config.component.HashCalculator;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class MulticastListener {
    private final HashCalculator hashCalculator;
    private final RestService restService;
    private final NetworkConfig networkConfig;
    private final MulticastSocket socket;
    private final NodeStructure nodeStructure;
    private boolean hasShutdown;

    public MulticastListener(HashCalculator hashCalculator, RestService restService, NetworkConfig networkConfig, MulticastSocket socket, NodeStructure nodeStructure) {
        this.hashCalculator = hashCalculator;
        this.restService = restService;
        this.networkConfig = networkConfig;
        this.socket = socket;
        this.nodeStructure = nodeStructure;
        this.hasShutdown = false;
    }

    @Async
    public void listenForMulticast() {
        while (true) {
            if (socket.isClosed()) {
                log.warn("Previous socket closed exception is normal.");
                break;
            }
            try {
                byte[] buffer = new byte[networkConfig.getHostName().getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // calculate hash of the node that sent the multicast
                String nodeName = new String(buffer, StandardCharsets.UTF_8);
                // ignore if same node
                if (!nodeName.equals(networkConfig.getHostName())) {

                    int nodeHash = hashCalculator.calculateHash(nodeName);

                    int ownHash = hashCalculator.calculateHash(networkConfig.getHostName());
                    nodeStructure.setCurrentHash(ownHash);

                    NextAndPreviousNode nextAndPreviousNode = restService.getNextAndPrevious(nodeStructure.getCurrentHash());
                    nodeStructure.setNextNode(nextAndPreviousNode.getIdNext());
                    nodeStructure.setPreviousNode(nextAndPreviousNode.getIdPrevious());

                    /* if (nodeHash < ownHash && nodeStructure.getPreviousNode() < nodeHash) nodeStructure.setPreviousNode(nodeHash);
                    if (nodeHash > ownHash && nodeStructure.getNextNode() > nodeHash) nodeStructure.setNextNode(nodeHash);*/
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Socket could not be read");
            }
        }

        if (hasShutdown)
            System.exit(0);
    }

    public void setHasShutdown(boolean shutdown) {
        this.hasShutdown = shutdown;
    }
}
