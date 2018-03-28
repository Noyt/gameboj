package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {

    private Cartridge cart;
    
    private boolean bootRomDisabled = false;
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(0 <= address && address <= 0xFF) {
            return bootRomDisabled ? cart.read(address) : Byte.toUnsignedInt(BootRom.DATA[address]);
        }
        return cart.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomDisabled = true;
        } else {
            cart.write(address, data);
        }
        
    }
    
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        cart = cartridge;
    }

}