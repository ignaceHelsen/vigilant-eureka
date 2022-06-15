package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class ReplicatedFile {
    private String filename;
    private MultipartFile file;
    private MultipartFile logFile;
}
