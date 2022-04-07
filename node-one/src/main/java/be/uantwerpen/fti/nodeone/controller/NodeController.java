package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.controller.dto.NodeStructureDto;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/node")
public class NodeController {
    private final NetworkService networkService;

    @PostMapping("registerNode")
    public ResponseEntity<Boolean> registerFile(@RequestBody NodeStructureDto structure) {
        networkService.setNodeStructure(structure);

        return ResponseEntity.ok(true);
    }
}
