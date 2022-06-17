package be.uantwerpen.fti.nodeone.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NodeStructureDto {
    private int previousNode; // contains a hash
    private int nextNode; // contains a hash
}
