package be.uantwerpen.fti.namingserver.service;

import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.NavigableMap;
import java.util.TreeMap;

@Service
public class HashService {
    private NavigableMap<Integer, String> nodes;
    private final Environment env;

    public HashService(Environment env) {
        nodes = new TreeMap<>();
        this.env = env;
    }

    public int calculateHash(String filename) {
        return (filename.hashCode() + Integer.MAX_VALUE) * (32768 / (Short.MAX_VALUE + Math.abs(Integer.MIN_VALUE)));
    }

    public String registerFile(String filename) {
        int hash = calculateHash(filename); // e.g. 18
        // now decide where to register the file

        //TODO NOW SAVE TO HASHMAP
        int keyOfClosestAndLowerNode = nodes.floorKey(hash);
        return nodes.get(keyOfClosestAndLowerNode); // dns  name
    }
}
