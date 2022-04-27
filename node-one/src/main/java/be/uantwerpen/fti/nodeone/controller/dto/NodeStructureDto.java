package be.uantwerpen.fti.nodeone.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeStructureDto {
    private int previousNode; // contains a hash
    private int nextNode; // contains a hash
}
