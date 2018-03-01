package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Alu.RotDir;

public class AluTest {
    
    // Illegal Values Tests
    @Test 
    void unpackValueFailsForInvalidValueFlags() {
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackValue(-1));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackValue(1 << 24));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackValue( (1 << 24 ) + 5));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackValue(-100));
    }
 
    @Test
    void unpackFlagsFailsForInvalidValueFlags() {
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackFlags(-1));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackFlags(1 << 24));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackFlags( (1 << 24 ) + 5));
        assertThrows(IllegalArgumentException.class, () -> Alu.unpackFlags(-100));
    }
    
    @Test
    void addFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(256, 255)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add(255, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(256, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-15, -300));
        
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-1, 3,true));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(3, -1, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-5, -1, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(256, 255, true)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add(255, 256, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(256, 256, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(980, -3, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(-15, -300, true));
    }
    
    @Test 
    void add16FailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(256, 1 << 16 + 5)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(1 << 16, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(1<<16, 1 << 16));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(-15, -300));
        
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(266, 1 << 16)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(1<<16, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(1 << 16 -1, 1 << 16));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(-15, -300));
    }
    
    @Test
    void subFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(256, 255)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(255, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(256, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-15, -300));
        
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-1, 3,true));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(3, -1, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-5, -1, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(256, 255, true)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(255, 256, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(256, 256, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(980, -3, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-15, -300, true));
    }
    
    @Test
    void bcdAdjustFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(-1, true, false, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(-200, false, false, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(256, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(1000, true, false, false));

    }
    
    @Test
    void andFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(256, 255)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.and(255, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(256, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-15, -300));
    }
 
    @Test
    void orFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(256, 255)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.or(255, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(256, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-15, -300));
    }
    
    @Test
    void xorFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(256, 255)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(255, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(256, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-15, -300));
    }
    
    @Test
    void shiftFailForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(-1));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(1000));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightA(-5));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightA(256));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightL(-100));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightL(500));

    }

    @Test
    void rotateFailForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(RotDir.LEFT,-5));
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(RotDir.RIGHT,256));
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(RotDir.RIGHT,-45));
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(RotDir.LEFT,1000));

    }
    
    @Test
    void swapFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(-45));   
    }
    
    @Test
    void testBitFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(256, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(256, 8));
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, 8));
    }
    
    @Test
    void maskZNHCWorksForAnyCombinationOfBoolean() {
        
        // 0 true
        assertEquals(0, Alu.maskZNHC(false, false, false, false));
        
        // 1 true
        assertEquals(1 << 7, Alu.maskZNHC(true, false, false, false));
        assertEquals(1 << 6, Alu.maskZNHC(false, true, false, false));
        assertEquals(1 << 5, Alu.maskZNHC(false, false, true, false));
        assertEquals(1 << 4, Alu.maskZNHC(false, false, false, true));
        
        //2 true
        assertEquals(192, Alu.maskZNHC(true, true, false, false));
        assertEquals(160, Alu.maskZNHC(true, false, true, false));
        assertEquals(144, Alu.maskZNHC(true, false, false, true));
        assertEquals(96, Alu.maskZNHC(false, true, true, false));
        assertEquals(80, Alu.maskZNHC(false, true, false, true));
        assertEquals(48, Alu.maskZNHC(false, false, true, true));
        
        //3 true
        assertEquals(224, Alu.maskZNHC(true, true, true, false));
        assertEquals(208, Alu.maskZNHC(true, true, false, true));
        assertEquals(112, Alu.maskZNHC(false, true, true, true));
        
        //4 true
        assertEquals(240, Alu.maskZNHC(true, true, true, true));
    }
    
    @Test
    void bcdAdjustWorksForValidValue() {
        assertEquals(0x7300, Alu.bcdAdjust(0x6D, false, false, false));
        assertEquals(0x0940, Alu.bcdAdjust(0x0F, true, true, false));
    }
    
    @Test
    void andWorksForValidValue() {
        assertEquals(0x320, Alu.and(0x53, 0xA7));
        assertEquals(0xA0, Alu.and(0xF0, 0x0F));
    }
    
    @Test
    void orWorksForValidValue() {
        assertEquals(0xF700, Alu.or(0x53, 0xA7));
        assertEquals(0x80, Alu.or(0, 0));
    }
    
    @Test
    void xorWorksForValidValue() {
        assertEquals(0xF400, Alu.xor(0x53, 0xA7));
        assertEquals(0x80, Alu.xor(0xAB, 0xAB));
    }
    
    @Test
    void shiftLeftWorksForValidValue() {
        assertEquals(0x90, Alu.shiftLeft(0x80));
        assertEquals(0x2000, Alu.shiftLeft(0x10));
    }
    
    @Test
    void shiftRightLWorksForValidValue() {
        assertEquals(0x4000, Alu.shiftRightL(0x80));
        assertEquals(0x90, Alu.shiftRightL(0x1));
    }
    
    @Test
    void shiftRightAWorksForValidValue() {
        assertEquals(0xC000, Alu.shiftRightA(0x80));
        assertEquals(0xC010, Alu.shiftRightA(0x81));
        assertEquals(0x90, Alu.shiftRightA(0x1));
    }
    
    @Test
    void rotateWorksForValidValue() {
        assertEquals(0x110, Alu.rotate(RotDir.LEFT, 0x80));
        assertEquals(0x90, Alu.rotate(RotDir.LEFT, 0x80, false));
        assertEquals(0x100, Alu.rotate(RotDir.LEFT, 0x00, true));
        assertEquals(0x80, Alu.rotate(RotDir.LEFT, 0));
        assertEquals(0x80, Alu.rotate(RotDir.RIGHT, 0));
        assertEquals(0x8010, Alu.rotate(RotDir.RIGHT, 0x1));
        assertEquals(0x5010, Alu.rotate(RotDir.RIGHT, 0xA1, false));
        assertEquals(0x5000, Alu.rotate(RotDir.RIGHT, 0xA0, false));
        assertEquals(0xE800, Alu.rotate(RotDir.RIGHT, 0xD0, true));
        
    }
    
    @Test
    void swapWorksForValidValue() {
        assertEquals(0x80, Alu.swap(0));
        assertEquals(0xF00, Alu.swap(0xF0));
    }
    
    @Test
    void testBitWorksForValidValue() {
        assertEquals(0xA0, Alu.testBit(0x20, 5));
        assertEquals(0x20, Alu.testBit(0x08, 5));
    }
}
