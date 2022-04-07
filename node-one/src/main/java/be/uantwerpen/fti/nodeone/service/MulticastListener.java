package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MulticastListener extends Thread {
    private final NetworkConfig networkConfig;
    private MulticastSocket socket;
    private NodeStructure nodeStructure;

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(4446);
            SocketAddress group = new InetSocketAddress("230.0.0.0", 4446);
            InetAddress addresss = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group, NetworkInterface.getByInetAddress(addresss));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                byte[] buffer = new byte[networkConfig.getHostName().getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // calculate hash of the node that sent the multicast
                int nodeHash = calculateHash(Arrays.toString(buffer));

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
