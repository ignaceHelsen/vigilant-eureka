package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RemoveNodeRequest {
    String hostname;
}
