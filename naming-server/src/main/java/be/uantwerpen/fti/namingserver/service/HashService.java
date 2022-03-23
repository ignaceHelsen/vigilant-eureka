package be.uantwerpen.fti.namingserver.service;

public class HashService {
    private String ipAddresses[];

    public String calculateHash(String filename) {
        short hashcode = (short) filename.hashCode();

        // e.g. 32104


    }
}
