package be.uantwerpen.fti.nodeone.component;

import be.uantwerpen.fti.nodeone.config.ReplicationConfig;
import be.uantwerpen.fti.nodeone.domain.FileStructure;
import be.uantwerpen.fti.nodeone.domain.LogStructure;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private Set<FileStructure> replicatedLocalFiles;
    private Set<FileStructure> replicatedFiles;
    private final ReplicationConfig replicationConfig;
    private final Gson gson;
    private final NodeStructure nodeStructure;

    public void initialize() {
        localFiles = new TreeSet<>();
        replicatedFiles = new TreeSet<>();

        File file = new File(replicationConfig.getStorage());

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            replicatedLocalFiles = gson.fromJson(new FileReader(file), new TypeToken<TreeSet<FileStructure>>(){}.getType());
        } catch (FileNotFoundException e) {
            replicatedLocalFiles = new TreeSet<>();
        }

        lookForNewFiles();
    }

    /**
     * Will look for files in the /storage directory. /local files will be added to internal list while /replica files are added to another internal list. Gitkeeps will be ignored.
     * Files that have not been found in the config json are added and regarded as NOT yet replicated.
     */
    public void lookForNewFiles() {
        log.info("Loading replication structure");
        File dir = new File(replicationConfig.getLocal());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // ignore gitkeeps
            List<String> replicatedLocalPaths = localFiles.stream().map(FileStructure::getPath).collect(Collectors.toList());
            for (File child : Arrays.stream(directoryListing).filter(f -> !(f.getName().equalsIgnoreCase(".gitkeep")) && replicatedLocalPaths.stream().noneMatch(path -> path.equals(f.getPath()))).collect(Collectors.toList())) {
                // add to files (t's a set so no duplicates)
                // load josn containing list of all files that have been replicated
                // if current child is foundin this list, set replicated to true. otherwise set to false
                FileStructure fileStructure = new FileStructure(child.getPath(), child.getName(), false, new LogStructure(createLogPath(child.getName())));
                localFiles.add(fileStructure); // As new files are being found, these are of course not replicated yet so we set the boolean to false
                fileStructure.getLogFile().registerOwner(nodeStructure.getCurrentHash());
                fileStructure.getLogFile().setNewLocalFileOwner(nodeStructure.getCurrentHash());
                saveLog(fileStructure.getLogFile(), child.getName());
            }
        }
    }

    public String createLogPath(String fileName) {
        return String.format("%s/%s.json", getReplicationConfig().getLog(), fileName);
    }

    public String createFilePath(String fileName) {
        return String.format("%s/%s", replicationConfig.getReplica(), fileName);
    }

    public void addLocalFile(FileStructure fileStructure) {
        this.localFiles.add(fileStructure);
    }

    public void addReplicationFile(FileStructure fileStructure)  {
        this.replicatedFiles.add(fileStructure);
    }

    public void addReplicatedLocalFile(FileStructure fileStructure) {
        this.replicatedLocalFiles.add(fileStructure);
    }

    public void addReplicatedFile(FileStructure fileStructure) {
        this.replicatedFiles.add(fileStructure);
    }

    public Optional<LogStructure> loadLog(String fileName) {
        try  {
            Reader reader = Files.newBufferedReader(Paths.get(createLogPath(fileName)));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            LogStructure logStructure = gson.fromJson(reader, LogStructure.class);
            return Optional.of(logStructure);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void saveLog(LogStructure logStructure, String fileName) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(logStructure);
            String logPath = createLogPath(fileName);
            Path path = Paths.get(logPath);
            File file = new File(logPath);

            if (!file.exists()) {
                file.createNewFile();
            }

            Files.write(path, json.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
