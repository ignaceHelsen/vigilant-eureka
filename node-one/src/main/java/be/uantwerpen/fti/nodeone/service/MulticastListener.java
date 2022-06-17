package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.HashCalculator;
import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@Slf4j
public class MulticastListener {
    private final HashCalculator hashCalculator;
    private final RestService restService;
    private final NetworkConfig networkConfig;
    private final MulticastSocket socket;
    private NodeStructure nodeStructure;
    private boolean hasShutdown = false;

    @Async
    public void listenForMulticast() {
        while (!hasShutdown) {
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
                    int ownHash = hashCalculator.calculateHash(networkConfig.getHostName());
                    nodeStructure.setCurrentHash(ownHash);

                    NextAndPreviousNode nextAndPreviousNode = restService.getNextAndPrevious(nodeStructure.getCurrentHash());
                    nodeStructure.setNextNode(nextAndPreviousNode.getIdNext());
                    nodeStructure.setPreviousNode(nextAndPreviousNode.getIdPrevious());

                }
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Socket could not be read");
            }
        }

        System.exit(0);
    }

    @PreDestroy
    public void setHasShutdown() {
        hasShutdown = true;
    }
}
