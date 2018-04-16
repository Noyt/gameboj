package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class BitVectorTest {

    @Test
    void extractWorksForValidValue() {
         int[] array = {-1, 0};
         BitVector test = new BitVector(array);
         
         assertEquals(0b11111111_11111111_00000000_00000000, test.extract(-16, 32, true)[0]);
         assertEquals(0b11111111_11111111_00000000_00000000, test.extract(-16, 32, false)[0]);
         assertEquals(0b00000000_00000001_11111111_11111110, test.extract(-1, 32, false)[0]);
    }
}
