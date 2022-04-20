package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.config.NetworkConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Service
public class MulticastListener extends Thread {
    private final NetworkConfig networkConfig;
    private final HashService hashService;
    private final TcpService tcpService;
    private final MulticastSocket socket;

    public MulticastListener(NetworkConfig networkConfig, HashService hashService, TcpService tcpService, MulticastSocket socket) {
        this.networkConfig = networkConfig;
        this.hashService = hashService;
        this.tcpService = tcpService;
        this.socket = socket;
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[networkConfig.getHostName().getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // calculate hash of the node that sent the multicast
                String nodeName = new String(buffer, StandardCharsets.UTF_8);

                hashService.registerNode(nodeName, nodeName);

                // send the map size as unicast back to the node
                tcpService.sendUnicastResponse(hashService.mapSize(), nodeName);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
