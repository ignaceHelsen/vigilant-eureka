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

    @GetMapping("/registerFile/{hashValue}")
    public ResponseEntity<String> registerFile(@PathVariable int hashValue) {
        log.info("Ip address of node with hashValue ({}) has been requested", hashValue);
        String destination = hashService.getAddressWithKey(hashValue);
        if (destination == null) {
            log.info("No node found.");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(destination);
    }


    @PostMapping("/registerNode")
    public ResponseEntity<Boolean> registerNode(@RequestBody RegisterNodeDto registerDto) {
        log.info("The registration of node with hostname ({}) and ip address ({}) has been requested",
                registerDto.getHostname(), registerDto.getIpAddress());
        boolean succes = hashService.registerNode(registerDto.getIpAddress(), registerDto.getHostname());
        return ResponseEntity.ok(succes);
    }

    @DeleteMapping("/removeNode")
    public ResponseEntity<Boolean> removeNode(@RequestBody RemoveNodeDto removeDto) {
        log.info("The removal of node with hostname ({}) has been requested", removeDto.getHostname());
        hashService.removeNode(removeDto.getHostname());
        return ResponseEntity.ok(true);
    }


}
