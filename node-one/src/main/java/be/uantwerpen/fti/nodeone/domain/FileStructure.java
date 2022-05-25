package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class FileStructure implements Comparable<FileStructure> {
    private String path;
    private String filename;
    private boolean activeLock;
    private boolean replicated;

    @Override
    public int compareTo(FileStructure o) {
        return this.path.hashCode() - o.getPath().hashCode();
    }
}
