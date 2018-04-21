package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {

    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;
    
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && msb.size() == opacity.size());

        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    public final class Builder {

    }

    public int size() {
        return msb.size();
    }

    public BitVector msb() {
        return msb;
    }

    public BitVector lsb() {
        return lsb;
    }

    public BitVector opacity() {
        return opacity;
    }

    public LcdImageLine shift(int pixels) {
        return new LcdImageLine(msb.shift(pixels), lsb.shift(pixels),
                opacity.shift(pixels));
    }

    // TODO check length multiple de 32
    public LcdImageLine extractWrapped(int pixel, int length) {
        return new LcdImageLine(msb.extractWrapped(pixel, length),
                lsb.extractWrapped(pixel, length),
                opacity.extractWrapped(pixel, length));
    }

    //TODO ou preconditions check8
    public LcdImageLine mapColors(byte palette) {
        if (palette == 0b11_10_01_00) {
            return this;
        }

        if(palette)
    }

    // TODO a effacer potentiellement
    private int valueAtIndex(int index) {
        return (msb.testBit(index) ? 1 : 0) * 2 + (lsb.testBit(index) ? 1 : 0);
    }

    private BitVector identifyBitsOfColor(int color) {
        Preconditions.checkArgument(color < 4 || color >=0);
        switch (color) {
        case 0b00:
            return msb.or(lsb).not();
        case 0b01:
            return lsb.and(msb.not());
        case 0b10:
            return msb.and(lsb.not());
        case 0b11:
            return msb.and(lsb);
        default:
            throw new Error("incorret color");
        }
        
    }
    
    private BitVector operateBitChange(BitVector vec, BitVector change) {
        
    }
}
