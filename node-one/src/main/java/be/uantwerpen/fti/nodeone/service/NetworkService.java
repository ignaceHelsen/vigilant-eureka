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
            log.info("Registering node failed.");
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
        nodeShutDown(networkConfig.getHostName(), nodeStructure.getNextNode(), nodeStructure.getPreviousNode());
    }


    public void nodeShutDown(String hostname, int next, int previous) {
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
        NextAndPreviousNode nextAndPrevious = restService.getNextAndPrevious(hostname);
        sendUpdatePrevious(nextAndPrevious.getIpNext(), nextAndPrevious.getIdPrevious());
        sendUpdatePrevious(nextAndPrevious.getIpPrevious(), nextAndPrevious.getIdNext());
    }

    /**
     * For shutdown
     * @param ipAddress next node ip address
     * @param newNextNode
     */
    public void sendUpdateNext(String ipAddress, int newNextNode) {
        try (Socket socket = new Socket(ipAddress, networkConfig.getSocketPort())) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newNextNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ipAddress
     * @param newPreviousNode
     */
    public void sendUpdatePrevious(String ipAddress, int newPreviousNode) {
        try (Socket socket = new Socket(ipAddress, networkConfig.getSocketPort())) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newPreviousNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
