package be.uantwerpen.fti.nodeone.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/replication")
public class NodeController {
    @PostMapping("/replicate")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
        log.info(file.getName());

        return ResponseEntity.ok("success");
    }
}
