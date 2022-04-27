package be.uantwerpen.fti.nodeone.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.net.*;

@Configuration
@AllArgsConstructor
@EnableAsync
public class MulticastConfig {
    private NetworkConfig networkConfig;

    @Bean
    public MulticastSocket socket() {
        try {
            MulticastSocket socket = new MulticastSocket(networkConfig.getMulticastPort());
            SocketAddress group = new InetSocketAddress(networkConfig.getMulticastGroupIp(), networkConfig.getMulticastPort());
            InetAddress address = InetAddress.getByName(networkConfig.getMulticastGroupIp());
            socket.joinGroup(group, NetworkInterface.getByInetAddress(address));
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
