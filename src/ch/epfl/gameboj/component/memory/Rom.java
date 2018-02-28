package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

public final class Rom {
    private byte[] memory;

    // TODO import exception ?
    public Rom(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }

        memory = Arrays.copyOf(data, data.length);
    }
    
    public int size() {
        return memory.length;
    }

    public int read(int index) {
        if (index < 0 || index >= memory.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(memory[index]);
    }
}
