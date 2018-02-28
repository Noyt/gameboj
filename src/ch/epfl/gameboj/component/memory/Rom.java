package ch.epfl.gameboj.component.memory;

<<<<<<< HEAD
import java.util.Arrays;

=======
>>>>>>> 1ef0e68418330c03eb5f02388b72de1f4b291b2f
public final class Rom {
    private byte[] memory;

    // TODO import exception ?
    public Rom(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
<<<<<<< HEAD

        memory = Arrays.copyOf(data, data.length);
=======
        
        
        // TODO A corriger : il faut utiliser copyOf de la class Array
        memory = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            memory[i] = data[i];
        }
>>>>>>> 1ef0e68418330c03eb5f02388b72de1f4b291b2f
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
