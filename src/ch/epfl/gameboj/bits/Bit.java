package ch.epfl.gameboj.bits;

public interface Bit {
    
    //TODO public, private pour ces méthodes ?
    int ordinal();
    
    default int index() {
        return ordinal();
    }
    
    //TODO juste ?
    default int mask() {
        return Bits.mask(ordinal());
    }
}
