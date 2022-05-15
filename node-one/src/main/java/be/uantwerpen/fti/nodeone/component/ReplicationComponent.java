package be.uantwerpen.fti.nodeone.component;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
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
/**
 * This component will take care of the storage of local and replication files
 */
public class ReplicationComponent {
    private Set<FileStructure> localFiles;
    private Set<FileStructure> replicatedFiles;
    private final ReplicationConfig replicationConfig;

    public void initialize() {
        localFiles = new TreeSet<>();
        replicatedFiles = new TreeSet<>();

        lookForNewFiles();
    }

    /**
     * Will look for files in the /storage directory. /local files will be added to internal list while /replica files are added to another internal list. Gitkeeps will be ignored.
     * Files that have not been found in the config json are added and regarded as NOT yet replicated.
     */
    public void lookForNewFiles() {
        // TODO load json
        log.info("Loading replication structure");
        File dir = new File(replicationConfig.getLocal());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().equalsIgnoreCase(".gitkeep"))).collect(Collectors.toList())) {
                // add to files (it's a set so no duplicates)
                localFiles.add(new FileStructure(child.getPath(), false)); // As new files are being found, these are of course not replicated yet so we set the boolean to false
            }
        }

        dir = new File(replicationConfig.getReplica());
        directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().equalsIgnoreCase(".gitkeep"))).collect(Collectors.toList())) {
                // add to files
                replicatedFiles.add(new FileStructure( child.getPath(), false));
            }
        }
    }

    public void addLocalFile(FileStructure fileStructure) {
        this.localFiles.add(fileStructure);
    }

    public void addReplicationFile(FileStructure fileStructure)  {
        this.replicatedFiles.add(fileStructure);
    }
}
