package ch.epfl.gameboj.component.cpu;

import java.util.ArrayList;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu.Reg;

public final class Cpu implements Component, Clocked {

    private RegisterFile<Reg> file;
    private int SP;
    private int PC;
    private Bus bus; //TODO arraylist ou bus tout seul?
    
    private long nextNonIdleCycle;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    
    public Cpu() {
        file = new RegisterFile<Reg>(Reg.values());
        SP = 0;
        PC = 0;
        nextNonIdleCycle = 0;
    }
    
    @Override
    public void cycle(long cycle) {
        if (cycle < nextNonIdleCycle) {
            return;
        } else {
            // TODO get next instruction and execute it
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

    //TODO quel visibilité?
    protected void dispatch(Opcode instruction) {
        switch (instruction.family) {
        case NOP: {
        }
            break;
        case LD_R8_HLR: {
        }
            break;
        case LD_A_HLRU: {
        }
            break;
        case LD_A_N8R: {
        }
            break;
        case LD_A_CR: {
        }
            break;
        case LD_A_N16R: {
        }
            break;
        case LD_A_BCR: {
        }
            break;
        case LD_A_DER: {
        }
            break;
        case LD_R8_N8: {
        }
            break;
        case LD_R16SP_N16: {
        }
            break;
        case POP_R16: {
        }
            break;
        case LD_HLR_R8: {
        }
            break;
        case LD_HLRU_A: {
        }
            break;
        case LD_N8R_A: {
        }
            break;
        case LD_CR_A: {
        }
            break;
        case LD_N16R_A: {
        }
            break;
        case LD_BCR_A: {
        }
            break;
        case LD_DER_A: {
        }
            break;
        case LD_HLR_N8: {
        }
            break;
        case LD_N16R_SP: {
        }
            break;
        case LD_R8_R8: {
        }
            break;
        case LD_SP_HL: {
        }
            break;
        case PUSH_R16: {
        }
            break;
        }
    }
    
    /*
     * ------------------------------      Bus methods      -----------------------------
     */
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }
    
    private int read8(int address);
    private int read8AtHl();
    int read8AfterOpcode();
    int read16(int address);
    int read16AfterOpcode();
    void write8(int address, int v);
    void write16(int address, int v);
    void write8AtHl(int v);
    void push16(int v);
    int pop16();
    
    /*
     * ---------------------      Extract information from Opcodes      ------------------
     */
    private Reg extractReg(Opcode opcode, int startBit);
    private Reg16 extractReg(Opcode opcode);
    private int extractHlIncrement(Opcode opcode);
    
    
    /*
     * --------------------------      Registers Managements      -------------------------
     */
    private int reg16(Reg16 r);
    private void setReg16(Reg16 r, int newV);
    private void setReg16SP(Reg16 r, int newV);
    
    // TODO
    public int[] _testGetPcSpAFBCDEHL() {
        return new int[1];
    }

}
