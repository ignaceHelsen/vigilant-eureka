package be.uantwerpen.fti.nodeone.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class ReplicatedFileDto {
    private String filename;
    private Resource file;
    private Resource logFile;
}
