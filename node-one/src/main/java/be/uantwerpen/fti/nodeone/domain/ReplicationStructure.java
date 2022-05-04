package be.uantwerpen.fti.nodeone.domain;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@RequiredArgsConstructor
@Component
public class ReplicationStructure {
    private Set<FileStructure> localFiles;
    private Set<FileStructure> replicatedFiles;
    private final ReplicationConfig replicationConfig;

    public void initialize() {
        localFiles = new TreeSet<>();
        replicatedFiles = new TreeSet<>();

        // TODO load json
        File dir = new File(replicationConfig.getLocal());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // add to files
                localFiles.add(new FileStructure(false, child.getPath())); // todo decide whether or not the file should be replicated
            }
        }

        dir = new File(replicationConfig.getReplica());
        directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // add to files
                replicatedFiles.add(new FileStructure(false, child.getPath())); // todo decide whether or not the file should be replicated
            }
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public class FileStructure implements Comparable<FileStructure> {
        private boolean replicated;
        private String path;

        @Override
        public int compareTo(FileStructure o) {
            return this.path.hashCode() - o.getPath().hashCode();
        }
    }
}
