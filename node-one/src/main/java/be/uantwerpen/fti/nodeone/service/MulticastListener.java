package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class MulticastListener {
    private final NetworkConfig networkConfig;
    private final MulticastSocket socket;
    private NodeStructure nodeStructure;

    @Async
    public void listenForMulticast() {
        while (true) {
            try {
                byte[] buffer = new byte[networkConfig.getHostName().getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // calculate hash of the node that sent the multicast
                String nodeName = new String(buffer, StandardCharsets.UTF_8);
                // ignore if same node
                if (nodeName.equals(networkConfig.getHostName())) continue;

                int nodeHash = calculateHash(nodeName);

                // calculate own hash
                int ownHash = calculateHash(networkConfig.getHostName());
                nodeStructure.setCurrentHash(ownHash);

                if (nodeHash < ownHash) nodeStructure.setPreviousNode(nodeHash);
                else if (nodeHash > ownHash) nodeStructure.setNextNode(nodeHash);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int calculateHash(String hostname) {
        return (int) (((long) hostname.hashCode() + (long) Integer.MAX_VALUE) * ((double) Short.MAX_VALUE / (2 * (double) Integer.MAX_VALUE)));
    }

    public void setNodes(NodeStructure nodeStructure) {
        this.nodeStructure = nodeStructure;
    }
}
