package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkService {
    private final NetworkConfig networkConfig;
    private final RestService restService;
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
            log.warn("Registering node failed.");
        }
    }

    public void nodeShutDown() {
        nodeShutDown(networkConfig.getHostName(), nodeStructure.getNextNode(), nodeStructure.getPreviousNode());
    }


    public void nodeShutDown(String hostname, int next, int previous) {
        log.info("Node request to shut down, next and previous node will be updated.");
        //Request ip with id next node namingservice (REST)
        String NextIp = restService.requestNodeIpWithHashValue(next);
        //Send id previous to next (TCP)
        sendUpdatePrevious(NextIp, previous);
        //Request ip with id previous node namingservice (REST)
        String PreviousIp = restService.requestNodeIpWithHashValue(previous);
        //Send id next to previous (TCP)
        sendUpdateNext(PreviousIp, next);
        restService.removeNode(new RemoveNodeRequest(hostname));
    }

    public void nodeFailure(String hostname) {
        log.info("Failed to communicate with other nodes, next and previous node will be updated. Current node will be removed.");
        NextAndPreviousNode nextAndPrevious = restService.getNextAndPrevious(hostname);
        sendUpdatePrevious(nextAndPrevious.getIpNext(), nextAndPrevious.getIdPrevious());
        sendUpdatePrevious(nextAndPrevious.getIpPrevious(), nextAndPrevious.getIdNext());
    }

    public void sendUpdateNext(String ipAddress, int newNextNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            log.info("Updating the 'next node' parameter of the previous node.");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newNextNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("'Next node' parameter of previous node failed to update.");
        }
    }

    public void sendUpdatePrevious(String ipAddress, int newPreviousNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            log.info("Updating the 'previous node' parameter of the next node.");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newPreviousNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("'Previous node' parameter of next node failed to update.");
        }
    }

}
