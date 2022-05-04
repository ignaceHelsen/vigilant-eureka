package be.uantwerpen.fti.nodeone.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/replication")
public class NodeController {
    @PostMapping(path = "/replicate/")
    public ResponseEntity<String> processFile(@RequestParam("file") MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
            System.out.println("File Name: " + file.getOriginalFilename());
            System.out.println("File Content Type: " + file.getContentType());
            System.out.println("File Content:\n" + new String(bytes));

            return ResponseEntity.ok("host4.group5.6dist");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
