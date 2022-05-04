package be.uantwerpen.fti.namingserver.controller;

import be.uantwerpen.fti.namingserver.controller.dto.NextAndPreviousDto;
import be.uantwerpen.fti.namingserver.controller.dto.RegisterNodeDto;
import be.uantwerpen.fti.namingserver.controller.dto.RemoveNodeDto;
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
        boolean success = hashService.registerNode(registerDto.getIpAddress(), registerDto.getHostname());
        return ResponseEntity.ok(success);
    }

    @DeleteMapping("/removeNode")
    public ResponseEntity<Boolean> removeNode(@RequestBody RemoveNodeDto removeDto) {
        log.info("The removal of node with hostname ({}) has been requested", removeDto.getCurrentHash());
        hashService.removeNode(removeDto.getCurrentHash());
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getNextAndPrevious/{hash}")
    public ResponseEntity<NextAndPreviousDto> getNextAndPrevious(@PathVariable int hash) {
        log.info("The next and previous node of node {} has been requested", hash);
        NextAndPreviousDto nextAndPrevious = hashService.getNextAndPrevious(hash);
        return ResponseEntity.ok(nextAndPrevious);
    }

    @GetMapping("/getPreviousNode/{hashValue}")
    public ResponseEntity<Integer> getPreviousNode(@PathVariable int hashValue) {
        log.info("Hash value of the previous node of node {} has been requested.", hashValue);
        int previousNode = hashService.getPrevious(hashValue);
        if (previousNode == 0) {
            log.info("No previous node found.");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(previousNode);
    }
}