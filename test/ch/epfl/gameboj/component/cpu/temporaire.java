package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class temporaire {
    
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    private void cycleCpu(Cpu cpu, long start, long cycles) {
        for (long c = start; c < start + cycles; ++c) {
            cpu.cycle(c);
        }
    }

    
    @Test
    void LD_HLSP_S8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0);

        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);

        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);

        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0);

        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0);
        System.out.println("hello 1");
        cycleCpu(c, 13);
        System.out.println("fin hello 1");
        // b.write(10,nop)
        // b.write(10,nop)
        // b.write(10,nop)

        b.write(13, Opcode.LD_SP_N16.encoding);
        b.write(14, 0b1001000);
        b.write(15, 0b1111011);

        b.write(16, Opcode.LD_HL_SP_N8.encoding);
        b.write(17, 92);
        System.out.println("hellooooooooooooooooooooooooooooooooooooooooooo ");
        cycleCpu(c, 16, 3);
        System.out.println("fin hello ");
        assertArrayEquals(new int[] { 18, 31560, 0, 0b0010 << 4, 0, 0, 0, 0,
                0b1111011, 0b10100100 }, c._testGetPcSpAFBCDEHL());

        b.write(18, Opcode.LD_SP_N16.encoding);
        b.write(19, 0b11001000);
        b.write(20, 0b1111011);
        cycleCpu(c, 19, 3);

        // maintenant SP = 31688

        b.write(21, Opcode.LD_HL_SP_N8.encoding);
        b.write(22, 92);
        cycleCpu(c, 22, 3);
        assertArrayEquals(new int[] { 23, 31688, 0, 0b0011 << 4, 0, 0, 0, 0,
                0b1111100, 0b100100 }, c._testGetPcSpAFBCDEHL());

        b.write(23, Opcode.ADD_SP_N.encoding);
        b.write(24, 237);
        cycleCpu(c, 25, 4);
        assertArrayEquals(new int[] { 25, 31669, 0, 0b0011 << 4, 0, 0, 0, 0,
                0b1111100, 0b100100 }, c._testGetPcSpAFBCDEHL());

        b.write(25, Opcode.LD_SP_N16.encoding);
        b.write(26, 0b11001000);
        b.write(27, 0b1111011);
        cycleCpu(c, 29, 3);

        b.write(28, Opcode.ADD_SP_N.encoding);
        b.write(29, 213);
        cycleCpu(c, 31, 4);
        assertArrayEquals(new int[] { 30, 31645, 0, 0b0001 << 4, 0, 0, 0, 0,
                0b1111100, 0b100100 }, c._testGetPcSpAFBCDEHL());
    }
}
