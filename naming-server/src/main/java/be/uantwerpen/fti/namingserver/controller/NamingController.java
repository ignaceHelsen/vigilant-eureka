package be.uantwerpen.fti.namingserver.controller;

import be.uantwerpen.fti.namingserver.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/naming")
public class NamingController {
    private final HashService hashService;

    @GetMapping("/registerFile/{filename}")
    public ResponseEntity<String> registerFile(@PathVariable String filename) {
        log.info("Ip address of node with file ({}) has been requested", filename);
        String destination = hashService.registerFile(filename);
        if (destination == null) {
            log.info("No node found.");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(destination);
    }
}
