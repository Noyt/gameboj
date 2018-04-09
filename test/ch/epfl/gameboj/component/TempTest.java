package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import static ch.epfl.test.TestRandomizer.*;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Cpu;

public class TempTest implements ComponentTest {

    @Override
    public Timer newComponent() {
        return new Timer(new Cpu());
    }
    
    @Test
    void mainCounterGetsResetByAnyWriteToDIV() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            Timer t = newComponent();
            t.write(0xFF04, 0);
            for (int c = 0; c <= 0x3F; ++c)
                t.cycle(c);
            
            assertEquals(1, t.read(0xFF04));
            t.write(0xFF04, rng.nextInt(0x100));
            assertEquals(0, t.read(0xFF04));
        }
    }
}
