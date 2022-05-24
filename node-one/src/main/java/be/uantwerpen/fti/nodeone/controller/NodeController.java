package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/files")
public class NodeController {
    private final ReplicationComponent replicationComponent;

    @GetMapping("/local/all")
    public ResponseEntity<List<String>> getLocalFiles() {
        return ResponseEntity.ok(replicationComponent.getLocalFiles().stream().map(FileStructure::getPath).collect(Collectors.toList()));
    }

    @GetMapping("/replicated/all")
    public ResponseEntity<List<String>> getReplicatedFiles() {
        return ResponseEntity.ok(replicationComponent.getReplicatedFiles().stream().map(FileStructure::getPath).collect(Collectors.toList()));
    }
}