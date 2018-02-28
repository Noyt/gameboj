package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AluTest {

    @Test
    void maskZNHCWorksForAnyCombinationOfBoolean() {
        assertEquals(Alu.maskZNHC(true, false, false, false), 1 << 7);
    }
 
}
