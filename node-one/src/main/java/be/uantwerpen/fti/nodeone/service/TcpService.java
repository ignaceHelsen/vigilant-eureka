package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Service
@RequiredArgsConstructor
public class TcpService implements ApplicationListener<ContextRefreshedEvent> {

    private final NodeStructure nodeStructure;

    @Async
    public void listenForUpdateNext() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int newNextNode = inputStream.readInt();
                        nodeStructure.setNext(newNextNode);
                        inputStream.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void listenForUpdatePrevious() {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int newPreviousNode = inputStream.readInt();
                        nodeStructure.setPrevious(newPreviousNode);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateNext(String ipAddress, int newNextNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newNextNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdatePrevious(String ipAddress, int newPreviousNode) {
        try (Socket socket = new Socket(ipAddress, 5000)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(newPreviousNode);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        listenForUpdateNext();
        listenForUpdatePrevious();
    }
}
