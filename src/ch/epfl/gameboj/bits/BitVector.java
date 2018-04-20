package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import javax.swing.text.ChangedCharSetException;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {

    public static final int ALL_ZEROS_INTEGER = 0b00000000_00000000_00000000_00000000;
    public static final int ALL_ONES_INTEGER = 0b11111111_11111111_11111111_11111111;

    // TODO à discuter
    private final int[] vector;

    // TODO il faudra mettre en private quand on aura fini les tests
    public enum Extraction {
        WRAPPED, ZERO_EXTENDED
    };

    // TODO essayer d'utiliser le BitVector privé dans ce constructeur ?
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size >= 0 && is32Multiple(size));

        int numberOfInts = size / Integer.SIZE;
        vector = new int[numberOfInts];
        if (initialValue) {
            Arrays.fill(vector, ALL_ONES_INTEGER);
        }
    }

    public BitVector(int size) {
        this(size, false);
    }

    // stocker sans copier
    private BitVector(int[] elements) {
        vector = elements;
    }

    public static final class Builder {

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
                int intByteRatio = Integer.SIZE / Byte.SIZE;
                for (int j = 0; j < intByteRatio; j++) {
                    a += Byte.toUnsignedInt(
                            bytes[i * intByteRatio + j]) << (Byte.SIZE * j);
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
        return andOr(that, true);
    }

    public BitVector or(BitVector that) {
        return andOr(that, false);
    }

    public BitVector extractZeroExtended(int index, int length) {
        return new BitVector(extract(index, length, Extraction.ZERO_EXTENDED));
    }

    public BitVector extractWrapped(int index, int length) {
        return new BitVector(extract(index, length, Extraction.WRAPPED));
    }

    public BitVector shift(int distance) {
        return new BitVector(
                extract(-distance, size(), Extraction.ZERO_EXTENDED));
    }

    private int[] extract(int index, int length, Extraction type) {

        Preconditions.checkArgument(is32Multiple(length));

        int arrayIndex = Math.floorDiv(index, Integer.SIZE);
        int intIndex = Math.floorMod(index, Integer.SIZE);

        int nbIntegersToCompute = Math.floorDiv(length, Integer.SIZE);
        int[] array = new int[nbIntegersToCompute];

        int temp1 = computeInt(type, arrayIndex);
        int temp2 = 0;
        for (int i = 0; i < nbIntegersToCompute; i++) {
            if (is32Multiple(index)) {
                array[i] = computeInt(type, i + arrayIndex);
            } else {
                temp2 = computeInt(type, i + arrayIndex + 1);
                temp1 = temp1 >>> intIndex;
                array[i] = (temp2 << (32 - intIndex)) | temp1;

                temp1 = temp2;
            }
        }

        return array;
    }

    @Override
    public boolean equals(Object that) {
        Preconditions.checkArgument(that instanceof BitVector);
        return this.vector.equals(((BitVector) that).vector);
    }

    @Override
    public int hashCode() {
        return vector.hashCode();
    }

    @Override
    public String toString() {
        String binary = "";
        for (int i = this.size()-1; i >= 0; i--) {
           binary += this.testBit(i) ? "1" : "0";
        }
        return binary;
    }

    private static boolean is32Multiple(int a) {
        return a % 32 == 0;
    }

    // TODO enumeration maybe pour le type
    private int computeInt(Extraction type, int index) {
        int size = vector.length;
        if (size <= index || index < 0) {
            return type == Extraction.ZERO_EXTENDED ? ALL_ZEROS_INTEGER
                    : vector[Math.floorMod(index, size)];
        } else {
            return vector[Math.floorMod(index, size)];
        }

    }
    
    private BitVector andOr(BitVector that, boolean and) {
        Preconditions.checkArgument(that.size() == size());
        int length = vector.length;

        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            int other = that.vector[i];
            int me = vector[i];
            result[i] = and ? other & me : other | me;
        }

        return new BitVector(result);
    }

    // TODO il faudra supprimer ça quand on en n'aura plus besooin pour les
    // tests
    public int[] extractP(int index, int length, Extraction type) {
        return extract(index, length, type);
    }
}
