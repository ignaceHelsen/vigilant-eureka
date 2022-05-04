package be.uantwerpen.fti.namingserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NextAndPreviousDto {
    int idNext;
    String ipNext;
    int idPrevious;
    String ipPrevious;
}
