package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {

    private MBC0 mbc;

    // TODO ou bien
    // private Cartridge(Component mbc)
    private Cartridge(MBC0 mbc) {
        this.mbc = mbc;
    }

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

    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream s = new FileInputStream(romFile)) {
            byte[] tab = new byte[(int) romFile.length()];
           // s.read(tab);
            tab = s.readAllBytes().clone();
            if(!(Byte.toUnsignedInt(tab[0x147]) == 0)) {
                throw new IllegalArgumentException();
            }
            
            Cartridge cart = new Cartridge(new MBC0(new Rom(tab)));
            return cart;

        } catch (FileNotFoundException a) {
            throw new IOException();
        }
    }
}
