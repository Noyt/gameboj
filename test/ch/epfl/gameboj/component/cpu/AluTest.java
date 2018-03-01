package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Alu.RotDir;

public class AluTest {
    
    /*
     *  Illegal Values Tests
     */
    
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
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(256, (1 << 16) + 5)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(1 << 16, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(1<<16, 1 << 16));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(980, -3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(-15, -300));
        
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(3, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(-5, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(266, 1 << 16)); 
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(1<<16, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H((1 << 16) -1, 1 << 16));
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
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(256));
    }
    
    @Test
    void testBitFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(-5, 3));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(900, 5));
        
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(250, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, 8));
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, 255));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(256, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(256, 8));
    }
    
    
    /*
     * Functions Work for all valid values Tests
     */
    
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
    void unpackValueWorksForAnyValidValue() {
        assertEquals(128, Alu.unpackValue((128 << 8) + 3));
        assertEquals(34, Alu.unpackValue(34 << 8));
        assertEquals(255, Alu.unpackValue((255 << 8) + 25));
        assertEquals(1678, Alu.unpackValue((1678 << 8) + 127));
        assertEquals(298, Alu.unpackValue((298 << 8) + 16));
    }
  
    @Test
    void unpackFlagsWorksForAnyValidValue() {
        assertEquals(1 << 7, Alu.unpackFlags((1 << 7) + 4864));
        assertEquals(1 << 6, Alu.unpackFlags((1 << 6) + 256));
        assertEquals(1 << 5, Alu.unpackFlags((1 << 5) + 33024));
        assertEquals(1 << 4, Alu.unpackFlags((1 << 4) + (1 << 23)));
        
        assertEquals(192, Alu.unpackFlags(192 + (65535 << 8)));
        assertEquals(160, Alu.unpackFlags(160 + (26 << 8)));
        assertEquals(144, Alu.unpackFlags(144 + (8243 << 8)));
        assertEquals(96, Alu.unpackFlags(96 + (324 << 8)));
        assertEquals(80, Alu.unpackFlags(80 + (8243 << 8)));
        assertEquals(48, Alu.unpackFlags(48 + (72 << 8)));

        assertEquals(224, Alu.unpackFlags(224 + (7362 << 8)));
        assertEquals(208, Alu.unpackFlags(208 + (43323 << 8)));
        assertEquals(112, Alu.unpackFlags(112 + (32156 << 8)));

        assertEquals(240, Alu.unpackFlags(240 + (8243 << 8)));

        assertEquals(201, Alu.unpackFlags(6681289));
    }
    
    @Test
    void addWorksForAnyValidValues() {
        assertEquals((37 << 8) + (0 << 4), Alu.add(16, 21));
        assertEquals((16 << 8) + (2 << 4), Alu.add(8, 8));
        assertEquals((29 << 8) + (0 << 4) , Alu.add(12, 17));
        assertEquals((146 << 8) + (3 << 4) , Alu.add(230, 172));
        assertEquals((0 << 8) + (8 << 4) , Alu.add(0, 0));
        assertEquals((142 << 8) + (1 << 4) ,Alu.add(226, 172));
        assertEquals((255 << 8) + ( 0 << 4),Alu.add(128, 127));
        assertEquals((0 << 8) + (9 << 4), Alu.add(192, 64));
        assertEquals((0 << 8) + (11 << 4), Alu.add(158, 98));

        
        assertEquals((0 << 8) + (11 << 4) ,Alu.add(128, 127, true));
        assertEquals((30 << 8) + (0 << 4) ,Alu.add(12, 17, true));
        assertEquals((17 << 8) + (2 << 4) ,Alu.add(8, 8, true));
        assertEquals((32 << 8) + (2 << 4) ,Alu.add(8, 23,true));
        assertEquals((13 << 8) + (1 << 4), Alu.add(170, 98, true));
        assertEquals((37 << 8) + (3 << 4), Alu.add(101, 191,true));
        
        assertEquals((27853 << 8) + (0 << 4), Alu.add16L(10757, 17096));
        assertEquals((28045 << 8) + (1 << 4), Alu.add16L(17096, 10949));
        assertEquals((59109 << 8) + (2 << 4) , Alu.add16L(8990, 50119));
        assertEquals((26385 << 8) + (3 << 4) , Alu.add16L(17010, 9375));
        assertEquals((5736 << 8) + (0 << 4) , Alu.add16L(18754, 52518));
        assertEquals((0 << 8) + (3 << 4) , Alu.add16L(65535, 1)); 
        assertEquals((0 << 8) + (1 << 4) , Alu.add16L(65504, 32)); 
        assertEquals((0 << 8) + (0 << 4) , Alu.add16L(49152, 16384)); 

       
        assertEquals((27853 << 8) + (0 << 4), Alu.add16H(10757, 17096));
        assertEquals((9601 << 8) + (1 << 4), Alu.add16H(45743, 29394));
        assertEquals((57473 << 8) + (2 << 4) , Alu.add16H(45615, 11858));
        assertEquals((41089 << 8) + (3 << 4) , Alu.add16H(45615, 61010));
        assertEquals((0 << 8) + (1 << 4) , Alu.add16H(49152, 16384)); 
        assertEquals((0 << 8) + (3 << 4) , Alu.add16H(63488, 2048)); 
    }
    
    @Test
    void subWorksForAnyValidValues() {
        assertEquals((9 << 8) + (4 << 4), Alu.sub(9, 0));
        assertEquals((0 << 8) + (12 << 4), Alu.sub(0, 0));
        assertEquals((144 << 8) + (5 << 4), Alu.sub(16, 128));
        assertEquals((0 << 8) + (12 << 4), Alu.sub(16, 16));
        assertEquals((200 << 8) + (5 << 4), Alu.sub(136, 192));
        assertEquals((200 << 8) + (7 << 4), Alu.sub(144, 200));

        assertEquals((255 << 8) + (7 << 4), Alu.sub(1, 1, true));
        assertEquals((147 << 8) + (4 << 4), Alu.sub(152, 4,true));
        assertEquals((0 << 8) + (12 << 4) , Alu.sub(1, 0, true));
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
