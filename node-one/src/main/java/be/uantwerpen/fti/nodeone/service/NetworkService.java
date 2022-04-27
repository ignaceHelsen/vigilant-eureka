package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RegisterNodeRequest;
import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkService {
    private final NetworkConfig networkConfig;
    private final RestService restService;
    private final TcpService tcpService;
    private final NodeStructure nodeStructure;

    @Async
    public void registerNode() {
        // https://www.baeldung.com/java-broadcast-multicast
        // multicast to group
        DatagramSocket socket;
        InetAddress group;

        try {
            log.info(String.format("Registering node: %s", networkConfig.getHostName()));
            socket = new DatagramSocket();
            group = InetAddress.getByName(networkConfig.getMulticastGroupIp());
            byte[] buffer = networkConfig.getHostName().getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, networkConfig.getMulticastPort());
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("Registering node failed.");
        }
    }

    public void nodeShutDown() {
        nodeShutDown(networkConfig.getHostName(), nodeStructure.getNextNode(), nodeStructure.getPreviousNode());
    }


    public void nodeShutDown(String hostname, int next, int previous) {
        //Request ip with id next node namingservice (REST)
        String NextIp = restService.requestNodeIpWithHashValue(next);
        //Send id previous to next (TCP)
        tcpService.sendUpdatePrevious(NextIp, previous);
        //Request ip with id previous node namingservice (REST)
        String PreviousIp = restService.requestNodeIpWithHashValue(previous);
        //Send id next to previous (TCP)
        tcpService.sendUpdateNext(PreviousIp, next);
        restService.removeNode(new RemoveNodeRequest(hostname));
    }

    public void nodeFailure(String hostname) {
        NextAndPreviousNode nextAndPrevious = restService.getNextAndPrevious(hostname);
        tcpService.sendUpdatePrevious(nextAndPrevious.getIpNext(), nextAndPrevious.getIdPrevious());
        tcpService.sendUpdatePrevious(nextAndPrevious.getIpPrevious(), nextAndPrevious.getIdNext());
    }
}
