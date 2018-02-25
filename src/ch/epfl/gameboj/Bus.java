package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {

    private ArrayList<Component> components;
    
    public Bus() {
        components = new ArrayList<Component>();
    }
    
    public void attach(Component component) {
        components.add(Objects.requireNonNull(component));
    }
    
    //TODO variable pour le read ?
    public int read(int address) {
        Preconditions.checkBits16(address);
        for(Component c : components) {
            if(c.read(address) != Component.NO_DATA) {
                return c.read(address);
            }
        } 
        return 0xFF;
    }
    
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for(Component c : components) {
            c.write(address, data);
        }
    }
}
