package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

/**
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Rom {

    private byte[] memory;

    /**
     * constructs the non-volatile memory that contains an array of byte
     * 
     * @param data
     *            : an array of byte which will be copied as the memory
     */
    public Rom(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }

        memory = Arrays.copyOf(data, data.length);
    }

    /**
     * gives the size of the memory
     * 
     * @return an int, the size of the memory
     */
    public int size() {
        return memory.length;
    }

    /**
     * returns the data contained in the memory at the given index
     * 
     * @param index
     *            an int, the index of the array that we want the data
     * @return an int, the data at the given index in the memory
     */
    public int read(int index) {
        if (index < 0 || index >= memory.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(memory[index]);
    }
}
