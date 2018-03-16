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

    private void cycleCpu(Cpu cpu, long start, long cycles) {
        for (long c = start; c < start + cycles; ++c) {
            cpu.cycle(c);
        }
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
    void LD_R8_HLRWorksForValidValues() {
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
    void LD_A_HLRU_WorksForValidValues() {
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
    void PUSH_R16_WorksForValidValues() {
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
    void LD_SP_HLWorksForValidValues() {

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
    void LD_HLR_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_HLR_B.encoding);
        cycleCpu(c, Opcode.LD_HLR_B.cycles);
        assertEquals(0xF2, b.read(0xFAF5));

        b.write(1, Opcode.LD_HLR_A.encoding);
        cycleCpu(c, Opcode.LD_HLR_A.cycles * 2);
        assertEquals(0xF0, b.read(0xFAF5));

        b.write(2, Opcode.LD_HLR_H.encoding);
        cycleCpu(c, Opcode.LD_HLR_H.cycles * 3);
        assertEquals(0xFA, b.read(0xFAF5));
    }

    @Test
    void LD_HLRU_A_IncrementWorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_HLRI_A.encoding);
        cycleCpu(c, Opcode.LD_HLRI_A.cycles);
        assertEquals(0xF0, b.read(0xFAF5));
        assertArrayEquals(new int[] { 1, 0, 0xF0, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF6 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLRU_A_DecrementWorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_HLRD_A.encoding);
        cycleCpu(c, Opcode.LD_HLRD_A.cycles);
        assertEquals(0xF0, b.read(0xFAF5));
        assertArrayEquals(new int[] { 1, 0, 0xF0, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF4 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_N8R_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_N8R_A.encoding);
        b.write(1, 0x45);
        cycleCpu(c, Opcode.LD_N8R_A.cycles);
        assertEquals(0xF0, b.read(AddressMap.REGS_START + 0x45));
    }

    @Test
    void LD_CR_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_CR_A.encoding);
        cycleCpu(c, Opcode.LD_CR_A.cycles);
        assertEquals(0xF0, b.read(AddressMap.REGS_START + 0xF4));
    }

    @Test
    void LD_N16R_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_N16R_A.encoding);
        b.write(1, 0x50);
        b.write(2, 0x91);
        cycleCpu(c, Opcode.LD_N16R_A.cycles);
        assertEquals(0xF0, b.read(0x9150));
    }

    @Test
    void LS_R8_R8_WorksForValidValue() {
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

    @Test
    void LD_N16R_SP_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_N16R_SP.encoding);
        b.write(1, 0x67);
        b.write(2, 0x22);
        cycleCpu(c, Opcode.LD_N16R_SP.cycles);
        assertEquals(0, b.read(0x2267));
        assertEquals(0, b.read(0x2267 + 1));
    }

    @Test
    void LD_HLR_N8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_HLR_N8.encoding);
        b.write(1, 0xB3);
        cycleCpu(c, Opcode.LD_HLR_N8.cycles);
        assertEquals(0xB3, b.read(0xFAF5));
    }

    @Test
    void LD_DER_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC5);
        b.write(2, Opcode.LD_DER_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.LD_DER_A.cycles);

        assertArrayEquals(new int[] { 3, 0, 0xC5, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xC5, b.read(0xF3F7));
    }

    @Test
    void LD_BCR_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x8);
        b.write(2, Opcode.LD_BCR_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.LD_BCR_A.cycles);

        assertArrayEquals(new int[] { 3, 0, 0x8, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0x8, b.read(0xF2F4));
    }

    @Test
    void ADD_A_N8_WorksForValudValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 49);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, 242);

        cycleCpu(c, 4);
        assertArrayEquals(
                new int[] { 4, 0, 35, 0b0001 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(4, Opcode.ADC_A_N8.encoding);
        b.write(5, 12);
        cycleCpu(c, 4, 2);
        assertArrayEquals(
                new int[] { 6, 0, 48, 0b0010 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void ADD_A_HLR_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 49);
        b.write(2, Opcode.LD_HL_N16.encoding);
        b.write(3, 0x8C);
        b.write(4, 0xAB);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 214);
        b.write(7, Opcode.ADD_A_HLR.encoding);

        cycleCpu(c, 9);
        assertArrayEquals(
                new int[] { 8, 0, 7, 0b001 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xAB, 0x8C },
                c._testGetPcSpAFBCDEHL());
        
        b.write(8, Opcode.LD_HLR_N8.encoding);
        b.write(9, 253);
        b.write(10, Opcode.ADC_A_HLR.encoding);
        cycleCpu(c, 9, 5);
        assertArrayEquals(
                new int[] { 11, 0, 5, 0b0011 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xAB, 0x8C },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_R8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 52);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 148);
        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);
        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 255);
        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 12);
        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 3);
        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 224);

        b.write(14, Opcode.ADD_A_A.encoding);

        cycleCpu(c, Opcode.LD_A_N8.cycles * 7 + Opcode.ADD_A_A.cycles);
        assertArrayEquals(new int[] { 15, 0, 104, 0, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(15, Opcode.LD_A_N8.encoding);
        b.write(16, 52);
        b.write(17, Opcode.ADD_A_B.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 8 + Opcode.ADD_A_A.cycles * 2);
        assertArrayEquals(new int[] { 18, 0, 200, 0, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(18, Opcode.LD_A_N8.encoding);
        b.write(19, 52);
        b.write(20, Opcode.ADD_A_C.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 9 + Opcode.ADD_A_A.cycles * 3);
        assertArrayEquals(new int[] { 21, 0, 52, 0, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(21, Opcode.LD_A_N8.encoding);
        b.write(22, 52);
        b.write(23, Opcode.ADD_A_D.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 10 + Opcode.ADD_A_A.cycles * 4);
        assertArrayEquals(
                new int[] { 24, 0, 51, 0b0011 << 4, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(24, Opcode.LD_A_N8.encoding);
        b.write(25, 52);
        b.write(26, Opcode.ADD_A_E.encoding);
        cycleCpu(c, 24, 3);
        assertArrayEquals(
                new int[] { 27, 0, 64, 0b0010 << 4, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(27, Opcode.LD_A_N8.encoding);
        b.write(28, 52);
        b.write(29, Opcode.ADD_A_H.encoding);
        cycleCpu(c, 27, 3);
        assertArrayEquals(new int[] { 30, 0, 55, 0, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(30, Opcode.LD_A_N8.encoding);
        b.write(31, 52);
        b.write(32, Opcode.ADD_A_L.encoding);
        cycleCpu(c, 30, 3);
        assertArrayEquals(
                new int[] { 33, 0, 20, 0b0001 << 4, 148, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(33, Opcode.LD_B_N8.encoding);
        b.write(34, 204);
        b.write(35, Opcode.LD_A_N8.encoding);
        b.write(36, 52);
        b.write(37, Opcode.ADD_A_B.encoding);
        cycleCpu(c, 34, 5);
        assertArrayEquals(
                new int[] { 39, 0, 0, 0b1011 << 4, 204, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());

        b.write(39, Opcode.LD_A_N8.encoding);
        b.write(40, 45);
        b.write(41, Opcode.ADC_A_A.encoding);
        cycleCpu(c, 39, 3);
        assertArrayEquals(
                new int[] { 42, 0, 91, 0b010 << 4, 204, 0, 255, 12, 3, 224 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void INC_R8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xFF);
        
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);
        
        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 47);
        
        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 89);
        
        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 1);
        
        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 15);
        
        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 2);
        
        b.write(14, Opcode.INC_A.encoding);
        cycleCpu(c, 15);
        assertArrayEquals(
                new int[] { 15, 0, 0, 0b1011 << 4, 0, 47, 89, 1, 15, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(15, Opcode.INC_B.encoding);
        cycleCpu(c, 15, 1);
        assertArrayEquals(
                new int[] { 16, 0, 0, 0b0001 << 4, 1, 47, 89, 1, 15, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(16, Opcode.INC_C.encoding);
        cycleCpu(c, 16, 1);
        assertArrayEquals(
                new int[] { 17, 0, 0, 0b0011 << 4, 1, 48, 89, 1, 15, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(17, Opcode.INC_E.encoding);
        cycleCpu(c, 17, 1);
        assertArrayEquals(
                new int[] { 18, 0, 0, 0b0001 << 4, 1, 48, 89, 2, 15, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(18, Opcode.INC_D.encoding);
        cycleCpu(c, 18, 1);
        assertArrayEquals(
                new int[] { 19, 0, 0, 0b0001 << 4, 1, 48, 90, 2, 15, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(19, Opcode.INC_H.encoding);
        cycleCpu(c, 19, 1);
        assertArrayEquals(
                new int[] { 20, 0, 0, 0b0011 << 4, 1, 48, 90, 2, 16, 2 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(20, Opcode.INC_L.encoding);
        cycleCpu(c, 20, 1);
        assertArrayEquals(
                new int[] { 21, 0, 0, 0b0001 << 4, 1, 48, 90, 2, 16, 3 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_HLR_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0xFAF5, 0xFF);
        b.write(0, Opcode.INC_HLR.encoding);
        cycleCpu(c, 3);
        assertArrayEquals(
                new int[] { 1, 0, 0xF0, 0b1011 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 0);
        
        b.write(1, Opcode.INC_HLR.encoding);
        cycleCpu(c, 3, 3);
        assertArrayEquals(
                new int[] { 2, 0, 0xF0, 0b0001 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 1);
        
        b.write(0xFAF5, 15);
        b.write(2, Opcode.INC_HLR.encoding);
        cycleCpu(c, 6, 3);
        assertArrayEquals(
                new int[] { 3, 0, 0xF0, 0b0011 << 4, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 16);
    }

    @Test
    void INC_R16SP_WorksForValidValues() {
        
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0xFF);
        b.write(2, 0xFF);
        
        b.write(3, Opcode.LD_DE_N16.encoding);
        b.write(4, 0);
        b.write(5, 0);
        
        b.write(6, Opcode.LD_HL_N16.encoding);
        b.write(7, 15);
        b.write(8, 0);
        
        b.write(9, Opcode.LD_SP_N16.encoding);
        b.write(10, 0xFF);
        b.write(11, 0);
        
        b.write(12, Opcode.INC_BC.encoding);
        b.write(13, Opcode.INC_DE.encoding);
        b.write(14, Opcode.INC_HL.encoding);
        b.write(15, Opcode.INC_SP.encoding);
  
        
        int totalCycles1 = Opcode.LD_BC_N16.cycles * 4 + Opcode.INC_BC.cycles * 4;
        cycleCpu(c, totalCycles1);
        assertArrayEquals(
                new int[] { 16, 0x100, 0xF0, 0xF1, 0, 0, 0, 1, 0, 0x10 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(16, Opcode.INC_HL.encoding);
        cycleCpu(c, totalCycles1, 2);
        assertArrayEquals(
                new int[] { 17, 0x100, 0xF0, 0xF1, 0, 0, 0, 1, 0, 0x11 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(17, Opcode.LD_A_N8.encoding);
        b.write(18, 58);
        b.write(19, Opcode.ADD_A_A.encoding);
        b.write(20, Opcode.INC_HL.encoding);
        int totalCycles2 = Opcode.LD_A_N8.cycles + Opcode.ADD_A_A.cycles + Opcode.INC_HL.cycles;
        cycleCpu(c, totalCycles1 + 2, totalCycles2);
        assertArrayEquals(
                new int[] { 21, 0x100, 116, 0b0010 << 4, 0, 0, 0, 1, 0, 0x12 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(21, Opcode.LD_HL_N16.encoding);
        b.write(22, 254);
        b.write(23, 210);
        b.write(24, Opcode.INC_HL.encoding);
        cycleCpu(c, totalCycles1 + totalCycles2 + 2, 5);
        assertArrayEquals(
                new int[] { 25, 0x100, 116, 0b0010 << 4, 0, 0, 0, 1, 210, 255 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(25, Opcode.INC_HL.encoding);
        cycleCpu(c, totalCycles1 + totalCycles2 + 7, 2);
        assertArrayEquals(
                new int[] { 26, 0x100, 116, 0b0010 << 4, 0, 0, 0, 1, 211, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void ADD_HL_R16SP_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 20);
        b.write(2, 0);
        
        b.write(3, Opcode.LD_DE_N16.encoding);
        b.write(4, 228);
        b.write(5, 10);
        
        b.write(6, Opcode.LD_HL_N16.encoding);
        b.write(7, 15);
        b.write(8, 0);
        
        b.write(9, Opcode.LD_SP_N16.encoding);
        b.write(10, 64);
        b.write(11, 76);
        
        int cyclesLD = 4 * Opcode.LD_BC_N16.cycles;
        
        b.write(12, Opcode.ADD_HL_BC.encoding);
        
        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles);
        assertArrayEquals(
                new int[] { 13, 19520, 0xF0, 0b1000 << 4, 0, 20, 10, 228, 0, 35 },
                c._testGetPcSpAFBCDEHL());
        
        //maintenant HL = 35
        
        b.write(13, Opcode.ADD_HL_DE.encoding);
        
        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles, Opcode.ADD_HL_BC.cycles );
        assertArrayEquals(
                new int[] { 14, 19520, 0xF0, 0b1000 << 4, 0, 20, 10, 228, 11, 7 },
                c._testGetPcSpAFBCDEHL());
        
        //maintenant HL = 2823
        
        b.write(14, Opcode.ADD_HL_SP.encoding);
        
        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles * 2, Opcode.ADD_HL_SP.cycles);
        assertArrayEquals(
                new int[] { 15, 19520, 0xF0, 0b1010 << 4, 0, 20, 10, 228, 87, 71 },
                c._testGetPcSpAFBCDEHL());
        
        //maintenant HL = 22343
        b.write(15, Opcode.ADD_HL_HL.encoding);
        b.write(16, Opcode.ADD_HL_HL.encoding);
        
        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles * 3, Opcode.ADD_HL_BC.cycles * 4);
        assertArrayEquals(
                new int[] { 21, 19520, 0xF0, 0b1011 << 4, 0, 20, 10, 228, 93, 28 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void LD_HLSP_S8_WorksForValidValues() {
        
    }
    
    @Test
    void SUB_A_R8_WorksForValidValues() {
        
    }
    
    @Test
    void SUB_A_N8_WorksForValidValues() {
        
    }

    @Test
    void SUB_A_HLR_WorksForValidValues() {
        
    }
    
    @Test
    void DEC_R8_WorksForValidValues() {
        
    }
    
    @Test
    void DEC_HLR_VowrksForValidValues() {
        
    }
    
    @Test
    void CP_A_R8_WorksForValidValues() {
        
    }
    
    @Test
    void CP_A_N8_WorksForValidValues() {
        
    }
    
    @Test
    void CP_A_HLR_WorksForValidValues() {
        
    }
    
    @Test
    void DEC_R16_SP_WorksForValidValues() {
        
    }
    
    @Test
    void AND_A_N8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x23);
        b.write(2, Opcode.AND_A_N8.encoding);
        b.write(3, 0xDC);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.AND_A_N8.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0, 0xA0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0x55);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0xAC);
        b.write(6, Opcode.AND_A_B.encoding);
        b.write(7, Opcode.AND_A_L.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 3 + Opcode.AND_A_B.cycles * 2);

        assertArrayEquals(
                new int[] { 8, 0, 0, 0xA0, 0x55, 0xF4, 0xF3, 0xF7, 0xFA, 0xAC },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, Opcode.AND_A_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.AND_A_HLR.cycles);

        assertArrayEquals(new int[] { 8, 0, 0xA4, 0x20, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0x55);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0x1C);
        b.write(6, Opcode.OR_A_B.encoding);
        b.write(7, Opcode.OR_A_L.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 3 + Opcode.OR_A_B.cycles * 2);

        assertArrayEquals(
                new int[] { 8, 0, 0x7F, 0, 0x55, 0xF4, 0xF3, 0xF7, 0xFA, 0x1C },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_R8_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);
        b.write(4, Opcode.OR_A_B.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 2 + Opcode.OR_A_B.cycles);

        assertArrayEquals(
                new int[] { 5, 0, 0, 0x80, 0, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_N8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.OR_A_N8.encoding);
        b.write(3, 0x55);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.OR_A_N8.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0x7F, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }
}
