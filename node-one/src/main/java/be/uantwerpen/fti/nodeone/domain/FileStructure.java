package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@AllArgsConstructor
public class FileStructure implements Comparable<FileStructure> {
    private String path;
    private boolean replicated;
    private LogStructure logFile;

    public String getPath() {
        return path;
    }

    public boolean isReplicated() {
        return replicated;
    }

    public LogStructure getLogFile() {
        return logFile;
    }

    @Override
    public int compareTo(FileStructure o) {
        return this.path.hashCode() - o.getPath().hashCode();
    }
}