package ch.epfl.gameboj;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    
    
    private RamController workRam;
    private RamController echoRam;
    private Cpu cpu;
    private long cycleGB;
        
    public GameBoy(Object cartridge) {
        bus = new Bus();
        
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        
        workRam = new RamController(ram,AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram,AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        cycleGB = 0;
        
        cpu.attachTo(bus);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public void runUntil(long cycle) {
        if(cycleGB>cycle) {
            throw new IllegalArgumentException();
        }
        for (long c = cycleGB; c < cycle; c++) {
            cpu.cycle(c);
            cycleGB++;
        }
    }
    
    public long cycles() {
        return cycleGB;
    };

}
