package be.uantwerpen.fti.namingserver.service;

import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.Map.Entry;

@Service
public class HashService {
    private NavigableMap<Integer, String> nodes;

    public HashService() {
        nodes = new TreeMap<>();
    }

    public int calculateHash(String filename) {
        return (filename.hashCode() + Integer.MAX_VALUE) * (Short.MAX_VALUE / (Integer.MAX_VALUE + Math.abs(Integer.MIN_VALUE)));
    }

    public String registerFile(String filename) {
        int hash = calculateHash(filename); // e.g. 18
        // now decide where to register the file

        // TODO NOW SAVE TO HASHMAP
        // check if lowest hash of the nodes is higher than the calculated hash
        int lowestKey = nodes.firstKey();
        if (lowestKey > hash) {
            return nodes.get(nodes.lastKey()); // return the highest node
        }

        int keyOfClosestAndLowerNode = nodes.floorKey(hash);
        return nodes.get(keyOfClosestAndLowerNode); // dns  name
    }
}
