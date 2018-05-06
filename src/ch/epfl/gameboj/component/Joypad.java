package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Joypad implements Component {

    private static final int NUMBER_OF_KEYS = 8;

    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    private final Cpu cpu;

    private int P1;

    private final int[] allKeys;

    public Joypad(Cpu cpu) {
        this.cpu = cpu;
        P1 = Bits.clip(Byte.SIZE, -1);
        allKeys = new int[NUMBER_OF_KEYS];
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_P1) {
            return P1;
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            data = (data >>> 4) << 4;
            P1 = Bits.set(Bits.set(P1, 4, false), 5, false);
            P1 = P1 | data;
            
            int oldP1 = P1;
            updateP1();
            compareOldP1AndNewP1(oldP1);
        }
    }

    public void keyPressed(Key key) {
        int keyIndex = key.ordinal();
        System.out.println("key pressed");
        allKeys[keyIndex] = Bits.set(0, keyIndex, true);
    }

    public void keyReleased(Key key) {
        int keyIndex = key.ordinal();
        allKeys[keyIndex] = 0;
    }
    
    private void updateP1() {
        int tmp1 = 0;
        
        if (!Bits.test(P1, 4)) {
            for (int i = 0; i < NUMBER_OF_KEYS/2; ++i) {
                tmp1 |= allKeys[i];
            }
        }
        
        if (!Bits.test(P1, 5)) {
            for (int i = NUMBER_OF_KEYS/2; i < NUMBER_OF_KEYS; ++i) {
                tmp1 |= (allKeys[i] >>> (NUMBER_OF_KEYS/2));
            }
        }
        
        int tmp2 = (~P1 >>> 4) << 4;
        P1 = ~(tmp1 | tmp2);
        
        if (P1%2 == 0) {
            System.out.println("A a été appuye " + P1);
        }
    }
    
    private void compareOldP1AndNewP1(int oldP1) {
        oldP1 = Bits.clip(4, oldP1);
        int newP1 = Bits.clip(4, P1);
        
        int mama = (oldP1^newP1)&oldP1; //TODO
        if (mama != 0) {
        //if (((oldP1^newP1)& oldP1) != 0) {
            cpu.requestInterrupt(Interrupt.JOYPAD);
            System.out.println("interruption car changement " + mama);
        }
    }
}
