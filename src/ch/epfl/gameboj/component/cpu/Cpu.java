package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {
    
    private RegisterFile<Reg> file;
    
    private long nextNonIdleCycle;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub
        if (cycle < nextNonIdleCycle) {
            return;
        } else {
            
        }
    }

    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub

    }

    public int[] _testGetPcSpAFBCDEHL() {
        return 
    }

}
