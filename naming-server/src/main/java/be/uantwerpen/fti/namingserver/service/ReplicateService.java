package be.uantwerpen.fti.namingserver.service;


import be.uantwerpen.fti.namingserver.config.NetworkConfig;
import be.uantwerpen.fti.namingserver.controller.NamingController;
import lombok.AllArgsConstructor;

import java.net.MulticastSocket;

@AllArgsConstructor
public class ReplicateService {
    private HashService hashService;

    public String getNodeLocation(String filename) {

        // calculate hash
        int fileHash = hashService.calculateHash(filename);
        String nodeAddress = hashService.getNodeFromHash(fileHash);

        return nodeAddress;
    }
}