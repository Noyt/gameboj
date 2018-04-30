package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * A single line of an image
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 * 
 */
public final class LcdImageLine {

    // TODO immuable, donc mettre des finals pour les attributs ?
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;

    // TODO methode public le constructeur?
    /**
     * Builds an LcdImageLine, all three BitVector given as parameters must have
     * the same size
     * 
     * @param msb
     *            vector for the most significant bits
     * @param lsb
     *            vector for the least significant bits
     * @param opacity
     *            vector the opacity
     * @throws IllegalArgumentException
     *             if vectors do not all have the same size
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && msb.size() == opacity.size());

        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * Builder pattern
     * 
     * @author Arnaud Robert (287964)
     * @author Sophie Du Couedic (260007)
     * 
     */
    public static final class Builder {

        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;

        public Builder(int size) {
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
        }

        public Builder setBytes(int index, int msbByte, int lsbByte) {
            msbBuilder.setByte(index, msbByte);
            lsbBuilder.setByte(index, lsbByte);

            return this;
        }

        public LcdImageLine build() {
            BitVector finalMsb = msbBuilder.build();
            BitVector finalLsb = lsbBuilder.build();
            BitVector finalOpacity = finalMsb.or(finalLsb);

            return new LcdImageLine(finalMsb, finalLsb, finalOpacity);
        }
    }

    /**
     * Getter for the size of the line
     * 
     * @return the size of one of its vectors
     */
    public int size() {
        return msb.size();
    }

    /**
     * Getter for the msb
     * 
     * @return BitVector msb
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Getter for the lsb
     * 
     * @return BitVector lsb
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Getter for the opacity
     * 
     * @return BitVector opacity
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Shifts the line by a given number of pixels while preserving its length
     * 
     * @param pixels,
     *            number of pixels to shift by
     * @return the shifted line
     */
    // TODO pas de restriction au niveau de pixels ? pas de precondition à
    // vérifier ?
    public LcdImageLine shift(int pixels) {
        return new LcdImageLine(msb.shift(pixels), lsb.shift(pixels),
                opacity.shift(pixels));
    }

    /**
     * Starting from a given pixel, extract a line of a given length from the
     * infinite wrapped extension
     * 
     * @param pixel
     *            the pixel from which to start the extraction
     * @param length
     *            the length of the line to extract
     * @throws IllegalArgumentException
     *             if length is not divisible by 32
     * @return the extracted line
     */
    // TODO pas de restriction au niveau de pixels ? pas de precondition à
    // vérifier ?
    public LcdImageLine extractWrapped(int pixel, int length) {
        Preconditions.checkArgument(is32Multiple(length));
        return new LcdImageLine(msb.extractWrapped(pixel, length),
                lsb.extractWrapped(pixel, length),
                opacity.extractWrapped(pixel, length));
    }

    // TODO ou preconditions check8
    // vraiment un byte ? je pense qu'il faut faire precondition check8, du coup
    // mettre un throws dans la javadoc ?
    /**
     * Transforms the colors of a line according to a "palette" When the palette
     * doesn't actually require any changes in terms of colors, the line is
     * directly returned as such and no calculations are made. Since this case
     * is particularly common, this shortcut allows the program to save a lot of
     * time and resources
     * 
     * @param palette
     *            a byte encoding the color changes to be done
     * @return the color adjusted line
     */
    public LcdImageLine mapColors(byte palette) {
        // TODO cette valeur 0b est elle bien un byte ? le programme va t il
        // vraiment passer par là ?
        if (palette == (byte) 0b11_10_01_00)
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

    /**
     * Creates a line from two lines of equal lengths by superposing them. The
     * opacity vector given in parameter determines which pixels are conserved
     * in the final line. For a particular pixel, if the corresponding bit in
     * the opacity vector is equal to 1, then the pixel takes the value of the
     * line above. If it is equal to 0, the pixel takes the value of the pixel
     * from the line below.
     * 
     * @param that
     *            the line above, whose pixels are conserved if they are opaque
     * @param opacity
     *            the vector which determines the source of the pixels for the
     *            resulting line
     * @throws IllegalArgumentException
     *             if the given line and the given opacity vector are not the
     *             same size as the below line on which the method is called
     * @return the line resulting from the composition
     */
    public LcdImageLine below(LcdImageLine that, BitVector opacity) {
        Preconditions.checkArgument(
                this.size() == that.size() && opacity.size() == this.size());
        BitVector finalMsb = null;
        BitVector finalLsb = null;
        BitVector finalOpacity = null;

        finalMsb = this.msb.or(that.msb.and(opacity))
                .and(that.msb.not().and(opacity).not());
        finalLsb = this.lsb.or(that.lsb.and(opacity))
                .and(that.lsb.not().and(opacity).not());
        finalOpacity = this.opacity.or(opacity);

        return new LcdImageLine(finalMsb, finalLsb, finalOpacity);
    }

    /**
     * Creates a line from two lines of equal lengths by superposing them. The
     * resulting line's pixels are those of the above line, if these were
     * opaque, and those of the line below if they were not.
     * 
     * @param that
     *            the line above, whose pixels are conserved if they are opaque
     * @throws IllegalArgumentException
     *             if the given line is not the same size as the line below
     * @return the line resulting from the composition
     */
    public LcdImageLine below(LcdImageLine that) {
        Preconditions.checkArgument(size() == that.size());
        return below(that, that.opacity);
    }

    /**
     * Creates a line from two lines of equal lengths. The first part of the
     * line is made of the pixels from the current line while the second part of
     * the line is made of the pixels from the line given in parameter. The
     * parameter pixel indicates how many pixels form the current line are to be
     * preserved (i.e, the first "pixel" pixels are those of the current line
     * and the rest are those of the line in parameter)
     * 
     * @param that
     *            the line to join with
     * @param pixel
     *            the number of pixels to preserve from the current line
     * @throws IllegalArgumentException
     *             if the two lines to join do not have the same length or if
     *             pixel is bigger than the size of the line
     * @return the line resulting from the composition
     */
    public LcdImageLine join(LcdImageLine that, int pixel) {
        Preconditions.checkArgument(size() == that.size() && pixel <= size());
        Objects.checkIndex(pixel, size());
        BitVector finalMsb = msb.shift(size() - pixel).shift(pixel - size())
                .or(that.msb.shift(-pixel).shift(pixel));
        BitVector finalLsb = lsb.shift(size() - pixel).shift(pixel - size())
                .or(that.lsb.shift(-pixel).shift(pixel));
        BitVector finalOpacity = opacity.shift(size() - pixel)
                .shift(pixel - size())
                .or(that.opacity.shift(-pixel).shift(pixel));

        return new LcdImageLine(finalMsb, finalLsb, finalOpacity);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        Preconditions.checkArgument(that instanceof LcdImageLine);

        LcdImageLine tmp = (LcdImageLine) that;
        if (size() != tmp.size()) {
            return false;
        }

        if (msb.equals(tmp.msb) && lsb.equals(tmp.lsb)
                && opacity.equals(tmp.opacity)) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = "";
        s += msb.toString();
        s += "\r\n";
        s += lsb.toString();
        s += "\r\n";
        s += opacity.toString();
        return s;
    }

    private static boolean is32Multiple(int a) {
        return a % 32 == 0;
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
