package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {

    private Alu() {
    };

    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
    };

    public enum RotDir {
        LEFT, RIGHT
    }

    /**
     * 
     * @param v
     * @param z
     * @param n
     * @param h
     * @param c
     * @return
     */
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        return (v << Byte.SIZE) | Flag.C.mask() * (c ? 1 : 0)
                | Flag.H.mask() * (h ? 1 : 0) | Flag.N.mask() * (n ? 1 : 0)
                | Flag.Z.mask() * (z ? 1 : 0);
    }

    /**
     * 
     * @param z
     * @param n
     * @param h
     * @param c
     * @return
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(0, z, n, h, c);
    }

    /**
     * works for 8 bits and 16 bits values
     * 
     * @param valueFlags
     * @return
     */
    public static int unpackValue(int valueFlags) {
        return Preconditions.checkBits16(valueFlags >>> Byte.SIZE);
    }

    public static int unpackFlags(int valueFlags) {
        Preconditions.checkBits16(unpackValue(valueFlags));

        return Bits.clip(Byte.SIZE, valueFlags);
    }

    /**
     * 
     * @param l
     * @param r
     * @param c0
     * @return
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int carry = c0 ? 1 : 0;
        int uncutResult = l + r + carry;
        int result = Bits.clip(Byte.SIZE, uncutResult);
        boolean z = (result == 0);
        // un peu lourd
        boolean h = (Bits.clip(4, l) + Bits.clip(4, r) + carry > 0x0F);
        boolean c = uncutResult > 0xFF;

        return packValueZNHC(result, z, false, h, c);
    }

    public static int add(int l, int r) {
        return add(l, r, false);
    }

    // TODO guerre de la mise en forme
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        int result = Bits.clip(16, l + r);
        int lowResult = add(Bits.clip(Byte.SIZE, l), Bits.clip(Byte.SIZE, r));
        int lowFlags = unpackFlags(lowResult);
        boolean h = Bits.test(lowFlags, Flag.H.index());
        boolean c = Bits.test(lowFlags, Flag.C.index());

        return packValueZNHC(result, false, false, h, c);
    }

    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        int result = Bits.clip(16, l + r);
        boolean carry = Bits.clip(8, l) + Bits.clip(8, r) > 0xFF;
        int highResult = add(Bits.extract(l, Byte.SIZE, Byte.SIZE),
                Bits.extract(r, Byte.SIZE, Byte.SIZE), carry);

        int highFlags = unpackFlags(highResult);
        boolean h = Bits.test(highFlags, Flag.H.index());
        boolean c = Bits.test(highFlags, Flag.C.index());

        return packValueZNHC(result, false, false, h, c);
    }

    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int borrow = (b0 ? 1 : 0);
        // TODO coder les 4 avec une static final
        boolean c = l < r + borrow;
        boolean h = Bits.clip(4, l) < Bits.clip(4, r) + borrow;
        int result = l - r - borrow;
        if (c) {
            result += 256;
        }
        boolean z = (r == l);

        return packValueZNHC(result, z, true, h, c);
    }

    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {

        Preconditions.checkBits8(v);

        boolean fixL = (h || (!n && Bits.clip(4, v) > 0x9));
        boolean fixH = (c || (!n && (v > 0x99)));
        int fix = 0x60 * (fixH ? 1 : 0) + 0x6 * (fixL ? 1 : 0);
        int Va = n ? v - fix : v + fix;

        return packValueZNHC(Va, Va == 0, n, false, fixH);
    }

    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int v = l & r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, true, false);
    }

    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int v = l | r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, false);
    }

    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int v = l ^ r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, false);
    }

    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);

        int value = Bits.clip(Byte.SIZE, v << 1);
        boolean z = (value == 0);
        boolean c = Bits.test(v, Byte.SIZE - 1);
        return packValueZNHC(value, z, false, false, c);
    }

    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        
        int value = Bits.clip(Byte.SIZE, Bits.signExtend8(v) >> 1);
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);

        int value = v >>> 1;
        // int value = v / 2;
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        // TODO precondition on RotDir?

        int rotValue = rotateFor9or8Int(d, v, false);

        boolean c = Bits.test(rotValue, (d == RotDir.LEFT) ? 0 : Byte.SIZE - 1);

        return packValueZNHC(rotValue, rotValue == 0, false, false, c);
    }

    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);

        int nineBitsValue = v | (c ? Bits.mask(Byte.SIZE) : 0);

        int rotValue = rotateFor9or8Int(d, nineBitsValue, true);
        int clippedRotValue = Bits.clip(Byte.SIZE, rotValue);

        return packValueZNHC(clippedRotValue, clippedRotValue == 0, false,
                false, Bits.test(rotValue, Byte.SIZE));

    }


    private static int rotateFor9or8Int(RotDir d, int v, boolean nineOrNot) {

        int rotValue = 0;

        if (d == RotDir.LEFT) {
            rotValue = Bits.rotate(Byte.SIZE + (nineOrNot ? 1 : 0), v, 1);
        } else {
            rotValue = Bits.rotate(Byte.SIZE + (nineOrNot ? 1 : 0), v, -1);
        }

        return rotValue;
    }

    public static int swap(int v) {
        Preconditions.checkBits8(v);

        int lowBits = Bits.clip(4, v) << 4;
        int highBits = Bits.extract(v, 4, 4);

        boolean z = (v == 0);

        return packValueZNHC(highBits | lowBits, z, false, false, false);
    }

    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, Byte.SIZE);

        return packValueZNHC(0, Bits.test(v, bitIndex), false, true, false);
    }

}
