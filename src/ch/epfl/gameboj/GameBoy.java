package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class GameBoy {
    
    private Bus bus;
    
    private BootRomController brc;
    private RamController workRam;
    private RamController echoRam;
    private Cpu cpu;
    private long cycleGB;
    private Timer timer;
        
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        
        bus = new Bus();
        
        brc = new BootRomController(cartridge);
        
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        
        workRam = new RamController(ram,AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram,AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        
        timer = new Timer(cpu);
        
        cycleGB = 0;
        
        cpu.attachTo(bus);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
        brc.attachTo(bus);
        timer.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public Timer timer() {
        return timer;
    }
    
    public void runUntil(long cycle) {

        if(cycleGB > cycle) {
            throw new IllegalArgumentException();
        }
        
        while(cycleGB < cycle) {
            
            timer.cycle(cycleGB);
            cpu.cycle(cycleGB);
            cycleGB++;
        }
    }
    
    public long cycles() {
        return cycleGB;
    };
    

}
