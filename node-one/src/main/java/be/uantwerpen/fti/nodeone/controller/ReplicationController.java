package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.service.ReplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/replication")
public class ReplicationController {
    private final ReplicationService replicationService;

    @PostMapping(path = "/store")
    public ResponseEntity<Boolean> storeFile(@RequestParam("file") MultipartFile file) {
        try {
            boolean success = replicationService.storeFile(file, Action.LOCAL);
            if (success) return ResponseEntity.ok(true);
        } catch (IOException e) {
            log.warn("Error replicating file: {}", file.getName());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(path = "/replicate")
    public ResponseEntity<Boolean> replicateFile(@RequestParam("file") MultipartFile file) {
        try {
            boolean success = replicationService.storeFile(file, Action.REPLICATE);
            if (success) return ResponseEntity.ok(true);
        } catch (IOException e) {
            log.warn("Error replicating file: {}", file.getName());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Will search for replicas that are stored on this node but should be located in another node.
     * Called from another node.
     * @param destinationNode: The node that is asking for its replicas.
     * @return A True boolean indicating the transfer is in progress.
     */
    @GetMapping(path = "/move/{destinationNode}")
    public ResponseEntity<Boolean> move(@PathVariable String destinationNode) {
        replicationService.transferAndDeleteFiles(destinationNode);
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint where transferred files will arrive.
     * @param files: The files to store.
     * @return if transfer has started (async)
     */
    @PostMapping(path = "/transfer")
    public ResponseEntity<Boolean> transfer(@RequestParam("files") List<MultipartFile> files) {
        try {
            boolean success = replicationService.storeFiles(files, Action.REPLICATE);
            if (success) return ResponseEntity.ok(true);
        } catch (IOException e) {
            log.warn("Error transfering replication files.");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().build();
    }
}
