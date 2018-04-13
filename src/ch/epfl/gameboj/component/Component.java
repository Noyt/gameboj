package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * a Component connected to the bus
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public interface Component {

    /**
     * this value will returned by the method read if no data is stored at the
     * address
     */
    public static final int NO_DATA = 0x100;

    /**
     * returns the value that is stored in the adress
     * 
     * @param address
     *            an int : the adress in which we want the data
     * @return an int : the data contained in the adress
     */
    int read(int address);

    /**
     * stores a data in the adress
     * 
     * @param address
     *            an int : the adress where we want to store the data
     * @param data
     *            the data we want to store
     */
    void write(int address, int data);

    /**
     * attachs the component to the bus
     * 
     * @param bus
     *            a Bus : the bus that will be bounded to the component
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
