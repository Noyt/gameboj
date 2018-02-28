package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    
    
    private RamController workRam;
    private RamController echoRam;
        
    public GameBoy(Object cartridge) {
        bus = new Bus();
        
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        
        workRam = new RamController(ram,AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram,AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }

}
