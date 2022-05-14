package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.RequiredArgsConstructor;
import be.uantwerpen.fti.nodeone.controller.dto.ReplicatedFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ReplicationService {
    private final NodeStructure nodeStructure;
    private final RestService restService;


    private final RestTemplate restTemplate;

    public void shutdown() {
        // Find previous node -> Naming server
        int previousNode = nodeStructure.getPreviousNode();//restService.getPreviousNode(nodeStructure.getCurrentHash());
            //replicate(?, restService.requestNodeIpWithHashValue(previousNode)));
        // Edge case -> Previous previous
        int secondPreviousNode = restService.getPreviousNode(nodeStructure.getPreviousNode());
            //replicate.(?,  restService.requestNodeIpWithHashValue(secondPreviousNode));
        // Send all replicated files
        // Except not downloaded files


        // Send log file

        // Send owner of own locals files warning

    }

    public void sendReplicatedFiles() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        //body.add("replicatedFiles", new ReplicatedFileDto("test.txt", getFile("pom.xml"), getFile("pom.xml")));
        //body.add("replicatedFiles", new ReplicatedFileDto("test.txt", getFile("pom.xml"), getFile("pom.xml")));
        //body.add("replicatedFiles", new ReplicatedFileDto("test.txt", getFile("pom.xml"), getFile("pom.xml")));
        body.add("replicatedFiles", getFile("pom.xml"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String serverUrl = "http://localhost:6968/api/file/replicateFiles";
        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);

    }

    public Resource getFile(String path) {
        try {
            return new FileSystemResource(File.createTempFile(path, ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
