package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkService implements ApplicationListener<ContextRefreshedEvent> {
    private final NetworkConfig networkConfig;
    private final RestService restService;
    private final NodeStructure nodeStructure;
    private final MulticastListener listener;

    @Async
    public void registerNode() {
        // https://www.baeldung.com/java-broadcast-multicast
        // multicast to group
        DatagramSocket socket;
        InetAddress group;

        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(networkConfig.getMulticastGroupIp());
            byte[] buffer = networkConfig.getHostName().getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4446);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeNode() {
        RemoveNodeRequest removeNodeRequest = new RemoveNodeRequest(networkConfig.getHostName());
        boolean success = restService.removeNode(removeNodeRequest);

        if (success) {
            log.info("Removing node was successful");
        } else {
            log.warn("Removing node failed");
            // Handle failure
        }
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) { // TODO replace by ApplicationReadyEvent ?
        // https://stackoverflow.com/questions/20275952/java-listen-to-contextrefreshedevent
        registerNode();
    }

    public void setNodeStructure(NodeStructureDto structure) {
        this.nodeStructure.setNextNode(structure.getNextNode());
        this.nodeStructure.setPreviousNode(structure.getPreviousNode());
    }
}
