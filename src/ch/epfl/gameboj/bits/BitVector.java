package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {

    // TODO à discuter
    private final int[] vector;

    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size >= 0 && is32Multiple(size));
    }

    public BitVector(int size) {
        BitVector(size, false);
    }

    // stocker sans copier
    private BitVector(int[] elements) {
        vector = elements;
    }

    private static final class Builder {

        //TODO à discuter
        
        private byte[] components;
        
        public Builder(int size) {
            Preconditions.checkArgument(size >= 0 && is32Multiple(size));
            components = new byte[size / Byte.SIZE];
        }

        public Builder setByte(int index, int value) {
            Objects.checkIndex(index, components.length);
            Preconditions.checkBits8(value);
            components[index] = (byte) value;
            return this;
        }

        public BitVector build() {
            int length = components.length;
            int[] temp = new int[length / 4];
            
            int a;
            
            for (int i = 0; i < temp.length; i++) {
                a = 0;
                for (int j = 0; j < 4; j++) {
                  a += Byte.toUnsignedInt(components[i + j]) << Byte.SIZE * j;  
                }
                temp[i] = a;
            }
            
            return new BitVector(temp);
        }
    }

    public int size() {

    }


    public boolean testBit(int index) {

    }

    public BitVector not() {

    }

    public BitVector and(BitVector vector) {

    }

    public BitVector or(BitVector vector) {

    }

    public BitVector extractZeroExtended(int index, int length) {

    }

    public BitVector extractWrapped(int index, int length) {

    }

    public BitVector shift(int index) {

    }

    private BitVector extract(int index, int length, boolean type) {

    }

    @Override
    public boolean equals(Object that) {

    }
    
    @Override
    public int hashCode() {
        
    }
    
    @Override
    public String toString() {
        
    }

    private static boolean is32Multiple(int a) {
        return a % 32 == 0;
    }
}
