package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class NodeStructure {
    private int previousNode; // contains a hash
    private int currentHash; // contains a hash
    private int nextNode; // contains a hash
}
