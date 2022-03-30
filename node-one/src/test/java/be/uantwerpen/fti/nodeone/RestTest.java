package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.RestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
public class RestTest {

    @Autowired
    RestService restService;

    @Test
    public void requestIp() {
        String ip = restService.requestNodeId("test.txt");
        int test = 0;
    }
}
