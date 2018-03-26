package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {

    //TODO connecter au bus
    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub
        
    }
    
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        
    }

}
