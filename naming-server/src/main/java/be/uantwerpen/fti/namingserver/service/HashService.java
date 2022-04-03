package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.MapConfig;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
@Slf4j
public class HashService {
    private NavigableMap<Integer, String> nodes;
    private final MapConfig mapConfig;

    public HashService(MapConfig mapConfig) {
        this.mapConfig = mapConfig;
        nodes = new TreeMap<>();
        readMapFromFile();
    }

    public int calculateHash(String filename) {
        return (filename.hashCode() + Integer.MAX_VALUE) * (Short.MAX_VALUE / (Integer.MAX_VALUE + Math.abs(Integer.MIN_VALUE)));
    }

    public String registerFile(String filename) {
        int hash = calculateHash(filename); // e.g. 18
        // now decide where to register the file

        // check if lowest hash of the nodes is higher than the calculated hash
        int lowestKey = nodes.firstKey();
        if (lowestKey > hash) {
            return nodes.get(nodes.lastKey()); // return the highest node
        }

        int keyOfClosestAndLowerNode = nodes.floorKey(hash);
        return nodes.get(keyOfClosestAndLowerNode); // dns  name
    }

    public void registerNode(String ipAddress, String hostname) {
        nodes.put(Math.abs((short) hostname.hashCode()), hostname); //ipAddress is deprecated. (Werkt nog, maar is niet meer nodig)
        updateMap();
    }

    private void readMapFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mapConfig.getFilename()));
            Gson gson = new Gson();
            var map = gson.fromJson(reader, TreeMap.class);
            if (map == null) {
                nodes = new TreeMap<>();
            } else {
                nodes = map;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            nodes = new TreeMap<>();
            log.error("Could not find nodes.json file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateMap() {
        Gson gson = new Gson();
        String json = gson.toJson(nodes);
        try {
            Path path = Paths.get(mapConfig.getFilename());
            File file = new File(mapConfig.getFilename());

            if (!file.exists()) {
                file.createNewFile();
            }

            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
        } catch(NoSuchFileException e) {
            log.error("Could not find nodes.json.");
        }
        catch (IOException e) {
            log.error("Unable to create or find nodes.json.");
        }
    }
}
