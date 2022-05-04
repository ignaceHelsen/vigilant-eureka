/*
package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.RegisterNodeRequest;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import be.uantwerpen.fti.nodeone.service.HashCalculator;
import be.uantwerpen.fti.nodeone.service.RestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RestTest {
    @Autowired
    RestService restService;

    @Autowired
    NetworkConfig networkConfig;

    @Autowired
    HashCalculator hashCalculator;

    @Test
    public void requestAndPingIp() throws IOException {
        String ip = restService.requestNodeIp("test.txt");
        InetAddress ipAddress = InetAddress.getByName(ip);
        assertTrue(ipAddress.isReachable(5000), "Host not reachable");

    }

    @Test
    public void registerNodeTest() {
        restService.registerNode(new RegisterNodeRequest(networkConfig.getIpAddress(), networkConfig.getHostName()));
    }

    @Test
    public void registerNodeTwice() {
        restService.registerNode(new RegisterNodeRequest(networkConfig.getIpAddress(), networkConfig.getHostName()));
        restService.registerNode(new RegisterNodeRequest(networkConfig.getIpAddress(), networkConfig.getHostName()));
    }

    @Test
    public void removeNodeTest() {
        restService.removeNode(new RemoveNodeRequest(hashCalculator.calculateHash(networkConfig.getHostName())));
    }

    @Test
    public void registerAndDeleteNodeTest() {
        restService.registerNode(new RegisterNodeRequest(networkConfig.getIpAddress(), networkConfig.getHostName()));
        restService.removeNode(new RemoveNodeRequest(hashCalculator.calculateHash(networkConfig.getHostName())));
    }

    @Test
    public void requestAndPing() throws IOException {
        String ip = restService.requestNodeIpWithHashValue(1134);
        InetAddress ipAddress = InetAddress.getByName(ip);
        assertTrue(ipAddress.isReachable(5000), "Host not reachable");
    }
}
*/
