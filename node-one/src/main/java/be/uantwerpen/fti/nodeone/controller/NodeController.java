package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
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
    private final FileService fileService;
    private final ReplicationComponent replicationComponent;
    private final NetworkService networkService;

    @GetMapping("/local/all")
    public ResponseEntity<List<String>> getLocalFiles() {
        return ResponseEntity.ok(fileService.getAllLocalFiles());
    }

    @GetMapping("/replicated/all")
    public ResponseEntity<List<String>> getReplicatedFiles() {
        return ResponseEntity.ok(fileService.getAllReplicatedFiles());
    }

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