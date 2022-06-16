package be.uantwerpen.fti.nodeone.controller.dto;

import be.uantwerpen.fti.nodeone.domain.ReplicatedFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TransferReplicatedDto {
    MultipartFile replicateFiles;
    //List<ReplicatedFile> replicatedFiles;
}
