package ch.epfl.gameboj.component.memory;

import java.util.ArrayList;
import ch.epfl.gameboj.Preconditions;

public class Ram {
    
    //private ArrayList<Byte> memory;
    private byte[] memory;
    
    public Ram(int size){
        Preconditions.checkArgument(size >= 0);
        //memory = new ArrayList<Byte>(size);
        memory = new byte[size];
    }
    
    public int size() {
        //return memory.size();
        return memory.length;
    }
    
    public int read(int index) {
        //if (index < 0 || index > memory.size()) {
        if (index < 0 || index >= memory.length) {
            throw new IndexOutOfBoundsException();
        }
        //return Byte.toUnsignedInt(memory.get(index));
        return Byte.toUnsignedInt(memory[index]);
    }
    
    public void write(int index, int value) {
        //if (index < 0 || index > memory.size()) {
        if (index < 0 || index > memory.length) {
            throw new IndexOutOfBoundsException();
        }
        Preconditions.checkBits8(value);
        //memory.set(index, (byte)value);
        memory[index]=(byte)value;
    }
}
