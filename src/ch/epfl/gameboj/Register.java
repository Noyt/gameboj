package ch.epfl.gameboj;

public interface Register {

    int ordinal();
    
    public default int index() {
        return ordinal();
    }
}
