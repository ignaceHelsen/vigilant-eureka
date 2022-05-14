package be.uantwerpen.fti.nodeone.controller;

import be.uantwerpen.fti.nodeone.controller.dto.TransferReplicatedDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/file")
public class FileController {

    @GetMapping("/get")
    public int get() {
        return 0;
    }

    @PostMapping("/replicateFiles")
    public ResponseEntity<Boolean> ReplicateFiles(@RequestBody TransferReplicatedDto transferDto) {
        return ResponseEntity.ok(true);
    }
}
