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
        assertArrayEquals(new int[] {1,0,0xF0,0xF1,0xF2,0xF4,0xF3,0xF7,0xFA,0xF5}, c._testGetPcSpAFBCDEHL());
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
        
        assertArrayEquals(new int[] {5,0,34,0xF1,34,34,34,34,0xFA,0xF5}, c._testGetPcSpAFBCDEHL());
   }
}
