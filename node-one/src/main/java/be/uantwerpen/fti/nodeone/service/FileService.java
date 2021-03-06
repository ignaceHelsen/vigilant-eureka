package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableScheduling
@AllArgsConstructor
public class FileService {
    private final ReplicationComponent replicationComponent;
    private final ReplicationConfig replicationConfig;

    public List<String> getAllLocalFiles() {
        return replicationComponent.getLocalFiles().stream().map(FileStructure::getPath).collect(Collectors.toList());
    }

    public List<String> getAllReplicatedFiles() {
        return replicationComponent.getReplicatedFiles().stream().map(FileStructure::getPath).collect(Collectors.toList());
    }

    /**
     * Check if all needed directories for replication are present.
     * Create when not present.
     * <p>
     * Directories to be checked:
     * <ul>
     *     <li>/storage</li>
     *     <ul>
     *         <li>/local</li>
     *         <li>/replica</li>
     *         <li>/log</li>
     *     </ul>
     * </ul>
     */
    public void precheck() {
        File localDir = new File(replicationConfig.getLocal());
        File replicaDir = new File(replicationConfig.getReplica());
        File logDir = new File(replicationConfig.getLog());

        try {
            boolean created = localDir.mkdirs();

            if (!created) log.warn("Did not create dir {}", localDir.getName());
        } catch (SecurityException e) {
            log.error("Unable to create storage/local directory. Security Exception");
        }


        try {
            boolean created = replicaDir.mkdirs();

            if (!created) log.warn("Did not create dir {}", replicaDir.getName());
        } catch (SecurityException e) {
            log.error("Unable to create storage/replica directory. Security Exception");
        }


        try {
            boolean created = logDir.mkdirs();

            if (!created) log.warn("Did not create dir {}", logDir.getName());
        } catch (SecurityException e) {
            log.error("Unable to create storage/log directory. Security Exception");
        }
    }

    public void deletefiles(List<FileStructure> files) throws SecurityException {
        // now delete the file
        files.forEach(f -> {
            try {
                // first delete the file, then its log
                log.info("Deleting {}.", f.getPath());
                boolean success = new File(f.getPath()).delete();
                if (success) log.info("Successfully deleted file {}.", f.getPath());

                success = new File(f.getLogFile().getPath()).delete();
                if (success) log.info("Successfully deleted file {}.", f.getPath());

                replicationComponent.getReplicatedFiles().remove(f);
            } catch (SecurityException e) {
                e.printStackTrace();
                log.warn("Unable to delete file after transfer. File: {}", f.getFileName());
                throw e;
            }
        });
    }

    public void saveFile(MultipartFile file, String path) throws IOException {
        byte[] bytes;
        bytes = file.getBytes();

        File outputFile = new File(path);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(bytes);
        }
    }

    public Resource getFile(String path) {
        Path file = Paths.get(path);
        return new FileSystemResource(file.toFile());
    }
}
