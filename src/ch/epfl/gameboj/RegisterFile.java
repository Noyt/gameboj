package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {

    private int[] file;
    
    public RegisterFile(E[] allRegs) {
        //banc = Arrays.copyOf(allRegs, allRegs.length);
        file = new int[allRegs.length];
    }
    
    public int get(E reg) {
        return file[reg.index()];
    }
    
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        
        file[reg.index()] = newValue; 
    }
    
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    public void setBit(E reg, Bit bit, boolean newValue) {
        Bits.set(get(reg), bit.index(), newValue);
    }
}
