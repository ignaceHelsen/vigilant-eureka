package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.config.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.service.FileService;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.ShutdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api")
public class NodeController {
    private final ShutdownService shutdownService;
    private final FileService fileService;
    private final NetworkService networkService;

    private final ReplicationComponent replicationComponent;

    @GetMapping("/local/all")
    public ResponseEntity<List<String>> getLocalFiles() {
        log.info("Returning all local files.");
        try {
            return ResponseEntity.ok(fileService.getAllLocalFiles());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }    }

    @GetMapping("/replicated/all")
    public ResponseEntity<List<String>> getReplicatedFiles() {
        try {
            return ResponseEntity.ok(fileService.getAllReplicatedFiles());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }    }

    @GetMapping("/replicated/log")
    public ResponseEntity<List<String>> getLogFiles() {
        return ResponseEntity.ok(replicationComponent.getReplicatedFiles().stream().map(file -> file.getLogFile().getPath()).collect(Collectors.toList()));
    }

    @GetMapping("/config")
    public ResponseEntity<NodeStructureDto> getConfig() {
        try {
            return ResponseEntity.ok(networkService.getConfig());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }
}