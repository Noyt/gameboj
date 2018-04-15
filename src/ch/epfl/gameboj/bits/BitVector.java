package ch.epfl.gameboj.bits;

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

    }

    public static final class Builder {

        public Builder(int size) {
            Preconditions.checkArgument(size >= 0 && is32Multiple(size));
        }

        public Builder setByte(int index, int value) {
            Preconditions.checkBits8(value);
        }

        public BitVector build() {

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
