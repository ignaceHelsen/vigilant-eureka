package be.uantwerpen.fti.namingserver.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.net.Socket;

@Service
@AllArgsConstructor
public class TcpService {
    public void sendUnicastResponse(int mapSize, String node) {
        try(Socket socket = new Socket(node,8080)) {
            // Read file
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // send file size
            dataOutputStream.writeLong(mapSize);

            dataOutputStream.close();

        }  catch (Exception e){
            e.printStackTrace();
        }
    }
}
