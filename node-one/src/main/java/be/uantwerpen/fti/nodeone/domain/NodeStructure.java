package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
public class NodeStructure {
    private int previousNode; // contains a hash
    private int currentHash; // contains a hash
    private int nextNode; // contains a hash
    int next;
    int previous;
}
