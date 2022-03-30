package be.uantwerpen.fti.namingserver.service;

import org.springframework.stereotype.Service;

@Service
public class HashService {
    private String ipAddresses[];

    public int calculateHash(String filename) {
        int hashcode = filename.hashCode();

        // e.g. 32104
        return 0;

    }

    public String getIp(String fileName) {
        int hashCode =  calculateHash(fileName);

        String ip = null;
        //String ip = map[hashCode];
        return ip;
    }
 }
