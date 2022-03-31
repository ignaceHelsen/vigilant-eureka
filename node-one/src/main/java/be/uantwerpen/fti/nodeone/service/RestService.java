package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.domain.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestService {
    private final RestTemplate restTemplate;
    private final NamingServerConfig namingServerConfig;

    public String requestNodeId(String filename) {
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/getIp/%s",
                namingServerConfig.getAddress(), namingServerConfig.getPort(), filename), String.class);

        //Handle status code

        return response.getBody();
    }

    public void registerNode(RegisterRequest registerRequest) {
        ResponseEntity<Void> response = restTemplate.postForEntity(String.format("http://%s:%s/api/naming/registerNode/",
                namingServerConfig.getAddress(), namingServerConfig.getPort()), registerRequest, Void.class);

        //Handle status code
    }
}
