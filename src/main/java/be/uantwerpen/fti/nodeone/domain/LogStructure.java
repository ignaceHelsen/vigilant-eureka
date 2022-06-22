package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class LogStructure {
    private String path;
    private Owner localFileOwner;
    private List<Owner> fileOwners;
    private List<Download> downloads;

    public LogStructure(String path) {
        this.path = path;
        this.fileOwners = new ArrayList<>();
        this.downloads = new ArrayList<>();
    }

    public void registerDownload(int hashValue){
        this.downloads.add(new Download(hashValue, LocalDateTime.now()));
    }

    public void registerOwner(int hashValue) {
        this.fileOwners.add(new Owner(hashValue, LocalDateTime.now()));
    }

    public Owner getOwner(int index) {
        return fileOwners.get(index);
    }

    public void setNewLocalFileOwner(int hashValue) {
        this.localFileOwner = new Owner(hashValue, LocalDateTime.now());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Owner {
        private int hashValue;
        private LocalDateTime timeStamp;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Download {
        private int hashValue;
        private LocalDateTime timeStamp;
    }
}
