package be.uantwerpen.fti.namingserver.service;

import be.uantwerpen.fti.namingserver.HashConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
public class HashService {
    private NavigableMap<Integer, String> nodes;
    private final  HashConfig hashConfig;

    public HashService(HashConfig hashConfig) {
        this.hashConfig = hashConfig;
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
        nodes.put(hostname.hashCode(), hostname); //ipAddress is depricated. (Werkt nog, maar is niet meer nodig)
        updateMap();
    }

    private void readMapFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(hashConfig.getFilename()));
            Gson gson = new Gson();
            nodes = gson.fromJson(reader, new TypeToken<TreeMap<Integer, String>>() {}.getType());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateMap() {
        Gson gson = new Gson();
        String json = gson.toJson(nodes);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(hashConfig.getFilename()));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

