package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.service.ReplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/replication")
public class NodeController {
    private final ReplicationService replicationService;

    @PostMapping(path = "/store")
    public ResponseEntity<Boolean> storeFile(@RequestParam("file") MultipartFile file) {
        boolean success = replicationService.storeFile(file, null, Action.LOCAL);

        if (success)
            return ResponseEntity.ok(true);

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(path = "/replicate")
    public ResponseEntity<Boolean> replicateFile(@RequestParam("file") MultipartFile file, @RequestParam("log") MultipartFile log) {
        boolean success = replicationService.storeFile(file, log, Action.REPLICATE);

        if (success)
            return ResponseEntity.ok(true);

        return ResponseEntity.badRequest().build();
    }
}
