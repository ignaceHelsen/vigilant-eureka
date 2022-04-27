package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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

    // TODO: regularly broadcast (unicast) presence to other nodes (=next and previous node)
    @Scheduled(initialDelay = 30 * 1000, fixedRate = 30 * 1000) // start after 30s after startup and send every 30s.
    private void BroadcastPresence() {
        log.info(String.format("Current previous node: %d\t Current next node: %d", nodeStructure.getPreviousNode(), nodeStructure.getNextNode()));
        // first go to naming server to get ip of previous and next node
        // send notification to next

        // send notification to previous
    }

    public void nodeShutDown() {
        nodeShutDown(nodeStructure.getCurrentHash(), nodeStructure.getNextNode(), nodeStructure.getPreviousNode());
    }


    public void nodeShutDown(int currentHash, int nextNode, int previousNode) {
        log.info("Node request to shut down, next and previous node will be updated.");
        //Request ip with id next node namingservice (REST)
        String NextIp = restService.requestNodeIpWithHashValue(nextNode);
        //Send id previous to next (TCP)
        sendUpdatePrevious(NextIp, nextNode, previousNode);
        //Request ip with id previous node namingservice (REST)
        String PreviousIp = restService.requestNodeIpWithHashValue(previousNode);
        //Send id next to previous (TCP)
        sendUpdateNext(PreviousIp, previousNode, nextNode);
        restService.removeNode(new RemoveNodeRequest(currentHash));
    }

    public void nodeFailure(int hashNode) {
        log.info("Failed to communicate with other nodes, next and previous node will be updated. Current node will be removed.");
        NextAndPreviousNode nextAndPrevious = restService.getNextAndPrevious(hashNode);
        sendUpdateNext(nextAndPrevious.getIpNext(), nextAndPrevious.getIdNext(), nextAndPrevious.getIdPrevious());
        sendUpdatePrevious(nextAndPrevious.getIpPrevious(), nextAndPrevious.getIdPrevious(), nextAndPrevious.getIdNext());
    }

    public void sendUpdateNext(String ipAddress, int idNext, int newNextNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            log.info("Updating the 'next node' parameter of the previous node.");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newNextNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("'Next node' parameter of previous node failed to update.");
            nodeFailure(idNext);
        }
    }

    public void sendUpdatePrevious(String ipAddress, int idPrevious, int newPreviousNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            log.info("Updating the 'previous node' parameter of the next node.");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newPreviousNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("'Previous node' parameter of next node failed to update.");
            nodeFailure(idPrevious);
        }
    }

}
