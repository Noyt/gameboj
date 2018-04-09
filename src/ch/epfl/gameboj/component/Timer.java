package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {

    private Cpu cpu;

    private int DIV;
    private int TIMA;
    private int TMA;
    private int TAC;

    private final static int DIV_MAX_VALUE = 0xFFFF;
    private final static int TIMA_MAX_VALUE = 0xFF;

    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        DIV = 0;
        TIMA = 0;
        TMA = 0;
        TAC = 0;
    }

    @Override
    public void cycle(long cycle) {

        boolean previousState = state();

        manageDIV();

        incTIMAIfChange(previousState);
    }

    
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        
        boolean previousState = state();
        
        switch (address) {
        
        case AddressMap.REG_DIV:
            DIV = 0;
            break;

        case AddressMap.REG_TIMA:
            TIMA = data;
            break;

        case AddressMap.REG_TMA:
            TMA =data;
            break;

        case AddressMap.REG_TAC:
            TAC = data;
            break;

        default:
            break;
        }
        
        incTIMAIfChange(previousState);
    }

    @Override
    public int read(int address) {
        
        Preconditions.checkBits16(address);

        switch (address) {

        case AddressMap.REG_DIV:
            return Bits.extract(DIV, 0, Byte.SIZE);

        case AddressMap.REG_TIMA:
            return TIMA;

        case AddressMap.REG_TMA:
            return TMA;

        case AddressMap.REG_TAC:
            return TAC;

        default:
            return NO_DATA;
        }

    }

    private void manageDIV() {
        DIV = Bits.clip(Short.SIZE, DIV + 4);
    }

    private void incTIMAIfChange(boolean previousState) {
        
        boolean newState = state();

        if (previousState && !newState) {
            ++TIMA;
            
            if (TIMA > TIMA_MAX_VALUE) {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            }
        }
    }

    private boolean state() {

        int DIVBitToTest;

        switch (Bits.extract(TAC, 0, 2)) {

        case 0b00:
            DIVBitToTest = 9;
            break;
        case 0b01:
            DIVBitToTest = 3;
            break;
        case 0b10:
            DIVBitToTest = 5;
            break;
        case 0b11:
            DIVBitToTest = 7;
            break;
        default:
            throw new Error("no bit to test in the counter");
        }

        return Bits.test(TAC, 2) && Bits.test(DIV, DIVBitToTest);
    }

}
