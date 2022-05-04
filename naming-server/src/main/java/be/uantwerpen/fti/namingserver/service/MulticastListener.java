package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.config.NetworkConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@AllArgsConstructor
public class MulticastListener {
    private final NetworkConfig networkConfig;
    private final HashService hashService;
    private final TcpService tcpService;
    private final MulticastSocket socket;

    @Async
    public void listenForMulticast() {
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
                log.warn("Something went wrong while listening for multicast requests");
                e.printStackTrace();
            }
        }
    }
}
