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

        if (response.getStatusCode().is4xxClientError()) {
            log.error("Client error occurred while requesting ip of node with file({})", filename);
            return null;
        }
        else if (response.getStatusCode().is5xxServerError()) {
            log.error("Server error occurred while requesting ip of node with file({})", filename);
            return null;
        }

        return response.getBody();
    }

    public boolean(RegisterRequest registerRequest) {
        ResponseEntity<Boolean> response = restTemplate.postForEntity(String.format("http://%s:%s/api/naming/registerNode/",
                namingServerConfig.getAddress(), namingServerConfig.getPort()), registerRequest, Boolean.class);

        if (response.getStatusCode().is4xxClientError()) {
            log.error("Client error occurred while registering node");
            return false;
        }
        else if (response.getStatusCode().is5xxServerError()) {
            log.error("Server error occurred while registering node");
            return false;
        }

        return Boolean.TRUE.equals(response.getBody());
    }
}
