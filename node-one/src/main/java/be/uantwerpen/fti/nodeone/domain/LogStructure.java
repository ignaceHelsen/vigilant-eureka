package be.uantwerpen.fti.nodeone.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class LogStructure {
    private String path;
    private List<Owner> fileOwners;
    private List<Download> downloads;

    public LogStructure(String path) {
        this.path = path;
        this.fileOwners = new ArrayList<>();
        this.downloads = new ArrayList<>();
    }

    public void registerDownload(){
        this.downloads.add(new Download(LocalDate.now()));
    }

    public void registerOwner(int hashValue) {
        this.fileOwners.add(new Owner(hashValue, LocalDate.now()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class Owner {
        private int hashValue;
        private LocalDate timeStamp;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class Download {
        private LocalDate timeStamp;
    }
}
