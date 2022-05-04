package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/replication")
public class NodeController {
    private final ReplicationConfig replicationConfig;

    @PostMapping(path = "/replicate")
    public ResponseEntity<Boolean> processFile(@RequestParam("file") MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();

            log.info("Replicating to {}", String.format("%s/%s", replicationConfig.getReplica(), file.getOriginalFilename()));
            try (FileOutputStream fos = new FileOutputStream(String.format("%s/%s", replicationConfig.getReplica(), file.getOriginalFilename()))) {
                fos.write(bytes);
            }

            return ResponseEntity.ok(true);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
