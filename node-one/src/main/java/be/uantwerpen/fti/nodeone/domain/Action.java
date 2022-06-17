package be.uantwerpen.fti.nodeone.domain;

public enum Action {
    LOCAL(1),
    REPLICATE(2),
    LOCALTRANSFER(3);

    private int value;

    Action(int value) {
        this.value = value;
    }
}
