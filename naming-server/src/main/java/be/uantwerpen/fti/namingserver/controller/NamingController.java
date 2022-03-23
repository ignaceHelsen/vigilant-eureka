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
@RequestMapping("/api/naming")
@Slf4j
@RequiredArgsConstructor
public class NamingController {
    private final HashService hashService;

    @GetMapping("/calculateHash/{filename}")
    public ResponseEntity<String> getBalance(@PathVariable String filename) {
        String ipAddress = hashService.calculateHash(filename);

        return ipAddress;
    }
}
