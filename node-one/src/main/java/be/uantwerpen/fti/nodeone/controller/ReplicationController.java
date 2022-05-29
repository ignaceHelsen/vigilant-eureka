package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.domain.Action;
import be.uantwerpen.fti.nodeone.service.ReplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/replication")
public class ReplicationController {
    private final ReplicationService replicationService;

    @PostMapping(path = "/store")
    public ResponseEntity<Boolean> storeFile(@RequestParam("file") MultipartFile file) {
        try {
            boolean success = replicationService.storeFile(file, file, Action.LOCAL);
            if (success) return ResponseEntity.ok(true);
        } catch (IOException e) {
            log.warn("Error replicating file: {}", file.getName());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(path = "/replicate")
    public ResponseEntity<Boolean> replicateFile(@RequestParam("file") MultipartFile file, @RequestParam("logFile") MultipartFile logFile) {
        try {
            boolean success = replicationService.storeFile(file, logFile, Action.REPLICATE);
            if (success) return ResponseEntity.ok(true);
        } catch (IOException e) {
            log.warn("Error replicating file: {}", file.getName());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Will search for replicas that are stored on this node but should be located in another node.
     * Called from another node.
     *
     * @param destinationNode: The node that is asking for its replicas.
     * @return A True boolean indicating the transfer is in progress.
     */
    @GetMapping(path = "/move/{destinationNode}")
    public ResponseEntity<Boolean> move(@PathVariable String destinationNode) {
        boolean success = replicationService.transferAndDeleteFiles(destinationNode);
        return ResponseEntity.ok(success);
    }

    /**
     * Endpoint where transferred files will arrive.
     *
     * @param files: The files to store.
     * @return Boolean indicating if transfer has succeeded (async) OR String when storing one or more files has failed.
     */
    @PostMapping(path = "transfer")
    public ResponseEntity<Boolean> transfer(@RequestParam("files") List<MultipartFile> files, @RequestParam("logFiles") List<MultipartFile> logFiles) {
        boolean success = replicationService.storeFiles(files, logFiles, Action.REPLICATE);
        if (success) return ResponseEntity.ok(true);

        return ResponseEntity.badRequest().build();
    }

    @PutMapping(path = "/warnDeletedFiles")
    public ResponseEntity<Boolean> warnDeletedFiles(@PathVariable String fileName) {
        // No action taken at the moment
        return ResponseEntity.ok(true);
    }
}
