package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import ch.epfl.gameboj.AddressMap;
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
    void LD_A_HLRU_DecrementWorksForValidValue() {
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
    void LD_A_HLRU_IncrementWorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0xFAF5, 34);
        b.write(0, Opcode.LD_A_HLRI.encoding);

        cycleCpu(c, Opcode.LD_A_HLRI.cycles);
        assertArrayEquals(new int[] { 1, 0, 34, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 + 1 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_N8R_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(1, 0x55);
        b.write(AddressMap.REGS_START + 0x55, 0x23);
        b.write(0, Opcode.LD_A_N8R.encoding);

        cycleCpu(c, Opcode.LD_A_N8R.cycles);
        assertArrayEquals(new int[] { 2, 0, 0x23, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_CR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_A_CR.encoding);
        b.write(AddressMap.REGS_START + 0xF4, 0x25);
        
        cycleCpu(c, Opcode.LD_A_CR.cycles);
        assertArrayEquals(new int[] { 1, 0, 0x25, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void LD_A_N16R_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_A_N16R.encoding);
        b.write(1, 0xDA);
        b.write(2, 0xFA);
        b.write(0xFADA, 0x2);
        
        cycleCpu(c, Opcode.LD_A_N16R.cycles);
        assertArrayEquals(new int[] { 3, 0, 0x2, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void LD_A_BCR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_A_BCR.encoding);
        b.write(0xF2F4, 0xCE);
        
        cycleCpu(c, Opcode.LD_A_BCR.cycles);
        
        
    }
}
