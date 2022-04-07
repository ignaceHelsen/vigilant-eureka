package be.uantwerpen.fti.namingserver.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterNodeDto {
    private String ipAddress;
    private String hostname;
}
