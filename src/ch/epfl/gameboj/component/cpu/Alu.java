package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Arithmetic Logic Unit
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 *
 */
public final class Alu {

    private Alu() {
    };

    /**
     * 
     * @author Arnaud Robert (287964)
     * @author Sophie Du Couedic (260007)
     * 
     *         enumeration used to reprensents bits associated to flags the
     *         first 4 bits are unused according to GameBoy instructions
     *
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
    };

    /**
     * @author Arnaud Robert (287964)
     * @author Sophie Du Couedic (260007)
     * 
     *         enumeration used to represent directions when used in rotate()
     *         methods
     */
    public enum RotDir {
        LEFT, RIGHT
    }

    /**
     * Combining an 8/16 bits value and 8 bits value to represents the flags,
     * this method returns a single int in which all information given by Alu's
     * methods can be found
     * 
     * @param v
     *            6/16 bits value
     * @param z
     *            boolean : Z flag (true if the operation returns 0, false if it
     *            does not)
     * @param n
     *            boolean : N flag (true if the operation is a difference, false
     *            if not)
     * @param h
     *            boolean : H flag (true if the operation produces a carry
     *            through the first 4 bits, false if it does not)
     * @param c
     *            boolean : C flag (true if the operation produces a carry,
     *            false if it does not)
     * @return combined value/flags in single integer
     */
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        return (v << Byte.SIZE) | Flag.C.mask() * (c ? 1 : 0)
                | Flag.H.mask() * (h ? 1 : 0) | Flag.N.mask() * (n ? 1 : 0)
                | Flag.Z.mask() * (z ? 1 : 0);
    }

    /**
     * Produces a combined mask with 1s wherever a boolean is true
     *
     * @param z
     *            Z flag
     * @param n
     *            N flag
     * @param h
     *            H flag
     * @param c
     *            C flag
     * @return ZNHC flags mask
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(0, z, n, h, c);
    }

    /**
     * Extracts the value from the value/flags package
     * 
     * @param valueFlags
     *            a binary integer combining a value and flags
     * @return only the value
     */
    public static int unpackValue(int valueFlags) {
        return Preconditions.checkBits16(valueFlags >>> Byte.SIZE);
    }

    /**
     * Extracts the flags from the value/flags package
     * 
     * @param valueFlags
     *            a binary integer combining a value and flags
     * @return only the flags
     */
    public static int unpackFlags(int valueFlags) {
        Preconditions.checkBits16(unpackValue(valueFlags));

        return Bits.clip(Byte.SIZE, valueFlags);
    }

    /**
     * Returns the sum of the two given 8 bits values and the initial carry,
     * combined with Z0HC flags
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @param c0
     *            initial carry
     * @return combined sum results / resulting flags value
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int carry = c0 ? 1 : 0;
        int uncutResult = l + r + carry;
        int result = Bits.clip(Byte.SIZE, uncutResult);
        boolean z = (result == 0);
        boolean h = (Bits.clip(4, l) + Bits.clip(4, r) + carry > 0x0F);
        boolean c = uncutResult > 0xFF;

        return packValueZNHC(result, z, false, h, c);
    }

    /**
     * Identical to the previous method but always uses false as an argument
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return combined resulting sum/flags value
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Returns the sum of the two given 16 bits values and flags 00HC, where H
     * and C are flags corresponding to the addition of the lower 8 bits of both
     * values
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return combined resulting sum/flags value
     */
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

    /**
     * Returns the sum of the two given 16 bits values and flags 00HC, where H
     * and C are flags corresponding to the addition of the higher 8 bits of
     * both values
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return combined resulting sum/flags value
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        int result = Bits.clip(16, l + r);
        boolean carry = Bits.clip(Byte.SIZE, l) + Bits.clip(Byte.SIZE, r) > 0xFF;
        int highResult = add(Bits.extract(l, Byte.SIZE, Byte.SIZE),
                Bits.extract(r, Byte.SIZE, Byte.SIZE), carry);

        int highFlags = unpackFlags(highResult);
        boolean h = Bits.test(highFlags, Flag.H.index());
        boolean c = Bits.test(highFlags, Flag.C.index());

        return packValueZNHC(result, false, false, h, c);
    }

    /**
     * Subtracts the second value from the first one while taking into account
     * a potential initial borrow, as well as flags Z1HC
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @param b0
     *            initial borrow
     * @return combined resulting difference/flags value
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int borrow = (b0 ? 1 : 0);
        boolean c = l < r + borrow;
        boolean h = Bits.clip(4, l) < Bits.clip(4, r) + borrow;
        int result = l - r - borrow;
        if (c) {
            result += 256;
        }
        boolean z = (result == 0);

        return packValueZNHC(result, z, true, h, c);
    }

    /**
     * Identical to the previous method but always uses false as an argument
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return combined resulting difference/flags value
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Adjusts the given 8 bits value to BCD format
     * 
     * @param v
     *            value to be adjusted
     * @param n
     * @param h
     * @param c
     * @return adjusted value to BCD format
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {

        Preconditions.checkBits8(v);

        boolean fixL = h || (!n && (Bits.clip(4, v) > 0x9));
        boolean fixH = c || (!n && (v > 0x99));
        int fix = 0x60 * (fixH ? 1 : 0) + 0x6 * (fixL ? 1 : 0);
        int Va = n ? (v - fix) : (v + fix);

        Va = Bits.clip(Byte.SIZE, Va);

        return packValueZNHC(Va, Va == 0, n, false, fixH);
    }

    /**
     * Applies a bit-to-bit "and" (&) to the given 8 bits values
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return bit-to-bit "and" result
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int v = l & r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, true, false);
    }

    /**
     * Applies a bit-to-bit "inclusive or" (|) to the given 8 bits values
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return bit-to-bit "inclusive or" result
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int v = l | r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, false);
    }

    /**
     * Applies a bit-to-bit "exclusive or" (^) to the given 8 bits values
     * 
     * @param l
     *            1st value
     * @param r
     *            2nd value
     * @return bit-to-bit "exclusive or" result
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int v = l ^ r;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, false);
    }

    /**
     * Returns the given 8 bits value shifted on the left by 1 bit and flags
     * Z00C with flag C containing the ejected bit, C being false when said bit
     * was equal to 1
     * 
     * @param v
     *            8 bits value
     * @return shifted value combined with appropriate flags
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);

        int value = Bits.clip(Byte.SIZE, v << 1);
        boolean z = (value == 0);
        boolean c = Bits.test(v, Byte.SIZE - 1);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Returns the given 8 bits value arithmetically shifted on the right by 1
     * bit and flags Z00C with flag C containing the ejected bit, C being false
     * when said bit was equal to 1
     * 
     * @param v
     *            8 bits value
     * @return shifted value combined with appropriate flags
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);

        int value = Bits.clip(Byte.SIZE, Bits.signExtend8(v) >> 1);
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Returns the given 8 bits value logically shifted on the right by 1 bit
     * and flags Z00C with flag C containing the ejected bit, C being false when
     * said bit was equal to 1
     * 
     * @param v
     *            8 bits value
     * @return shifted value combined with appropriate flags
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);

        int value = v >>> 1;
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Returns the length one rotation of the given 8 bits value in the given
     * direction and flags Z00C with C containing the bit that was passed from
     * one end to another
     * 
     * @param d
     *            direction, can be Left or Right
     * @param v
     *            value to be rotated
     * @return rotated value combined with appropriate flags
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);

        int rotValue = rotateFor9or8Int(d, v, false);

        boolean c = Bits.test(rotValue, (d == RotDir.LEFT) ? 0 : Byte.SIZE - 1);

        return packValueZNHC(rotValue, rotValue == 0, false, false, c);
    }

    /**
     * Applies a rotation through carry to the given value and carry flag in the
     * given direction and then returns a combination of the result with flags
     * Z00C. This operation first constructs a 9 bits value from the initial
     * value and the carry flag, then rotates it in the given direction and
     * finally splits the result, with the lower 8 bits becoming the returned
     * value and the higher bit becoming the new carry flag
     * 
     * @param d
     *            direction, can be Left or Right
     * @param v
     *            value to be rotated
     * @param c
     *            carry
     * @return rotated value combined with appropriate flags
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);

        int nineBitsValue = v | (c ? Bits.mask(Byte.SIZE) : 0);

        int rotValue = rotateFor9or8Int(d, nineBitsValue, true);
        int clippedRotValue = Bits.clip(Byte.SIZE, rotValue);

        return packValueZNHC(clippedRotValue, clippedRotValue == 0, false,
                false, Bits.test(rotValue, Byte.SIZE));

    }

    /**
     * This method applies a rotation to an int which can be either 8 or 9 bits
     * long
     * 
     * @param d
     *            direction, can be Left or Right
     * @param v
     *            value to be rotated
     * @param nineOrNot
     *            boolean : true if the value is 9 bits, false if it is 8 bits
     * @return the rotated value
     */
    private static int rotateFor9or8Int(RotDir d, int v, boolean nineOrNot) {

        int rotValue = 0;

        if (d == RotDir.LEFT) {
            rotValue = Bits.rotate(Byte.SIZE + (nineOrNot ? 1 : 0), v, 1);
        } else {
            rotValue = Bits.rotate(Byte.SIZE + (nineOrNot ? 1 : 0), v, -1);
        }

        return rotValue;
    }

    /**
     * Returns the value obtained by swapping the lower 4 bits with the higher 4
     * bits of the given value as well as flags Z000
     * 
     * @param v
     *            value to be swapped
     * @return resulting value combined with appropriate flags
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);

        int lowBits = Bits.clip(4, v) << 4;
        int highBits = Bits.extract(v, 4, 4);

        boolean z = (v == 0);

        return packValueZNHC(highBits | lowBits, z, false, false, false);
    }

    /**
     * Returns value 0 and flags Z010 with Z true if and only if the bit on the
     * given index of the given value is 0
     * 
     * @param v
     * @param bitIndex
     * @return combined 0 value and flags Z010
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, Byte.SIZE);

        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }

}
