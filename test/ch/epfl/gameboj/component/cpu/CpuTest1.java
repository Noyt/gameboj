package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest1 {

    private enum RegList implements Register {
        PC, SP, A, F, B, C, D, E, H, L
    }

    private byte[] Fib = new byte[] { (byte) 0x31, (byte) 0xFF, (byte) 0xFF,
            (byte) 0x3E, (byte) 0x0B, (byte) 0xCD, (byte) 0x0A, (byte) 0x00,
            (byte) 0x76, (byte) 0x00, (byte) 0xFE, (byte) 0x02, (byte) 0xD8,
            (byte) 0xC5, (byte) 0x3D, (byte) 0x47, (byte) 0xCD, (byte) 0x0A,
            (byte) 0x00, (byte) 0x4F, (byte) 0x78, (byte) 0x3D, (byte) 0xCD,
            (byte) 0x0A, (byte) 0x00, (byte) 0x81, (byte) 0xC1, (byte) 0xC9 };

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

    private void assertRegisterValue(RegList reg, Cpu c, int v) {
        assertEquals(v, c._testGetPcSpAFBCDEHL()[reg.index()]);
    }

    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        c.initializeRegisters();
        
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
        
        c.initializeRegisters();

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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xFE);
        b.write(2, 0xFF);
        b.write(0xFFFE, 0x88);
        b.write(3, Opcode.POP_AF.encoding);
        b.write(4, Opcode.POP_BC.encoding);
        b.write(5, Opcode.POP_DE.encoding);
        b.write(6, Opcode.POP_HL.encoding);

        cycleCpu(c, Opcode.POP_AF.cycles * 5);
        assertArrayEquals(
                new int[] { 7, 6, 0, 0x80, 0xFE, 0x31, 0xF1, 0xFF, 0xD1, 0xC1 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLR_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_CR_A.encoding);
        cycleCpu(c, Opcode.LD_CR_A.cycles);
        assertEquals(0xF0, b.read(AddressMap.REGS_START + 0xF4));
    }

    @Test
    void LD_N16R_A_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 49);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, 242);

        cycleCpu(c, 4);
        assertArrayEquals(new int[] { 4, 0, 35, 0b0001 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(4, Opcode.ADC_A_N8.encoding);
        b.write(5, 12);
        cycleCpu(c, 4, 2);
        assertArrayEquals(new int[] { 6, 0, 48, 0b0010 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_HLR_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 49);
        b.write(2, Opcode.LD_HL_N16.encoding);
        b.write(3, 0x8C);
        b.write(4, 0xAB);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 214);
        b.write(7, Opcode.ADD_A_HLR.encoding);

        cycleCpu(c, 9);
        assertArrayEquals(new int[] { 8, 0, 7, 0b001 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xAB, 0x8C }, c._testGetPcSpAFBCDEHL());

        b.write(8, Opcode.LD_HLR_N8.encoding);
        b.write(9, 253);
        b.write(10, Opcode.ADC_A_HLR.encoding);
        cycleCpu(c, 9, 5);
        assertArrayEquals(new int[] { 11, 0, 5, 0b0011 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xAB, 0x8C }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_R8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
        b.write(0xFAF5, 0xFF);
        b.write(0, Opcode.INC_HLR.encoding);
        cycleCpu(c, 3);
        assertArrayEquals(new int[] { 1, 0, 0xF0, 0b1011 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 0);

        b.write(1, Opcode.INC_HLR.encoding);
        cycleCpu(c, 3, 3);
        assertArrayEquals(new int[] { 2, 0, 0xF0, 0b0001 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 1);

        b.write(0xFAF5, 15);
        b.write(2, Opcode.INC_HLR.encoding);
        cycleCpu(c, 6, 3);
        assertArrayEquals(new int[] { 3, 0, 0xF0, 0b0011 << 4, 0xF2, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xFAF5), 16);
    }

    @Test
    void INC_R16SP_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        int totalCycles1 = Opcode.LD_BC_N16.cycles * 4
                + Opcode.INC_BC.cycles * 4;
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
        int totalCycles2 = Opcode.LD_A_N8.cycles + Opcode.ADD_A_A.cycles
                + Opcode.INC_HL.cycles;
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

        c.initializeRegisters();
        
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
        assertArrayEquals(new int[] { 13, 19520, 0xF0, 0b1000 << 4, 0, 20, 10,
                228, 0, 35 }, c._testGetPcSpAFBCDEHL());

        // maintenant HL = 35

        b.write(13, Opcode.ADD_HL_DE.encoding);

        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles,
                Opcode.ADD_HL_BC.cycles);
        assertArrayEquals(new int[] { 14, 19520, 0xF0, 0b1000 << 4, 0, 20, 10,
                228, 11, 7 }, c._testGetPcSpAFBCDEHL());

        // maintenant HL = 2823

        b.write(14, Opcode.ADD_HL_SP.encoding);

        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles * 2,
                Opcode.ADD_HL_SP.cycles);
        assertArrayEquals(new int[] { 15, 19520, 0xF0, 0b1010 << 4, 0, 20, 10,
                228, 87, 71 }, c._testGetPcSpAFBCDEHL());

        // maintenant HL = 22343
        b.write(15, Opcode.ADD_HL_HL.encoding);
        b.write(16, Opcode.ADD_HL_HL.encoding);

        cycleCpu(c, cyclesLD + Opcode.ADD_HL_BC.cycles * 3,
                Opcode.ADD_HL_BC.cycles * 4);
        assertArrayEquals(new int[] { 21, 19520, 0xF0, 0b1011 << 4, 0, 20, 10,
                228, 93, 28 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLSP_S8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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
        cycleCpu(c, 10);

        b.write(13, Opcode.LD_SP_N16.encoding);
        b.write(14, 0b1001000);
        b.write(15, 0b1111011);
        cycleCpu(c, 13);

        b.write(16, Opcode.LD_HL_SP_N8.encoding);
        b.write(17, 92);
        cycleCpu(c, 16, 3);
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

    @Test
    void SUB_A_R8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0);

        cycleCpu(c, 14);

        b.write(14, Opcode.SUB_A_A.encoding);
        cycleCpu(c, 14, 1);

        assertArrayEquals(new int[] { 15, 0, 0, 0b1100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(15, Opcode.LD_B_N8.encoding);
        b.write(16, 5);
        b.write(17, Opcode.SUB_A_B.encoding);
        cycleCpu(c, 15, 3);
        assertArrayEquals(
                new int[] { 18, 0, 251, 0b0111 << 4, 5, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(18, Opcode.LD_C_N8.encoding);
        b.write(19, 3);
        b.write(20, Opcode.SBC_A_C.encoding);
        cycleCpu(c, 18, 3);
        assertArrayEquals(
                new int[] { 21, 0, 247, 0b0100 << 4, 5, 3, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(21, Opcode.LD_D_N8.encoding);
        b.write(22, 94);
        b.write(23, Opcode.SBC_A_D.encoding);
        cycleCpu(c, 21, 3);
        assertArrayEquals(
                new int[] { 24, 0, 153, 0b0110 << 4, 5, 3, 94, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(24, Opcode.LD_E_N8.encoding);
        b.write(25, 77);
        b.write(26, Opcode.SBC_A_E.encoding);
        cycleCpu(c, 24, 3);
        assertArrayEquals(
                new int[] { 27, 0, 76, 0b0110 << 4, 5, 3, 94, 77, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(27, Opcode.LD_H_N8.encoding);
        b.write(28, 146);
        b.write(29, Opcode.SBC_A_H.encoding);
        cycleCpu(c, 27, 3);
        assertArrayEquals(
                new int[] { 30, 0, 186, 0b0101 << 4, 5, 3, 94, 77, 146, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(30, Opcode.LD_L_N8.encoding);
        b.write(31, 193);
        b.write(32, Opcode.SUB_A_L.encoding);
        cycleCpu(c, 30, 3);
        assertArrayEquals(
                new int[] { 33, 0, 249, 0b0101 << 4, 5, 3, 94, 77, 146, 193 },
                c._testGetPcSpAFBCDEHL());

        b.write(33, Opcode.SBC_A_A.encoding);
        cycleCpu(c, 33, 1);
        assertArrayEquals(
                new int[] { 34, 0, 0xFF, 0b0111 << 4, 5, 3, 94, 77, 146, 193 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_N8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 13);

        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);

        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);

        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0);

        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0);

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0);

        cycleCpu(c, 14);
        assertArrayEquals(new int[] { 14, 0, 13, 0xF1, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(14, Opcode.SUB_A_N8.encoding);
        b.write(15, 13);
        cycleCpu(c, 14, 2);
        assertArrayEquals(new int[] { 16, 0, 0, 0b1100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(16, Opcode.SUB_A_N8.encoding);
        b.write(17, 51);
        cycleCpu(c, 16, 2);
        assertArrayEquals(
                new int[] { 18, 0, 205, 0b0111 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(18, Opcode.SBC_A_N8.encoding);
        b.write(19, 14);
        cycleCpu(c, 18, 2);
        assertArrayEquals(
                new int[] { 20, 0, 190, 0b0110 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(20, Opcode.SBC_A_N8.encoding);
        b.write(21, 198);
        cycleCpu(c, 20, 2);
        assertArrayEquals(
                new int[] { 22, 0, 248, 0b0101 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(23, Opcode.SUB_A_N8.encoding);
        b.write(24, 69);
        cycleCpu(c, 22, 2);
        assertArrayEquals(
                new int[] { 25, 0, 179, 0b0100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_HLR_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 13);

        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);

        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);

        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0);

        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0);

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0xF0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0xF0);

        cycleCpu(c, 14);
        assertArrayEquals(new int[] { 14, 0, 13, 0xF1, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());

        b.write(14, Opcode.LD_HLR_N8.encoding);
        b.write(15, 13);
        b.write(16, Opcode.SUB_A_HLR.encoding);
        cycleCpu(c, 14, 5);
        assertArrayEquals(
                new int[] { 17, 0, 0, 0b1100 << 4, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());

        b.write(17, Opcode.LD_HLR_N8.encoding);
        b.write(18, 51);
        b.write(19, Opcode.SUB_A_HLR.encoding);
        cycleCpu(c, 19, 5);
        assertArrayEquals(
                new int[] { 20, 0, 205, 0b0111 << 4, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());

        b.write(20, Opcode.LD_HLR_N8.encoding);
        b.write(21, 14);
        b.write(22, Opcode.SBC_A_HLR.encoding);
        cycleCpu(c, 24, 5);
        assertArrayEquals(
                new int[] { 23, 0, 190, 0b0110 << 4, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());

        b.write(23, Opcode.LD_HLR_N8.encoding);
        b.write(24, 198);
        b.write(25, Opcode.SBC_A_HLR.encoding);
        cycleCpu(c, 29, 5);
        assertArrayEquals(
                new int[] { 26, 0, 248, 0b0101 << 4, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());

        b.write(26, Opcode.LD_HLR_N8.encoding);
        b.write(27, 69);
        b.write(28, Opcode.SUB_A_HLR.encoding);
        cycleCpu(c, 34, 5);
        assertArrayEquals(
                new int[] { 29, 0, 179, 0b0100 << 4, 0, 0, 0, 0, 0xF0, 0xF0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_R8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0);

        cycleCpu(c, 14);

        b.write(14, Opcode.CP_A_A.encoding);
        cycleCpu(c, 14, 1);

        assertArrayEquals(new int[] { 15, 0, 0, 0b1100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(15, Opcode.LD_B_N8.encoding);
        b.write(16, 5);
        b.write(17, Opcode.CP_A_B.encoding);
        cycleCpu(c, 15, 3);
        assertArrayEquals(new int[] { 18, 0, 0, 0b0111 << 4, 5, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        // A = 251
        b.write(18, Opcode.LD_A_N8.encoding);
        b.write(19, 251);
        b.write(20, Opcode.LD_C_N8.encoding);
        b.write(21, 3);
        b.write(22, Opcode.CP_A_C.encoding);
        cycleCpu(c, 18, 5);
        assertArrayEquals(
                new int[] { 23, 0, 251, 0b0100 << 4, 5, 3, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        // A = 247
        b.write(23, Opcode.LD_A_N8.encoding);
        b.write(24, 247);
        b.write(25, Opcode.LD_D_N8.encoding);
        b.write(26, 94);
        b.write(27, Opcode.CP_A_D.encoding);
        cycleCpu(c, 23, 5);
        assertArrayEquals(
                new int[] { 28, 0, 247, 0b0110 << 4, 5, 3, 94, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        // A = 153
        b.write(28, Opcode.LD_A_N8.encoding);
        b.write(29, 153);
        b.write(30, Opcode.LD_E_N8.encoding);
        b.write(31, 77);
        b.write(32, Opcode.CP_A_E.encoding);
        cycleCpu(c, 28, 5);
        assertArrayEquals(
                new int[] { 33, 0, 153, 0b0110 << 4, 5, 3, 94, 77, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        // A = 76
        b.write(33, Opcode.LD_A_N8.encoding);
        b.write(34, 76);
        b.write(35, Opcode.LD_H_N8.encoding);
        b.write(36, 146);
        b.write(37, Opcode.CP_A_H.encoding);
        cycleCpu(c, 33, 5);
        assertArrayEquals(
                new int[] { 38, 0, 76, 0b0101 << 4, 5, 3, 94, 77, 146, 0 },
                c._testGetPcSpAFBCDEHL());

        // A = 186
        b.write(38, Opcode.LD_A_N8.encoding);
        b.write(39, 186);
        b.write(40, Opcode.LD_L_N8.encoding);
        b.write(41, 193);
        b.write(42, Opcode.CP_A_L.encoding);
        cycleCpu(c, 38, 5);
        assertArrayEquals(
                new int[] { 43, 0, 186, 0b0101 << 4, 5, 3, 94, 77, 146, 193 },
                c._testGetPcSpAFBCDEHL());

        // A = 43
        b.write(43, Opcode.LD_A_N8.encoding);
        b.write(44, 43);
        b.write(45, Opcode.LD_B_N8.encoding);
        b.write(46, 11);
        b.write(47, Opcode.CP_A_B.encoding);
        cycleCpu(c, 43, 5);
        assertArrayEquals(
                new int[] { 48, 0, 43, 0b0100 << 4, 11, 3, 94, 77, 146, 193 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_N8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 13);

        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);

        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);

        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0);

        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0);

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0);

        cycleCpu(c, 14);
        assertArrayEquals(new int[] { 14, 0, 13, 0xF1, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(14, Opcode.CP_A_N8.encoding);
        b.write(15, 13);
        cycleCpu(c, 14, 2);
        assertArrayEquals(
                new int[] { 16, 0, 13, 0b1100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(16, Opcode.CP_A_N8.encoding);
        b.write(17, 14);
        cycleCpu(c, 16, 2);
        assertArrayEquals(
                new int[] { 18, 0, 13, 0b0111 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void CP_A_HLR_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 13);

        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0);

        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, 0);

        b.write(6, Opcode.LD_D_N8.encoding);
        b.write(7, 0);

        b.write(8, Opcode.LD_E_N8.encoding);
        b.write(9, 0);

        b.write(10, Opcode.LD_H_N8.encoding);
        b.write(11, 0);

        b.write(12, Opcode.LD_L_N8.encoding);
        b.write(13, 0);

        cycleCpu(c, 14);
        assertArrayEquals(new int[] { 14, 0, 13, 0xF1, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(14, Opcode.LD_HLR_N8.encoding);
        b.write(15, 13);
        b.write(16, Opcode.CP_A_HLR.encoding);
        cycleCpu(c, 14, 5);
        assertArrayEquals(
                new int[] { 17, 0, 13, 0b1100 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(17, Opcode.LD_HLR_N8.encoding);
        b.write(18, 14);
        b.write(19, Opcode.CP_A_HLR.encoding);
        cycleCpu(c, 19, 5);
        assertArrayEquals(
                new int[] { 20, 0, 13, 0b0111 << 4, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_N8_WorksForValidValues() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
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

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.OR_A_N8.encoding);
        b.write(3, 0x55);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.OR_A_N8.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0x7F, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, Opcode.OR_A_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.OR_A_HLR.cycles);

        assertArrayEquals(
                new int[] { 8, 0, 0xE5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_N8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.XOR_A_N8.encoding);
        b.write(3, 0x55);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.XOR_A_N8.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0x6E, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0x55);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0x1C);
        b.write(6, Opcode.XOR_A_B.encoding);
        b.write(7, Opcode.XOR_A_L.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 3 + Opcode.XOR_A_B.cycles * 2);

        assertArrayEquals(
                new int[] { 8, 0, 0x72, 0, 0x55, 0xF4, 0xF3, 0xF7, 0xFA, 0x1C },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_R8_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x3B);
        b.write(2, Opcode.LD_B_N8.encoding);
        b.write(3, 0x55);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0x6E);
        b.write(6, Opcode.XOR_A_B.encoding);
        b.write(7, Opcode.XOR_A_L.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 3 + Opcode.XOR_A_B.cycles * 2);

        assertArrayEquals(
                new int[] { 8, 0, 0, 0x80, 0x55, 0xF4, 0xF3, 0xF7, 0xFA, 0x6E },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, Opcode.XOR_A_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.XOR_A_HLR.cycles);

        assertArrayEquals(
                new int[] { 8, 0, 0x41, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void CPL_WorksForvalidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.CPL.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.CPL.cycles);

        assertArrayEquals(new int[] { 3, 0, 0x3D, 0xF0, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, 0xCB);
        b.write(3, Opcode.SLA_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SLA_A.cycles);

        assertArrayEquals(new int[] { 4, 0, 0x84, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.SLA_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.SLA_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xC8, b.read(0xA5B7));
    }

    @Test
    void SRA_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, 0xCB);
        b.write(3, Opcode.SRA_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SRA_A.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0xE1, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRA_R8_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0);
        b.write(2, 0xCB);
        b.write(3, Opcode.SRA_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SRA_A.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0, 0x80, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRA_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.SRA_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.SRA_HLR.cycles);

        assertArrayEquals(
                new int[] { 9, 0, 0xA5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0xF2, b.read(0xA5B7));
    }

    @Test
    void SRL_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, 0xCB);
        b.write(3, Opcode.SRL_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SRL_A.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0x61, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRL_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.SRL_HLR.encoding);
        cycleCpu(c,
                Opcode.LD_A_N8.cycles + Opcode.LD_H_A.cycles
                        + Opcode.LD_L_N8.cycles + Opcode.LD_HLR_N8.cycles
                        + Opcode.SRL_HLR.cycles);

        assertArrayEquals(
                new int[] { 9, 0, 0xA5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0x72, b.read(0xA5B7));
    }

    @Test
    void ROTCA_RLCA_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.RLCA.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.RLCA.cycles);

        assertArrayEquals(new int[] { 3, 0, 0x85, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTCA_RRCA_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.RRCA.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.RRCA.cycles);

        assertArrayEquals(
                new int[] { 3, 0, 0x61, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTA_RLA_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.RLA.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.RLA.cycles);

        assertArrayEquals(new int[] { 3, 0, 0x85, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTA_RRA_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.RRA.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.RRA.cycles);

        assertArrayEquals(
                new int[] { 3, 0, 0xE1, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTC_R8_RLC_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_E_N8.encoding);
        b.write(1, 0xD4);
        b.write(2, Opcode.LD_C_N8.encoding);
        b.write(3, 0xD4);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0xD5);
        b.write(6, 0xCB);
        b.write(7, Opcode.RLC_C.encoding);
        b.write(8, 0xCB);
        b.write(9, Opcode.RLC_E.encoding);
        b.write(10, 0xCB);
        b.write(11, Opcode.RLC_L.encoding);
        cycleCpu(c, Opcode.LD_E_N8.cycles * 3 + Opcode.RLC_E.cycles * 3);

        assertArrayEquals(new int[] { 12, 0, 0xF0, 0x10, 0xF2, 0xA9, 0xF3, 0xA9,
                0xFA, 0xAB }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTC_R8_RRC_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_E_N8.encoding);
        b.write(1, 0xD4);
        b.write(2, Opcode.LD_C_N8.encoding);
        b.write(3, 0xD4);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0xD5);
        b.write(6, 0xCB);
        b.write(7, Opcode.RRC_C.encoding);
        b.write(8, 0xCB);
        b.write(9, Opcode.RRC_E.encoding);
        b.write(10, 0xCB);
        b.write(11, Opcode.RRC_L.encoding);
        cycleCpu(c, Opcode.LD_E_N8.cycles * 3 + Opcode.RRC_E.cycles * 3);

        assertArrayEquals(new int[] { 12, 0, 0xF0, 0x10, 0xF2, 0x6A, 0xF3, 0x6A,
                0xFA, 0xEA }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROT_R8_RL_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_E_N8.encoding);
        b.write(1, 0xD4);
        b.write(2, Opcode.LD_C_N8.encoding);
        b.write(3, 0xD4);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0xD5);
        b.write(6, 0xCB);
        b.write(7, Opcode.RL_C.encoding);
        b.write(8, 0xCB);
        b.write(9, Opcode.RL_E.encoding);
        b.write(10, 0xCB);
        b.write(11, Opcode.RL_L.encoding);
        cycleCpu(c, Opcode.LD_E_N8.cycles * 3 + Opcode.RL_E.cycles * 3);

        assertArrayEquals(new int[] { 12, 0, 0xF0, 0x10, 0xF2, 0xA9, 0xF3, 0xA9,
                0xFA, 0xAB }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROT_R8_RR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_E_N8.encoding);
        b.write(1, 0xD5);
        b.write(2, Opcode.LD_C_N8.encoding);
        b.write(3, 0xD5);
        b.write(4, Opcode.LD_L_N8.encoding);
        b.write(5, 0xD4);
        b.write(6, 0xCB);
        b.write(7, Opcode.RR_C.encoding);
        b.write(9, 0xCB);
        b.write(10, Opcode.RR_E.encoding);
        b.write(11, 0xCB);
        b.write(12, Opcode.RR_L.encoding);
        cycleCpu(c, Opcode.LD_E_N8.cycles * 3 + Opcode.RR_E.cycles * 3);

        assertArrayEquals(new int[] { 13, 0, 0xF0, 0, 0xF2, 0xEA, 0xF3, 0xEA,
                0xFA, 0xEA }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ROTC_HLR_RLC_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.RLC_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.RLC_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xC9, b.read(0xA5B7));
    }

    @Test
    void ROTC_HLR_RRC_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.RRC_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.RRC_HLR.cycles);

        assertArrayEquals(
                new int[] { 9, 0, 0xA5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0x72, b.read(0xA5B7));
    }

    @Test
    void ROT_HLR_RL_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.RL_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.RL_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0x10, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xC9, b.read(0xA5B7));
    }

    @Test
    void ROT_HLR_RR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.RR_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.RR_HLR.cycles);

        assertArrayEquals(
                new int[] { 9, 0, 0xA5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0xF2, b.read(0xA5B7));
    }

    @Test
    void SWAP_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, 0xCB);
        b.write(3, Opcode.SWAP_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SWAP_A.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0x5A, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SWAP_R8_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0);
        b.write(2, 0xCB);
        b.write(3, Opcode.SWAP_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SWAP_A.cycles);

        assertArrayEquals(
                new int[] { 4, 0, 0, 0x80, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SWAP_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.SWAP_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.SWAP_HLR.cycles);

        assertArrayEquals(
                new int[] { 9, 0, 0xA5, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xA5, 0xB7 },
                c._testGetPcSpAFBCDEHL());
        assertEquals(0x4E, b.read(0xA5B7));
    }

    @Test
    void SWAP_HLR_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0);
        b.write(7, 0xCB);
        b.write(8, Opcode.SWAP_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.SWAP_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0x80, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0, b.read(0xA5B7));
    }

    @Test
    void BIT_U3_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, 0xCB);
        b.write(3, Opcode.BIT_1_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.BIT_1_A.cycles);

        assertArrayEquals(new int[] { 4, 0, 0xA5, 0xB0, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void BIT_U3_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xE4);
        b.write(7, 0xCB);
        b.write(8, Opcode.BIT_5_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.BIT_5_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0x30, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SET_U3_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xD5);
        b.write(2, 0xCB);
        b.write(3, Opcode.SET_3_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.SET_3_A.cycles);

        assertArrayEquals(new int[] { 4, 0, 0xDD, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SET_U3_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xD5);
        b.write(7, 0xCB);
        b.write(8, Opcode.SET_3_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.SET_3_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xDD, b.read(0xA5B7));
    }

    @Test
    void RES_U3_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xD5);
        b.write(2, 0xCB);
        b.write(3, Opcode.RES_6_A.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.RES_6_A.cycles);

        assertArrayEquals(new int[] { 4, 0, 0x95, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void RES_U3_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xD5);
        b.write(7, 0xCB);
        b.write(8, Opcode.RES_6_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.RES_6_HLR.cycles);

        assertArrayEquals(new int[] { 9, 0, 0xA5, 0xF1, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0x95, b.read(0xA5B7));
    }

    @Test
    void DAA_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x35);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, 0x38);
        b.write(4, Opcode.DAA.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles + Opcode.ADD_A_N8.cycles
                + Opcode.DAA.cycles);

        assertArrayEquals(
                new int[] { 5, 0, 0x73, 0, 0xF2, 0xF4, 0xF3, 0xF7, 0xFA, 0xF5 },
                c._testGetPcSpAFBCDEHL());

    }

    @Test
    void SCCF_SCF_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.SCF.encoding);
        cycleCpu(c, Opcode.SCF.cycles);

        assertArrayEquals(new int[] { 1, 0, 0xF0, 0x90, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SCCF_CCF_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.CCF.encoding);
        cycleCpu(c, Opcode.CCF.cycles);

        assertArrayEquals(new int[] { 1, 0, 0xF0, 0x80, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R16SP_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0xDC);
        b.write(2, 0xAB);
        b.write(3, Opcode.DEC_BC.encoding);
        cycleCpu(c, Opcode.LD_BC_N16.cycles + Opcode.DEC_BC.cycles);

        assertArrayEquals(new int[] { 4, 0, 0xF0, 0xF1, 0xAB, 0xDB, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R16SP_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0);
        b.write(2, 0);
        b.write(3, Opcode.DEC_BC.encoding);
        cycleCpu(c, Opcode.LD_BC_N16.cycles + Opcode.DEC_BC.cycles);

        assertArrayEquals(new int[] { 4, 0, 0xF0, 0xF1, 0xFF, 0xFF, 0xF3, 0xF7,
                0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_H_N8.encoding);
        b.write(1, 0);
        b.write(2, Opcode.LD_A_N8.encoding);
        b.write(3, 0xC7);
        b.write(4, Opcode.DEC_A.encoding);
        b.write(5, Opcode.DEC_H.encoding);
        cycleCpu(c, Opcode.LD_H_N8.cycles * 2 + Opcode.DEC_A.cycles * 2);

        assertArrayEquals(new int[] { 6, 0, 0xC6, 0x70, 0xF2, 0xF4, 0xF3, 0xF7,
                0xFF, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_HLR_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0xD5);
        b.write(7, Opcode.DEC_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.DEC_HLR.cycles);

        assertArrayEquals(new int[] { 8, 0, 0xA5, 0x50, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xD4, b.read(0xA5B7));
    }

    @Test
    void DEC_HLR_WorksForValidValue0() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xA5);
        b.write(2, Opcode.LD_H_A.encoding);
        b.write(3, Opcode.LD_L_N8.encoding);
        b.write(4, 0xB7);
        b.write(5, Opcode.LD_HLR_N8.encoding);
        b.write(6, 0);
        b.write(7, Opcode.DEC_HLR.encoding);
        cycleCpu(c, Opcode.LD_A_A.cycles * 3 + Opcode.LD_HLR_N8.cycles
                + Opcode.DEC_HLR.cycles);

        assertArrayEquals(new int[] { 8, 0, 0xA5, 0x70, 0xF2, 0xF4, 0xF3, 0xF7,
                0xA5, 0xB7 }, c._testGetPcSpAFBCDEHL());
        assertEquals(0xFF, b.read(0xA5B7));
    }

    // @Test
    // void Fibonnaci_12th_Works() {
    // GameBoy g = new GameBoy(null);
    //
    // System.out.println("TESSSSSSSSSSSSSSSSSSSSSSSSSSSSST");
    // for (int i = AddressMap.WORK_RAM_START; i < Fib.length +
    // AddressMap.WORK_RAM_START; i++) {
    // g.bus().write(i, Bits.clip(Byte.SIZE, Fib[i-AddressMap.WORK_RAM_START]));
    // }
    //
    //
    // g.runUntil(6000);
    //
    // assertRegisterValue(RegList.A, g.cpu(), 89);
    // }

    @Test
    void FibOld() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        for (int i = 0; i < Fib.length; i++) {
            b.write(i, Bits.clip(Byte.SIZE, Fib[i]));
        }

        long a = 0;
        while (c._testGetPcSpAFBCDEHL()[0] != 8) {
            c.cycle(a);
            a++;
        }

        assertRegisterValue(RegList.A, c, 89);
    }

    @Test
    void JP_HL_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_H_N8.encoding);
        b.write(1, 0x23);
        b.write(2, Opcode.LD_L_N8.encoding);
        b.write(3, 0x30);
        b.write(4, Opcode.JP_HL.encoding);
        cycleCpu(c, Opcode.LD_A_N8.cycles * 2 + Opcode.JP_HL.cycles);

        assertRegisterValue(RegList.PC, c, 0x2330);

    }

    @Test
    void JP_N16_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.JP_N16.encoding);
        b.write(1, 0x23);
        b.write(2, 0x30);
        cycleCpu(c, Opcode.JP_N16.cycles);

        assertRegisterValue(RegList.PC, c, 0x3023);
    }

    @Test
    void JP_CC_N16_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.JP_C_N16.encoding);
        b.write(1, 0x23);
        b.write(2, 0x30);
        cycleCpu(c, Opcode.JP_C_N16.cycles);

        assertRegisterValue(RegList.PC, c, 0x3023);

        b.write(0x3023, Opcode.JP_Z_N16.encoding);
        b.write(0x3024, 0xAA);
        b.write(0x3025, 0xBB);
        cycleCpu(c, 3, Opcode.JP_Z_N16.cycles);

        assertRegisterValue(RegList.PC, c, 0xBBAA);

        b.write(0xBBAA, Opcode.JP_NZ_N16.encoding);
        b.write(0xBBAB, 0xCC);
        b.write(0xBBAC, 0xDD);

        cycleCpu(c, 6, Opcode.JP_NZ_N16.cycles);

        assertRegisterValue(RegList.PC, c, 0xBBAD);
    }

    @Test
    void JR_E8_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.JP_N16.encoding);
        b.write(1, 0x23);
        b.write(2, 0x30);
        b.write(0x3023, Opcode.JR_E8.encoding);
        // PC + 2 - 2
        b.write(0x3024, 0xFE);
        cycleCpu(c, Opcode.JP_N16.cycles + Opcode.JR_E8.cycles);

        assertRegisterValue(RegList.PC, c, 0x3023);

    }


    @Test
    void JR_CC_E8_WorksForValidValueSimpleCondition() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.JP_N16.encoding);
        b.write(1, 0x23);
        b.write(2, 0x30);
        b.write(0x3023, Opcode.JR_C_E8.encoding);
        b.write(0x3024, 0xFE);
        cycleCpu(c, Opcode.JP_N16.cycles + Opcode.JR_C_E8.cycles);

        assertRegisterValue(RegList.PC, c, 0x3023);
    }

    @Test
    void EDI_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.EI.encoding);
        cycleCpu(c, Opcode.EI.cycles);

        assertEquals(true, c.getIME());

        b.write(1, Opcode.DI.encoding);
        cycleCpu(c, 1, Opcode.DI.cycles);

        assertEquals(false, c.getIME());
    }

    @Test
    void RETI_WorksForValidValue() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        c.initializeRegisters();
        
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xFF);
        b.write(2, 0xFF);
        b.write(3, Opcode.CALL_N16.encoding);
        b.write(4, 0x30);
        b.write(5, 0xA6);
        b.write(0xA630, Opcode.RETI.encoding);
        cycleCpu(c, Opcode.CALL_N16.cycles + Opcode.RETI.cycles);
        
        assertEquals(true, c.getIME());
        assertRegisterValue(RegList.PC, c, 6);
    }
    
    @Test
    void RequestInterrupt_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.requestInterrupt(Interrupt.VBLANK);
        assertEquals(true, c.getIFIndex(Interrupt.VBLANK));
        
        c.requestInterrupt(Interrupt.LCD_STAT);
        assertEquals(true, c.getIFIndex(Interrupt.LCD_STAT));
        
        c.requestInterrupt(Interrupt.TIMER);
        assertEquals(true, c.getIFIndex(Interrupt.TIMER));
        
        c.requestInterrupt(Interrupt.SERIAL);
        assertEquals(true, c.getIFIndex(Interrupt.SERIAL));
        
        c.requestInterrupt(Interrupt.JOYPAD);
        assertEquals(true, c.getIFIndex(Interrupt.JOYPAD));
    }
    
    @Test
    void HALT_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.initializeRegisters();
        
        b.write(0, Opcode.JP_N16.encoding);
        b.write(1, 0x00);
        b.write(2, 0xC0);
        b.write(0xC000, Opcode.HALT.encoding);
        cycleCpu(c, Opcode.JP_N16.cycles + Opcode.HALT.cycles + 1);
        
        assertRegisterValue(RegList.PC, c, AddressMap.WORK_RAM_START + 1);
        
        c.setIE(Interrupt.VBLANK);
        b.write(0xC001, Opcode.JP_N16.encoding);
        b.write(0xC002, 0x23);
        b.write(0xC003, 0x30);
        cycleCpu(c, 5, Opcode.JP_N16.cycles);
        
        assertRegisterValue(RegList.PC, c, AddressMap.WORK_RAM_START + 1);
        
        c.requestInterrupt(Interrupt.VBLANK);
        b.write(0xC001, Opcode.JP_N16.encoding);
        b.write(0xC002, 0x23);
        b.write(0xC003, 0x30);
        cycleCpu(c, 5, Opcode.JP_N16.cycles);
        
        assertRegisterValue(RegList.PC, c, 0x3023);
        
        b.write(0x3023, Opcode.HALT.encoding);
        cycleCpu(c, 9, Opcode.HALT.cycles + 1);
        
        assertRegisterValue(RegList.PC, c, 0x3024);
        
        c.setIME(true);
        cycleCpu(c, 1);
        
        assertRegisterValue(RegList.PC, c, 0x40);
        
        b.write(0x40, Opcode.HALT.encoding);
        cycleCpu(c, 9, Opcode.HALT.cycles + 1);
        
        assertRegisterValue(RegList.PC, c, 0x41);
        
        c.setIE(Interrupt.SERIAL);
        c.requestInterrupt(Interrupt.SERIAL);
        c.setIME(true);
        cycleCpu(c, 1);
        
        assertRegisterValue(RegList.PC, c, 0x58);
    }

     @Test
     void JR_CC_E8_WorksForValidValue() {
     Cpu c = new Cpu();
     Ram r = new Ram(0xFFFF);
     Bus b = connect(c, r);
    
     c.initializeRegisters();
     
     b.write(0, Opcode.JP_N16.encoding);
     b.write(1, 0x23);
     b.write(2, 0x30);
     b.write(0x3023, Opcode.JR_NC_E8.encoding);
     b.write(0x3024, 0xFE);
     cycleCpu(c, Opcode.JP_N16.cycles + Opcode.JR_NC_E8.cycles);
    
     assertRegisterValue(RegList.PC, c, 0x3025);
    
     b.write(0x3025, Opcode.JR_NZ_E8.encoding);
     b.write(0x3026, 0xFE);
     cycleCpu(c, 6, Opcode.JR_NZ_E8.cycles);
    
     assertRegisterValue(RegList.PC, c, 0x3027);
     }

    @Test
    void CALL_N16_AND_RET_WorkForValidValues() {
        GameBoy g = new GameBoy(null);
        Cpu c = g.cpu();
        Bus b = g.bus();
        Ram r = new Ram(0xC000);
        RamController rc = new RamController(r, 0);
        rc.attachTo(b);

        c.initializeRegisters();
        
        for (int i = 0; i < 0xBFFF; i++) {
            b.write(i, Opcode.LD_A_A.encoding);
        }
        b.write(0xBFFF, Opcode.DEC_SP.encoding);
        cycleCpu(c, 0xC000);

        assertArrayEquals(new int[] { 0xC000, 0xFFFF, 0xF0, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC000, Opcode.LD_A_N8.encoding);
        b.write(0xC001, 3);
        b.write(0xC002, Opcode.LD_B_N8.encoding);
        b.write(0xC003, 0);

        b.write(0xC004, Opcode.CALL_N16.encoding);
        b.write(0xC005, 0x0F);
        b.write(0xC006, 0xC0);

        cycleCpu(c, 0xC000, 10);
        assertArrayEquals(new int[] { 0xC00F, 0xFFFD, 0x3, 0xF1, 0, 0xF4, 0xF3,
                0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC00F, Opcode.LD_L_N8.encoding);
        b.write(0xC010, 0x80);
        b.write(0xC011, Opcode.ADD_A_A.encoding);
        b.write(0xC012, Opcode.RET.encoding);

        cycleCpu(c, 0xC00A, 7);
        assertArrayEquals(new int[] { 0xC007, 0xFFFF, 0x6, 0, 0, 0xF4, 0xF3,
                0xF7, 0xFA, 0x80 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC007, Opcode.LD_C_N8.encoding);
        b.write(0xC008, 0);
        b.write(0xC009, Opcode.LD_D_N8.encoding);
        b.write(0xC00A, 0);
        b.write(0xC00B, Opcode.LD_E_N8.encoding);
        b.write(0xC00C, 0);
        b.write(0xC00D, Opcode.LD_H_N8.encoding);
        b.write(0xC00E, 0);

        cycleCpu(c, 0xC011, 8);
        assertArrayEquals(
                new int[] { 0xC00F, 0xFFFF, 0x6, 0, 0, 0, 0, 0, 0, 0x80 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void CALL_N16_AND_RET_CC_WorkForValidValues() {
        GameBoy g = new GameBoy(null);
        Cpu c = g.cpu();
        Bus b = g.bus();
        Ram r = new Ram(0xC000);
        RamController rc = new RamController(r, 0);
        rc.attachTo(b);
        
        c.initializeRegisters();

        b.write(0, Opcode.DEC_SP.encoding);
        b.write(1, Opcode.JP_N16.encoding);
        b.write(2, 0x00);
        b.write(3, 0xC0);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 0xC000, 0xFFFF, 0xF0, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC000, Opcode.LD_A_N8.encoding);
        b.write(0xC001, 3);
        b.write(0xC002, Opcode.ADD_A_A.encoding);

        b.write(0xC004, Opcode.CALL_C_N16.encoding);
        b.write(0xC005, 0x0F);
        b.write(0xC006, 0xC0);

        cycleCpu(c, 6, 6);
        assertArrayEquals(new int[] { 0xC007, 0xFFFF, 0x6, 0x0, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC007, Opcode.LD_A_N8.encoding);
        b.write(0xC008, 0);
        b.write(0xC009, Opcode.ADD_A_A.encoding);

        b.write(0xC00A, Opcode.CALL_Z_N16.encoding);
        b.write(0xC00B, 0x0F);
        b.write(0xC00C, 0xC0);

        cycleCpu(c, 12, 9);
        assertArrayEquals(new int[] { 0xC00F, 0xFFFD, 0, 0b1000 << 4, 0xF2,
                0xF4, 0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC00F, Opcode.LD_L_N8.encoding);
        b.write(0xC010, 0x80);
        b.write(0xC011, Opcode.LD_A_N8.encoding);
        b.write(0xC012, 15);
        b.write(0xC013, Opcode.ADD_A_A.encoding);
        b.write(0xC014, Opcode.CALL_NZ_N16.encoding);
        b.write(0xC015, 0xA0);
        b.write(0xC016, 0xC0);
        cycleCpu(c, 21, 11);
        assertArrayEquals(new int[] { 0xC0A0, 0xFFFB, 30, 0b0010 << 4, 0xF2,
                0xF4, 0xF3, 0xF7, 0xFA, 0x80 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC0A0, Opcode.LD_A_N8.encoding);
        b.write(0xC0A1, 3);
        b.write(0xC0A2, Opcode.ADD_A_A.encoding);
        b.write(0xC0A3, Opcode.CALL_NC_N16.encoding);
        b.write(0xC0A4, 0x50);
        b.write(0xC0A5, 0xC0);
        cycleCpu(c, 32, 9);
        assertArrayEquals(new int[] { 0xC050, 0xFFF9, 6, 0b0000 << 4, 0xF2,
                0xF4, 0xF3, 0xF7, 0xFA, 0x80 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC050, Opcode.LD_A_N8.encoding);
        b.write(0xC051, 0xFF);
        b.write(0xC052, Opcode.ADD_A_A.encoding);
        b.write(0xC053, Opcode.RET_C.encoding);
        cycleCpu(c, 41, 8);
        assertArrayEquals(new int[] { 0xC0A6, 0xFFFB, 0xFE, 0b0011 << 4, 0xF2,
                0xF4, 0xF3, 0xF7, 0xFA, 0x80 }, c._testGetPcSpAFBCDEHL());

        b.write(0xC0A6, Opcode.LD_A_N8.encoding);
        b.write(0xC0A7, 0x00);
        b.write(0xC0A8, Opcode.ADD_A_A.encoding);
        b.write(0xC0A9, Opcode.RET_Z.encoding);
        cycleCpu(c, 49, 8);
        assertArrayEquals(new int[] { 0xC017, 0xFFFD, 0, 0b1000 << 4, 0xF2,
                0xF4, 0xF3, 0xF7, 0xFA, 0x80 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void RST_WorksForValidValues() {
        GameBoy g = new GameBoy(null);
        Bus b = g.bus();
        Cpu c = g.cpu();
        Ram r = new Ram(0x1000);
        RamController rc = new RamController(r, 0);
        rc.attachTo(b); 
        
        c.initializeRegisters();

        b.write(0, Opcode.DEC_SP.encoding);
        b.write(1, Opcode.JP_N16.encoding);
        b.write(2, 0x80);
        b.write(3, 0xFF);
        g.runUntil(6);
        assertArrayEquals(new int[] { 0xFF80, 0xFFFF, 0xF0, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(0xFF80, Opcode.LD_A_N8.encoding);
        b.write(0xFF81, 9);
        b.write(0xFF82, Opcode.RST_0.encoding);
        g.runUntil(12);
        assertArrayEquals(new int[] { 0, 0xFFFD, 9, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 8);
        b.write(2, Opcode.RST_1.encoding);
        g.runUntil(18);
        assertArrayEquals(new int[] { 8, 0xFFFB, 0x8, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(3, Opcode.LD_B_N8.encoding);
        b.write(4, 12);
        b.write(8, Opcode.LD_A_N8.encoding);
        b.write(9, 76);
        b.write(10, Opcode.RST_2.encoding);
        g.runUntil(24);
        assertArrayEquals(new int[] { 16, 0xFFF9, 76, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(16, Opcode.LD_A_N8.encoding);
        b.write(17, 32);
        b.write(18, Opcode.RST_3.encoding);
        g.runUntil(30);
        assertArrayEquals(new int[] { 24, 0xFFF7, 32, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        
        b.write(24, Opcode.LD_A_N8.encoding);
        b.write(25, 42);
        b.write(26, Opcode.RST_4.encoding);
        g.runUntil(36);
        assertArrayEquals(new int[] { 32, 0xFFF5, 42, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(32, Opcode.LD_A_N8.encoding);
        b.write(33, 21);
        b.write(34, Opcode.RST_5.encoding);
        g.runUntil(42);
        assertArrayEquals(new int[] { 40, 0xFFF3, 21, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(40, Opcode.LD_A_N8.encoding);
        b.write(41, 90);
        b.write(42, Opcode.RST_5.encoding);
        g.runUntil(48);
        assertArrayEquals(new int[] { 40, 0xFFF1, 90, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(40, Opcode.LD_A_N8.encoding);
        b.write(41, 1);
        b.write(42, Opcode.RST_6.encoding);
        g.runUntil(54);
        assertArrayEquals(new int[] { 48, 0xFFEF, 1, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
        
        b.write(48, Opcode.LD_A_N8.encoding);
        b.write(49, 77);
        b.write(50, Opcode.RST_7.encoding);
        g.runUntil(60
                );
        assertArrayEquals(new int[] { 56, 0xFFED, 77, 0xF1, 0xF2, 0xF4,
                0xF3, 0xF7, 0xFA, 0xF5 }, c._testGetPcSpAFBCDEHL());
    }
}
