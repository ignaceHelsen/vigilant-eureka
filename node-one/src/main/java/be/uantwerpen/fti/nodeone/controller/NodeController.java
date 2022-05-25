package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.service.FileService;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.ShutdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api")
public class NodeController {
    private final FileService fileService;
    private final NetworkService networkService;
    private final ShutdownService shutdownService;

    @GetMapping("/local/all")
    public ResponseEntity<List<String>> getLocalFiles() {
        try {
            return ResponseEntity.ok(fileService.getAllLocalFiles());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/replicated/all")
    public ResponseEntity<List<String>> getReplicatedFiles() {
        try {
            return ResponseEntity.ok(fileService.getAllReplicatedFiles());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/config")
    public ResponseEntity<NodeStructureDto> getConfig() {
        try {
            return ResponseEntity.ok(networkService.getConfig());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Boolean> shutdown() {
        shutdownService.scheduleShutdown();

        return ResponseEntity.ok(true);
    }
}