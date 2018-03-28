package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {

    private Rom rom = null;

    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == 32768);
        this.rom = rom;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        // TODO la rom part elle vraiment de 0 (et au niveau de la rom de demarrage ?)
        if (address > 0x7FFF) {
            return NO_DATA;
        } else {
            return rom.read(address);
        }
    }

    @Override
    public void write(int address, int data) {
        
    }

    

}
