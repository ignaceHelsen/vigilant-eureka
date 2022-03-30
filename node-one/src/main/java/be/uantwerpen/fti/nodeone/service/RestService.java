package be.uantwerpen.fti.nodeone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestService {

    private final RestTemplate restTemplate;

    public String requestNodeId(String fileName) {
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://localhost:8080/api/naming/getIp/%s", fileName), String.class);

        //Handle status code

        return response.getBody();
    }
}
