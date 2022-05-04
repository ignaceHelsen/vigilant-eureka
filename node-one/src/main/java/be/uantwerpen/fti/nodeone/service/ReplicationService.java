package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.domain.NextAndPreviousNode;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplicationService {
    private final NodeStructure nodeStructure;
    private final RestService restService;


    public void Shutdown() {
        // Find previous node -> Naming server
        int previousNode = nodeStructure.getPreviousNode();//restService.getPreviousNode(nodeStructure.getCurrentHash());
            //replicate(?, restService.requestNodeIpWithHashValue(previousNode)));
        // Edge case -> Previous previous
        int secondPreviousNode = restService.getPreviousNode(nodeStructure.getPreviousNode());
            //replicate.(?,  restService.requestNodeIpWithHashValue(secondPreviousNode));
        // Send all replicated files
        // Except not downloaded files


        // Send log file

        // Send owner of own locals files warning

    }
}
