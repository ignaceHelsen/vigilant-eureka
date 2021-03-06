package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.config.NetworkConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.net.Socket;

@Service
@Slf4j
@AllArgsConstructor
public class TcpService {
    private final NetworkConfig networkConfig;

    public void sendUnicastResponse(int mapSize, String ipAddress) {
        try (Socket socket = new Socket(ipAddress, networkConfig.getSocketPort())) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(mapSize);
            outputStream.close();
        } catch (Exception e) {
            log.warn("Something went wrong while establishing tcp connection to node ({})", ipAddress);
            e.printStackTrace();
        }
    }
}
