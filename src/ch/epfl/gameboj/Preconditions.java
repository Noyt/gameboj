package ch.epfl.gameboj;

import java.lang.IllegalArgumentException;

public interface Preconditions {
    
    static void checkArgument(boolean b) throws IllegalArgumentException {
        if(!b) {
            throw new IllegalArgumentException();
        }
    }
    
    // TODO "throws IllegalArgument" ?????
    static int checkBits8(int v) throws IllegalArgumentException {
        
        if (v < 0 || v > 0xFF) {
            throw new IllegalArgumentException();
        }
        
        return v;
    }
    
    static int checkBits16(int v) throws IllegalArgumentException {
        
        if (v < 0 || v > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        
        return v;
    }
    
    
}
