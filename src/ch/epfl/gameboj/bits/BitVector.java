package ch.epfl.gameboj.bits;

import java.util.Iterator;
import java.util.Objects;

import javax.swing.text.ChangedCharSetException;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {

    public static final int ALL_ZEROS_INTEGER = 0b00000000_00000000_00000000_00000000;
    public static final int ALL_ONES_INTEGER = 0b11111111_11111111_11111111_11111111;

    // TODO à discuter
    private final int[] vector;

    // TODO essayer d'utiliser le BitVector privé dans ce constructeur ?
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size >= 0 && is32Multiple(size));

        int numberOfInts = size / Integer.SIZE;
        vector = new int[numberOfInts];
        for (int i = 0; i < numberOfInts; i++) {
            vector[i] = initialValue ? ALL_ONES_INTEGER : ALL_ZEROS_INTEGER;
        }
    }

    public BitVector(int size) {
        this(size, false);
    }

    // stocker sans copier
    public BitVector(int[] elements) {
        vector = elements;
    }

    private static final class Builder {

        // TODO à discuter

        private byte[] bytes;

        public Builder(int size) {
            Preconditions.checkArgument(size >= 0 && is32Multiple(size));
            bytes = new byte[size / Byte.SIZE];
        }

        public Builder setByte(int index, int value) {
            checkIfBuiltAlready();
            Objects.checkIndex(index, bytes.length);
            Preconditions.checkBits8(value);
            bytes[index] = (byte) value;
            return this;
        }

        public BitVector build() {
            checkIfBuiltAlready();
            int length = bytes.length;
            int[] temp = new int[length / (Integer.SIZE / Byte.SIZE)];

            for (int i = 0; i < temp.length; i++) {
                int a = 0;
                for (int j = 0; j < Integer.SIZE / Byte.SIZE; j++) {
                    a += Byte.toUnsignedInt(bytes[i + j]) << Byte.SIZE * j;
                }
                temp[i] = a;
            }

            bytes = null;
            return new BitVector(temp);
        }

        private void checkIfBuiltAlready() {
            if (bytes == null) {
                throw new IllegalStateException();
            }
        }
    }

    public int size() {
        return vector.length * Integer.SIZE;
    }

    public boolean testBit(int index) {
        Objects.checkIndex(index, this.size());
        return Bits.test(vector[index / Integer.SIZE], index % Integer.SIZE);
    }

    public BitVector not() {
        int[] elements = new int[vector.length];
        int i = 0;
        for (int a : vector) {
            elements[i] = ~a;
            i++;
        }
        return new BitVector(elements);
    }

    public BitVector and(BitVector that) {

    }

    public BitVector or(BitVector that) {

    }

    public BitVector extractZeroExtended(int index, int length) {

    }

    public BitVector extractWrapped(int index, int length) {

    }

    public BitVector shift(int index) {

    }

    public int[] extract(int index, int length, boolean type) {

        int arrayIndex = Math.floorDiv(index, Integer.SIZE);
        int intIndex = Math.floorMod(index, Integer.SIZE);

        int nbArrayToCompute = Math.floorDiv(length, Integer.SIZE);
        int[] array = new int[nbArrayToCompute];
        int temp1 = computeInt(type, arrayIndex);
        int temp2 = 0;
        for (int i = 0; i < nbArrayToCompute; i++) {
            if (is32Multiple(index)) {
                array[i] = computeInt(type, i + arrayIndex);
            } else {
                temp2 = computeInt(type, i + arrayIndex + 1);
                
                temp1 = temp1 >>> Integer.SIZE - intIndex;
                System.out.println("temp 1 " + temp1);
                System.out.println("temp 2 " + temp2);
                    
                array[i] = (temp2 << intIndex) | temp1;
                
                temp1 = temp2;
            }
        }

        return array;
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

    // TODO enumeration maybe pour le type
    private int computeInt(boolean type, int index) {

        int size = vector.length;
        if (size >= index && index < 0) {
            System.out.println("help");
            
            return type ? ALL_ZEROS_INTEGER
                    : vector[Math.floorMod(index, size)];
        } else {
            return vector[Math.floorMod(index, size)];
        }

    }
}
