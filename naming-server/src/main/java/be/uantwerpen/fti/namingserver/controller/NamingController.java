package be.uantwerpen.fti.namingserver.controller;

import be.uantwerpen.fti.namingserver.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/naming")
public class NamingController {
    private final HashService hashService;

    @GetMapping("/registerfile/{filename}")
    public ResponseEntity<String> registerFile(@PathVariable String filename) {
        String destination = hashService.registerFile(filename);
        if (destination == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(destination);
    }
}
