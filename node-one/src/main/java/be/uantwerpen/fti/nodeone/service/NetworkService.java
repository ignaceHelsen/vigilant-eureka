package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class NetworkService {
    private final NetworkConfig networkConfig;
    private final RestService restService;
    private final NodeStructure nodeStructure;
    private final RestTemplate restTemplate;
    private final NamingServerConfig namingServerConfig;

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

    @Scheduled(fixedRate = 30 * 1000, initialDelay = 30 * 1000) // start after 30s after startup and send every 30s.
    public void BroadcastPresence() {
        log.info(String.format("Broadcasting presence to other nodes, current previous node: %d\t Current next node: %d (0 means the current node is its own next node)", nodeStructure.getPreviousNode(), nodeStructure.getNextNode()));
        // first go to naming server to get ip of previous and next node
        try {
            ResponseEntity<NextAndPreviousNode> ipNodes = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/getNextAndPrevious/%d",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), nodeStructure.getCurrentHash()), NextAndPreviousNode.class);

            if (ipNodes.getBody() == null) log.warn("Node could not be found");
            else {
                // this is the important part: we need to update our own structure with the new info
                nodeStructure.setNextNode(ipNodes.getBody().getIdNext());
                nodeStructure.setPreviousNode(ipNodes.getBody().getIdPrevious());

                // now send to our peers
                if (ipNodes.getBody().getIdNext() != nodeStructure.getCurrentHash()) {
                    // send notification to next
                    updateNode(ipNodes.getBody().getIpNext(), networkConfig.getUpdateNextSocketPort(), nodeStructure.getNextNode());
                }

                if (ipNodes.getBody().getIdPrevious() != nodeStructure.getCurrentHash()) {
                    // send notification to previous
                    updateNode(ipNodes.getBody().getIpPrevious(), networkConfig.getUpdatePreviousSocketPort(), nodeStructure.getPreviousNode());
                }
            }
        } catch(ResourceAccessException e) {
            log.error("Connection to {}:{} timed out.", namingServerConfig.getAddress(), namingServerConfig.getPort());
        }
    }

    private void updateNode(String ip, int port, int hash) {
        try (Socket socket = new Socket(ip, port)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(nodeStructure.getCurrentHash());
            outputStream.close();
        } catch (IOException e) {
            nodeFailure(hash);
            e.printStackTrace();
        }
    }

    public void nodeShutDown() {
        nodeShutDown(nodeStructure.getCurrentHash(), nodeStructure.getNextNode(), nodeStructure.getPreviousNode());
    }


    public void nodeShutDown(int currentHash, int nextNode, int previousNode) {
        log.info("Node request to shut down, next and previous node will be updated.");
        //Request ip with id next node namingservice (REST)
        String NextIp = restService.requestNodeIpWithHashValue(nextNode);
        //Send id previous to next node (TCP)
        sendUpdatePrevious(NextIp, nextNode, previousNode);
        //Request ip with id previous node namingservice (REST)
        String PreviousIp = restService.requestNodeIpWithHashValue(previousNode);
        //Send id next to previous node (TCP)
        sendUpdateNext(PreviousIp, previousNode, nextNode);
        restService.removeNode(new RemoveNodeRequest(currentHash));
    }

    public void nodeFailure(int hashNode) {
        log.info("Failed to communicate with other nodes, next and previous node will be updated. Current node will be removed.");
        NextAndPreviousNode nextAndPrevious = restService.getNextAndPrevious(hashNode);
        sendUpdateNext(nextAndPrevious.getIpNext(), nextAndPrevious.getIdNext(), nextAndPrevious.getIdPrevious());
        sendUpdatePrevious(nextAndPrevious.getIpPrevious(), nextAndPrevious.getIdPrevious(), nextAndPrevious.getIdNext());
        restService.removeNode(new RemoveNodeRequest(hashNode));

    }

    public void sendUpdateNext(String ipAddress, int idNext, int newNextNode) {
        try (Socket socket = new Socket(ipAddress, networkConfig.getSocketPort())) {
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
        try (Socket socket = new Socket(ipAddress, networkConfig.getSocketPort())) {
            log.info("Updating the 'previous node' parameter of the next node.");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newPreviousNode);
            outputStream.close();
        } catch (IOException e) {
            log.warn("'Previous node' parameter of next node failed to update.");
            e.printStackTrace();
            nodeFailure(idPrevious);
        }
    }
}
