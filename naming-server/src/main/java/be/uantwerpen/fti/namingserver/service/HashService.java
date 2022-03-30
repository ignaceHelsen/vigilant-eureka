package be.uantwerpen.fti.namingserver.service;

import org.springframework.stereotype.Service;

@Service
public class HashService {
    private String ipAddresses[];

    public String calculateHash(String filename) {
        short hashcode = (short) filename.hashCode();

        // e.g. 32104
        return "aze";

    }
}
