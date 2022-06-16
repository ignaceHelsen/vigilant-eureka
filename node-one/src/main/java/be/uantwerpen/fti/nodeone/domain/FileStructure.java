package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
public class FileStructure implements Comparable<FileStructure> {
    private String path;
    private String fileName;
    private boolean replicated;
    private LogStructure logFile;

    @Override
    public int compareTo(FileStructure o) {
        return this.path.hashCode() - o.getPath().hashCode();
    }
}