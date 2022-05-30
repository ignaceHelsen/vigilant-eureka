package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.config.MapConfig;
import be.uantwerpen.fti.namingserver.controller.dto.NextAndPreviousDto;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Service
@Slf4j
public class HashService {
    private NavigableMap<Integer, String> nodes;
    private final MapConfig mapConfig;
    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    public HashService(MapConfig mapConfig) {
        this.mapConfig = mapConfig;
        nodes = new TreeMap<>();
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        readMapFromFile();
    }

    public int calculateHash(String filename) {
        return (int) (((long) filename.hashCode() + (long) Integer.MAX_VALUE) * ((double) Short.MAX_VALUE / (2 * (double) Integer.MAX_VALUE)));
    }

    public String registerFile(String filename) {
        try {
            readLock.lock();
            int hash = calculateHash(filename); // e.g. 18
            // now decide where to register the file

            // check if lowest hash of the nodes is higher than the calculated hash
            if (nodes.isEmpty()) return null;

            int lowestKey = nodes.firstKey();
            if (lowestKey > hash) {
                return nodes.get(nodes.lastKey()); // return the highest node
            }

            int keyOfClosestAndLowerNode = nodes.floorKey(hash);
            return nodes.get(keyOfClosestAndLowerNode); // dns  name
        } finally {
            readLock.lock();
        }
    }

    public boolean registerNode(String ipAddress, String hostname) {
        try {
            writeLock.lock();
            int hash = calculateHash(hostname);
            if (nodes.containsKey(hash)) {
                log.info("Node with hostname ({}) and ip address ({}) is already in the map", hostname, ipAddress);
                return false;
            }

            nodes.put(calculateHash(hostname), ipAddress);
            updateMap();
            log.info("Node with hostname ({}) and ip address ({}) has been added to the map", hostname, ipAddress);

            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public void removeNode(int currentHash) {
        try {
            writeLock.lock();
            nodes.remove(currentHash);
            updateMap();
            log.info("Node with hostname ({}) has been removed from the map", currentHash);
        } finally {
            writeLock.lock();
        }


    }

    private void readMapFromFile() {
        try {
            writeLock.lock();
            BufferedReader reader = new BufferedReader(new FileReader(mapConfig.getFilename()));
            Gson gson = new Gson();
            var map = gson.fromJson(reader, TreeMap.class);
            nodes = new TreeMap<>();

            if (map != null) {
                Object[] keys = map.keySet().toArray();
                Object[] values = map.values().toArray();
                for (int i = 0; i < keys.length; i++) {
                    nodes.put(Integer.parseInt((String) keys[i]), (String) values[i]);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            nodes = new TreeMap<>();
            log.error("Could not find nodes.json file.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }

    }

    private void updateMap() {
        try {
            writeLock.lock();
            Gson gson = new Gson();
            String json = gson.toJson(nodes);
            try {
                Path path = Paths.get(mapConfig.getFilename());
                File file = new File(mapConfig.getFilename());

                if (!file.exists()) {
                    file.createNewFile();
                }

                Files.write(path, json.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchFileException e) {
                log.error("Could not find nodes.json.");
            } catch (IOException e) {
                log.error("Unable to create or find nodes.json.");
            }
        } finally {
            writeLock.unlock();
        }

    }

    public String getAddressWithKey(int key) {
        try {
            readLock.lock();
            return nodes.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public int getNext(int currentHash) {
        try {
            readLock.lock();
            int highestKey = nodes.lastKey();
            if (highestKey <= currentHash) {
                return nodes.firstKey(); // return the lowest node
            }
            return nodes.higherKey(currentHash);
        } catch (NullPointerException | NoSuchElementException e) {
            return currentHash;
        } finally {
            readLock.unlock();
        }
    }

    public int getPrevious(int currentHash) {
        try {
            readLock.lock();
            int lowestKey = nodes.firstKey();
            if (lowestKey >= currentHash) {
                return nodes.lastKey(); // return the highest node
            }
            return nodes.lowerKey(currentHash);
        } catch (NullPointerException | NoSuchElementException e) {
            return currentHash;
        } finally {
            readLock.unlock();
        }
    }

    public NextAndPreviousDto getNextAndPrevious(int currentHash) {
        try {
            readLock.lock();
            int idNext = getNext(currentHash);
            int idPrevious = getPrevious(currentHash);
            return new NextAndPreviousDto(idNext, getAddressWithKey(idNext), idPrevious, getAddressWithKey(idPrevious));
        } finally {
            readLock.unlock();
        }
    }

    public int mapSize() {
        try {
            readLock.lock();
            return nodes.size();
        } finally {
            readLock.unlock();
        }
    }

    public String getReplicationNode(int hash, int sourceNode) {
        try {
            readLock.lock();
            // we can't call registerFile to know if the sourcenode is the original holder of the file since files could have been stored to a node directly without passing the namingserver
            // first check if node is the only node, we don't want to replicate to the same node where the file is locally stored
            if (nodes.size() == 1) return null;

            // check if the hash is lower than any node we have, if so, return the highest node
            try {
                if (hash < nodes.firstKey() && nodes.lastKey() != sourceNode) {
                    return nodes.lastEntry().getValue();
                }
            } catch (NoSuchElementException e) {
                log.warn("No nodes found, it's possible that all nodes shut down during the last second.");
            }

            int node = nodes.lowerKey(hash);

            // check if the replication node is the same as the source node's hash, if so, return the previous after that
            if (node == sourceNode) {
                Integer previousNodeKey = nodes.lowerKey(node);
                if (previousNodeKey == null) {
                    // if there isn't any previous node, return highest
                    // but also check that this highest is not the same as the sourcenode as well
                    Map.Entry<Integer, String> highestEntry = nodes.lastEntry();
                    if (highestEntry.getKey() == sourceNode) return null;
                    return highestEntry.getValue();
                }
                return nodes.get(previousNodeKey);
            }

            return nodes.get(node);
        } catch (NullPointerException e) {
            log.error("Unable to find node destination for replication.");
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public Map<Integer, String> getAllNodes() {
        try {
            readLock.lock();
            return nodes;
        } finally {
            readLock.unlock();
        }
    }
}
