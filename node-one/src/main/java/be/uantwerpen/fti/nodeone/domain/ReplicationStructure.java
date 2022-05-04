package be.uantwerpen.fti.nodeone.domain;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@AllArgsConstructor
public class ReplicationStructure {
    Set<FileStructure> files;
    ReplicationConfig replicationConfig;

    public ReplicationStructure() {
        this.files = new TreeSet<>();

        // TODO load json
        File dir = new File(replicationConfig.getLocal());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // add to files
                files.add(new FileStructure(false, child.getPath())); // todo decide if file should be replicated
            }
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class FileStructure implements Comparable<FileStructure> {
        private boolean replicated;
        private String path;


        @Override
        public int compareTo(FileStructure o) {
            return this.path.hashCode() - o.getPath().hashCode();
        }
    }
}
