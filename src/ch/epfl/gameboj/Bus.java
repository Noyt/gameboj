package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {

    private ArrayList<Component> components;
    
    /**
     * constructs a bus and initializes an empty arraylist
     */
    public Bus() {
        components = new ArrayList<Component>();
    }
    
    /**
     * attaches himself to a component, by adding it to his list of components
     * throws NullPointerException if the component is null
     * 
     * @param component
     */
    public void attach(Component component) {
        components.add(Objects.requireNonNull(component));
    }
    
    /**
     * returns the value stored at the address if at least one component has a data at the address
     * 
     * @param address an int
     * @return an int : the data at the address
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        for(Component c : components) {
            if(c.read(address) != Component.NO_DATA) {
                return c.read(address);
            }
        } 
        return 0xFF;
    }
    
    /**
     * store the data at the address in every component bouned to the bus
     * 
     * @param address an int : the address we want to store the new data
     * @param data an int
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for(Component c : components) {
            c.write(address, data);
        }
    }
}
