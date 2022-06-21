package be.uantwerpen.fti.nodeone.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NextAndPreviousNode {
    int idNext;
    String ipNext;
    int idPrevious;
    String ipPrevious;

    public NextAndPreviousNode() {
    }
}
