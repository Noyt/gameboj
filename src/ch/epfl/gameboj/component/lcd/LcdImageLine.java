package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Alu;

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

    // TODO ou preconditions check8
    // vraiment un byte ?
    public LcdImageLine mapColors(byte palette) {
        // TODO cette valeur 0b est elle bien un byte ? le programme va t il
        // vraiment passer par là ?
        if (palette == 0b11_10_01_00)
            return this;

        BitVector finalMsb = msb;
        BitVector finalLsb = lsb;

        for (int oldColor = 0; oldColor < Byte.SIZE / 2; oldColor++) {
            int newColor = Bits.extract(palette, oldColor * 2, 2);
            if (Bits.test(oldColor ^ newColor, 1)) {
                finalMsb = bitChange(finalMsb, identifyBitsOfColor(oldColor));
            }
            if (Bits.test(oldColor ^ newColor, 0)) {
                finalLsb = bitChange(finalLsb, identifyBitsOfColor(oldColor));
            }
        }
        return new LcdImageLine(finalMsb, finalLsb, this.opacity);
    }
    
    public LcdImageLine below(LcdImageLine that, BitVector opacity) {
        Preconditions.checkArgument(this.size() == that.size());
        BitVector finalMsb = null;
        BitVector finalLsb = null;
        
        finalMsb = this.msb.or(that.msb.and(opacity)).and(that.msb.not().and(opacity).not());
        finalLsb = this.lsb.or(that.lsb.and(opacity)).and(that.lsb.not().and(opacity).not());
        
        //TODO que mettre pour l'opacité de cette nouvelle ligne ? tout opaque ?
        return new LcdImageLine(finalMsb, finalLsb, new BitVector(this.size(), true));
    }
    
    public LcdImageLine below(LcdImageLine that) {
        Preconditions.checkArgument(this.size() == that.size());
        return below(that, that.opacity);
    }

    private BitVector identifyBitsOfColor(int color) {
        Preconditions.checkArgument(color < 4 || color >= 0);
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

    private BitVector bitChange(BitVector vec, BitVector change) {
        return vec.xor(change);
    }
}
