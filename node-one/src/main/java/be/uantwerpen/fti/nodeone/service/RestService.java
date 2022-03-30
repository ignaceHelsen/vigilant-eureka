package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.domain.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestService {

    private final RestTemplate restTemplate;

    public String requestNodeId(String filename) {
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://localhost:8080/api/naming/getIp/%s", filename), String.class);

        //Handle status code

        return response.getBody();
    }

    public void registerNode(RegisterRequest registerRequest) {

        ResponseEntity<Void> response = restTemplate.postForEntity("http://localhost:8080/api/naming/registerNode/", registerRequest, Void.class);

        //Handle status code
    }
}
