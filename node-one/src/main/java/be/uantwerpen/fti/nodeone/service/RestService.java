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

    public int requestNodeId(String fileName) {
        ResponseEntity<Integer> response = restTemplate.getForEntity(String.format("URL/%s", fileName), Integer.class);

        //Handle status code

        return response.getBody();
    }
}
