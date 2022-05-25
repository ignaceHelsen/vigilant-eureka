package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableScheduling
@AllArgsConstructor
public class FileService {
    private final ReplicationComponent replicationComponent;

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
        File localDir = new File("src/resources/storage/local");
        File replicaDir = new File("src/resources/storage/replica");
        File logDir = new File("src/resources/storage/log");

        if (!localDir.exists()) {
            log.info("Created local directory.");
            try {
                boolean created = localDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", localDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/local directory. Security Exception");
            }
        }

        if (!replicaDir.exists()) {
            log.info("Created replica directory.");
            try {
                boolean created = replicaDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", replicaDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/replica directory. Security Exception");
            }
        }

        if (!logDir.exists()) {
            log.info("Created log directory.");
            try {
                boolean created = logDir.mkdirs();

                if (!created) log.warn("Failure while creating dir {}", logDir.getName());
            } catch (SecurityException e) {
                log.error("Unable to create storage/log directory. Security Exception");
            }
        }
    }

    public void deletefiles(List<File> files) throws SecurityException {
        // now delete the file
        files.forEach(f -> {
            try {
                f.delete();
            } catch (SecurityException e) {
                e.printStackTrace();
                log.warn("Unable to delete file after transfer. File: {}", f.getName());
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
}
