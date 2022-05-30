package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.config.component.HashCalculator;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
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

    @Async
    public void listenForMulticast() {
        while (true) {
            if (socket.isClosed()) {
                System.exit(0);
            } else {
                try {
                    byte[] buffer = new byte[networkConfig.getHostName().getBytes().length];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // calculate hash of the node that sent the multicast
                    String nodeName = new String(buffer, StandardCharsets.UTF_8);
                    // ignore if same node
                    if (nodeName.equals(networkConfig.getHostName())) continue;

                    int nodeHash = hashCalculator.calculateHash(nodeName);

                    int ownHash = hashCalculator.calculateHash(networkConfig.getHostName());
                    nodeStructure.setCurrentHash(ownHash);

                    NextAndPreviousNode nextAndPreviousNode = restService.getNextAndPrevious(nodeStructure.getCurrentHash());
                    nodeStructure.setNextNode(nextAndPreviousNode.getIdNext());
                    nodeStructure.setPreviousNode(nextAndPreviousNode.getIdPrevious());

                /* if (nodeHash < ownHash && nodeStructure.getPreviousNode() < nodeHash) nodeStructure.setPreviousNode(nodeHash);
                if (nodeHash > ownHash && nodeStructure.getNextNode() > nodeHash) nodeStructure.setNextNode(nodeHash);*/

                } catch (IOException e) {
                    e.printStackTrace();
                    log.warn("Socket could not be read");
                }
            }
        }
    }
}
