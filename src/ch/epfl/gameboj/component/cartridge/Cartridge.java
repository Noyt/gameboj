package ch.epfl.gameboj.component.cartridge;

import java.io.File;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class Cartridge implements Component {
    
    private MBC0 mbc; 

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        mbc.write(address, data);
        
    }
    
    //TODO ou bien
    //private Cartridge(Component mbc)
    private Cartridge(MBC0 mbc) {
        this.mbc = mbc;
    }
    
    public static Cartridge ofFile(File romFile) {
        romFile.
        Cartridge cart = new Cartridge(mbc)
    }
}
