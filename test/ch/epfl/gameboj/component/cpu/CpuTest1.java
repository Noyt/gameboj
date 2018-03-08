package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest1 {

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

    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);

        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] { 1, 0, 0xF0, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_HLRWorksFine() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0xFAF5, 34);
        b.write(0, Opcode.LD_B_HLR.encoding);

        b.write(1, Opcode.LD_C_HLR.encoding);

        b.write(2, Opcode.LD_E_HLR.encoding);

        b.write(3, Opcode.LD_A_HLR.encoding);

        b.write(4, Opcode.LD_D_HLR.encoding);

        cycleCpu(c, 9);

        assertArrayEquals(
                new int[] { 5, 0, 34, 0xF1, 34, 34, 34, 34, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_HLRU_DecrementWorksFine() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0xFAF5, 34);
        b.write(0, Opcode.LD_A_HLRD.encoding);

        cycleCpu(c, Opcode.LD_A_HLRD.cycles);
        assertArrayEquals(new int[] { 1, 0, 34, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 - 1 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void PUSH_R16_WorksFine() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.PUSH_AF.encoding);

        b.write(1, Opcode.PUSH_BC.encoding);
        cycleCpu(c, Opcode.PUSH_BC.cycles * 2);
        assertEquals(0xF4, b.read(0x10000 - 2 * 2));
        assertEquals(0xF2, b.read(0x10000 - 2 * 2 + 1));

        b.write(2, Opcode.PUSH_DE.encoding);
        cycleCpu(c, Opcode.PUSH_DE.cycles * 3);
        assertEquals(0xF7, b.read(0x10000 - 3 * 2));
        assertEquals(0xF3, b.read(0x10000 - 3 * 2 + 1));

        b.write(3, Opcode.PUSH_HL.encoding);
        cycleCpu(c, Opcode.PUSH_HL.cycles * 4);
        assertEquals(0xF5, b.read(0x10000 - 2 * 4));
        assertEquals(0xFA, b.read(0x10000 - 2 * 4 + 1));

        // cycleCpu(c, Opcode.PUSH_AF.cycles * 4);
        // assertArrayEquals(new int[] {4, 0x10000 - 8, 0xF0, 0xF1, 0xF2, 0xF4,
        // 0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_SP_HLWorksFine() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_SP_HL.encoding);
        cycleCpu(c, Opcode.LD_SP_HL.cycles);
        assertArrayEquals(new int[] { 1, 0xFAF5, 0xF0, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void LS_R8_R8WorksFine() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_A_A.encoding);
        b.write(1, Opcode.LD_C_A.encoding);
        b.write(2, Opcode.LD_L_C.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3);
        assertArrayEquals(new int[] { 3, 0, 0xF0, 0xF1, 0xF2, 0xF0, 0xF3, 0xF7,
                0xFA, 0xF0 }, c._testGetPcSpAFBCDEHL());
        
        b.write(3, Opcode.LD_A_B.encoding);
        b.write(4, Opcode.LD_B_D.encoding);
        b.write(5, Opcode.LD_E_H.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 6);
        assertArrayEquals(new int[] { 6, 0, 0xF2, 0xF1, 0xF3, 0xF0, 0xF3, 0xFA,
                0xFA, 0xF0 }, c._testGetPcSpAFBCDEHL());
        
        b.write(6, Opcode.LD_H_L.encoding);
        b.write(7, Opcode.LD_D_E.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 8);
        assertArrayEquals(new int[] { 8, 0, 0xF2, 0xF1, 0xF3, 0xF0, 0xFA, 0xFA,
                0xF0, 0xF0 }, c._testGetPcSpAFBCDEHL());
    }

}
