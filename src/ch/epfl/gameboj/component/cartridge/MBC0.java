package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * A bank memory controller of type 0 : it means that it can contain
 * only a read-only memory of 32768 octets
 * 
 * @author Sophie du Couédic (26007)
 * @author Arnaud Robert (287964)
 */
public final class MBC0 implements Component {

    private Rom rom = null;

    /**
     * Constructs a new bank memory controller that contains the given
     * 3278 octets read-only memory
     * 
     * @param rom
     *            a Rom : read-only memory of 32768 octets
     * @throws IllegalArgumentException
     *             if rom is not of size 32768 octets
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == 32768);
        this.rom = rom;
    }

    /**
     * Implements the method read of Component, that returns the value stored at
     * the given address in the memory, or NO_DATA if the address doesn't belong
     * to the memory
     * 
     * @param address
     *            an int : the address that contains the desired data
     * @return an int (octet) : the value stored at the given address in the
     *         memory
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        // TODO la rom part elle vraiment de 0 (et au niveau de la rom de
        // demarrage ?)
        if (address > 0x7FFF) {
            return NO_DATA;
        } else {
            return rom.read(address);
        }
    }

    /**
     * Implements the method write of Component, that is supposed to store a
     * value at the given address. Actually, as the memory is a read-only
     * memory, the method doesn't store any value in the memory at the given
     * address
     * 
     * @param address
     *            an int : the address
     * @param data
     *            an int : the value 
     * 
     * @throws IllegalArgumentException
     *             if the address is not a 16-bits value or if data is not a
     *             8-bits value
     * 
     * @see ch.epfl.gameboj.component.Component#write(int,int)
     */
    @Override
    public void write(int address, int data) {
        // TODO est-ce qu'il faut quand meme teste si address est de taille 16?
        // et data?
    }

}
