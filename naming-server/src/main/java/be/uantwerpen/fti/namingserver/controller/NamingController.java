package be.uantwerpen.fti.namingserver.controller;

import be.uantwerpen.fti.namingserver.controller.dto.NextAndPreviousDto;
import be.uantwerpen.fti.namingserver.controller.dto.RegisterNodeDto;
import be.uantwerpen.fti.namingserver.controller.dto.RemoveNodeDto;
import be.uantwerpen.fti.namingserver.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/naming")
//@ServerEndpoint(value = "/nodes")
public class NamingController {
    private final HashService hashService;
    //private Session session;

    /*@OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("Error in connection with websocket.");
    }*/

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

    @GetMapping("/address/{hash}")
    public ResponseEntity<String> getAddressByHash(@PathVariable int hash) {
        log.info("Ip address of node with hash ({}) has been requested", hash);
        String destination = hashService.getAddressWithKey(hash);
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
        //broadcast();
        return ResponseEntity.ok(success);
    }

    @DeleteMapping("/removeNode")
    public ResponseEntity<Boolean> removeNode(@RequestBody RemoveNodeDto removeDto) {
        log.info("The removal of node with hostname ({}) has been requested", removeDto.getCurrentHash());
        hashService.removeNode(removeDto.getCurrentHash());
        //broadcast();
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getNextAndPrevious/{hash}")
    public ResponseEntity<NextAndPreviousDto> getNextAndPrevious(@PathVariable int hash) {
        log.info("The next and previous node of node {} has been requested", hash);
        NextAndPreviousDto nextAndPrevious = hashService.getNextAndPrevious(hash);
        return ResponseEntity.ok(nextAndPrevious);
    }

    /**
     * Will search for a node that will store the filehash.
     *
     * @param hash:       The hash of the filename.
     * @param sourceNode: The hash of the address of the requesting node.
     * @return String representing the address of the destination node.
     */
    @GetMapping("/replicationDestination/{hash}/{sourceNode}")
    public ResponseEntity<String> getReplicationDestination(@PathVariable int hash, @PathVariable int sourceNode) {
        log.info("Request received for file replication (hash: {})", hash);
        String destination = hashService.getReplicationNode(hash, sourceNode);
        if (destination == null) {
            log.info("No node found.");
            return new ResponseEntity<>("No node found.", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(destination);
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

    @GetMapping("/nodes/all")
    public ResponseEntity<Map<Integer, String>> getAllNodes() {
        return ResponseEntity.ok(hashService.getAllNodes());
    }

    private void broadcast() {
        /*try {
            session.getBasicRemote().sendText("update");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}