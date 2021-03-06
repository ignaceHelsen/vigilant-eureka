package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NamingServerConfig;
import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.RegisterNodeRequest;
import be.uantwerpen.fti.nodeone.domain.RemoveNodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    private final NamingServerConfig namingServerConfig;

    public String requestNodeIp(String filename) {
        log.info("Requesting ip address for file ({})", filename);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/registerfile/%s",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), filename), String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while requesting ip of node with file({})", filename);
            return null;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while requesting ip of node with file({})", filename);
            return null;
        }
    }

    @Deprecated
    public boolean registerNode(RegisterNodeRequest registerRequest) {
        log.info("Requesting node in naming server");
        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(String.format("http://%s:%s/api/naming/registerNode/",
                    namingServerConfig.getAddress(), namingServerConfig.getPort()), registerRequest, Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while registering node");
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while registering node");
            return false;
        }
    }

    public boolean removeNode(RemoveNodeRequest deleteRequest) {
        log.info("Removing node in naming server");

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(String.format("http://%s:%s/api/naming/removeNode/",
                            namingServerConfig.getAddress(), namingServerConfig.getPort()), HttpMethod.DELETE,
                    new HttpEntity<>(deleteRequest), Boolean.class);

            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while removing node");
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while removing node");
            return false;
        }
    }

    public String requestNodeIpWithHashValue(int hashValue) {
        log.info("Requesting ip address with hash value for file ({})", hashValue);
        if (hashValue == 0) return null; // in some weird cases hashvalue can be 0
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/address/%d",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), hashValue), String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while requesting ip of node with hash value({})", hashValue);
            return null;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while requesting ip of node with hash value({})", hashValue);
            return null;
        }
    }

    public NextAndPreviousNode getNextAndPrevious(int hash) {
        try {
            ResponseEntity<NextAndPreviousNode> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/getNextAndPrevious/%d",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), hash), NextAndPreviousNode.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while requesting next and previous node from node with hash value {}", hash);
            return null;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while requesting next and previous node from node with hash value {}", hash);
            return null;
        } catch (Exception e) {
            log.error("Unable to connect to naming server.");
            return null;
        }
    }

    public int getPreviousNode(int hash) {
        try {
            ResponseEntity<Integer> response = restTemplate.getForEntity(String.format("http://%s:%s/api/naming/getPreviousNode/%d",
                    namingServerConfig.getAddress(), namingServerConfig.getPort(), hash), Integer.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error occurred while requesting previous node from node with hash value {}", hash);
            return 0;
        } catch (HttpServerErrorException e) {
            log.error("Server error occurred while requesting previous node from node with hash value {}", hash);
            return 0;
        }
    }
}
