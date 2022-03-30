package be.uantwerpen.fti.namingserver.controller;

import be.uantwerpen.fti.namingserver.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/naming")
@Slf4j
@RequiredArgsConstructor
public class NamingController {
    private final HashService hashService;

    @GetMapping("/calculateHash/{filename}")
    public ResponseEntity<Integer> getBalance(@PathVariable String filename) {
        int ipAddress = hashService.calculateHash(filename);

        return ResponseEntity.ok(ipAddress);
    }

    @GetMapping("/getIp/{filename}")
    public ResponseEntity<String> getIp(@PathVariable String filename) {

        return ResponseEntity.ok("192.168.0.1");
    }

    @PostMapping("/registerNode")
    public ResponseEntity<Void> registerNode(@RequestBody RegisterNodeDto registerDto) {
        int a = 1;
        return null;
    }

}
