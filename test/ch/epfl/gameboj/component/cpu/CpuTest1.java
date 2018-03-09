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
    void LD_N16R_SPWorksFine() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_N16R_SP.encoding);
        b.write(1, 0x67);
        b.write(2, 0x22);
        cycleCpu(c, Opcode.LD_N16R_SP.cycles);
        assertEquals(0, b.read(1));
        assertEquals(0, b.read(2));
        
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

        cycleCpu(c, Opcode.LD_B_HLR.cycles * 5);

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

        cycleCpu(c, Opcode.PUSH_AF.cycles * 4);
        assertArrayEquals(new int[] { 4, 0x10000 - 8, 0xF0, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_SP_HLWorksFine() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_SP_HL.encoding);
        cycleCpu(c, Opcode.LD_SP_HL.cycles);
        assertArrayEquals(new int[] { 1, 0xFAF5, 0xF0, 0xF1, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
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
        assertArrayEquals(new int[] { 1, 0, 0xCE, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_DER_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_DER.encoding);
        b.write(0xF3F7, 0xAD);

        cycleCpu(c, Opcode.LD_A_DER.cycles);
        assertArrayEquals(new int[] { 1, 0, 0xAD, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_N8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x00);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0x01);
        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0x10);
        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0x11);
        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0x12);
        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0x31);
        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0x30);

        cycleCpu(c, Opcode.LD_A_N8.cycles * 7);
        assertArrayEquals(new int[] { 14, 0, 0, 0xF1, 0x01, 0x10, 0x11, 0x12,
                0x31, 0x30 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R16SP_N16_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0xAA);
        b.write(2, 0xBB);
        b.write(3, Opcode.LD_DE_N16.encoding);
        b.write(4, 0xCC);
        b.write(5, 0xDD);
        b.write(6, Opcode.LD_HL_N16.encoding);
        b.write(7, 0xEE);
        b.write(8, 0xFF);
        b.write(9, Opcode.LD_SP_N16.encoding);
        b.write(10, 0x22);
        b.write(11, 0x33);

        cycleCpu(c, Opcode.LD_BC_N16.cycles * 4);
        assertArrayEquals(new int[] { 12, 0x3322, 0xF0, 0xF1, 0xBB, 0xAA, 0xDD,
                0xCC, 0xFF, 0xEE }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void POP_R16_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xFE);
        b.write(2, 0xFF);
        b.write(0xFFFE, 0x88);
        b.write(3, Opcode.POP_AF.encoding);
        b.write(4, Opcode.POP_BC.encoding);
        b.write(5, Opcode.POP_DE.encoding);
        b.write(6, Opcode.POP_HL.encoding);

        cycleCpu(c, Opcode.POP_AF.cycles * 5);
        assertArrayEquals(new int[] { 7, 6, 0xFF, 0x80, 0xFE, 0x31, 0xF1, 0xFF,
                0xD1, 0xC1 }, c._testGetPcSpAFBCDEHL());

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
