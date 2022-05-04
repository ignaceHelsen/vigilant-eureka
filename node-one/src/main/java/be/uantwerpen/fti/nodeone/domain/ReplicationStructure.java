package be.uantwerpen.fti.nodeone.domain;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
@Component
@Slf4j
public class ReplicationStructure {
    private Set<FileStructure> localFiles;
    private Set<FileStructure> replicatedFiles;
    private final ReplicationConfig replicationConfig;

    public void initialize() {
        localFiles = new TreeSet<>();
        replicatedFiles = new TreeSet<>();

        // TODO load json
        log.info("Loading replication structure");
        File dir = new File(replicationConfig.getLocal());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().contains("gitkeep"))).collect(Collectors.toList())) {
                // add to files
                localFiles.add(new FileStructure(false, child.getPath())); // todo decide whether or not the file should be replicated
            }
        }

        dir = new File(replicationConfig.getReplica());
        directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().contains("gitkeep"))).collect(Collectors.toList())) {
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
