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

    public Owner getOwner(int index) {
        return fileOwners.get(index);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Owner {
        private int hashValue;
        private LocalDate timeStamp;

        public int getHashValue() {
            return hashValue;
        }

        public LocalDate getTimeStamp() {
            return timeStamp;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Download {
        private LocalDate timeStamp;
    }
}
