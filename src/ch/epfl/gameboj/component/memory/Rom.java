package ch.epfl.gameboj.component.memory;

public final class Rom {
    private byte[] memory;

    // TODO import exception ?
    public Rom(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        
        
        // TODO A corriger : il faut utiliser copyOf de la class Array
        memory = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            memory[i] = data[i];
        }
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
