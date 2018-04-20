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

    public LcdImageLine extractWrapped(int pixel, int length) {
        return new LcdImageLine(msb.extractWrapped(pixel, length),
                lsb.extractWrapped(pixel, length),
                opacity.extractWrapped(pixel, length));
    }

    public LcdImageLine mapColors(byte palette) {
        //TODO checker palette
        BitVector.Builder b = new BitVector.Builder(size());
        
        for (int i = 0; i < size() / Byte.SIZE; i++) {
            
            byte newColor = 0;
            
            for (int j = 0; j < Byte.SIZE; j++) {
                
                int substitustionColor = Bits.extract(palette,
                        valueAtIndex((i*Byte.SIZE + j) * 2), 2);
                
                int msbValue = substitustionColor/2;
                int lsbValue = substitustionColor%2;
            }
        }
    }

    private int valueAtIndex(int index) {
        return (msb.testBit(index) ? 1 : 0) * 2 + (lsb.testBit(index) ? 1 : 0);
    }
}
