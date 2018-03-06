package ch.epfl.gameboj.bits;

public interface Bit {

    // TODO public, private pour ces méthodes ?
    /**
     * Returns the position of the Bit, in the order of its enum declaration
     * 
     * @return an int
     */
    int ordinal();

    /**
     * @return an int : the same value as ordinal, but with a little more
     *         "expressive" name
     */
    default int index() {
        return ordinal();
    }

    /**
     * constructs a mask of the Bit : a value which the only "1" is the bit
     * which corresponds to the index
     * 
     * @return an int : the mask of the Bit
     */
    default int mask() {
        return Bits.mask(ordinal());
    }
}
