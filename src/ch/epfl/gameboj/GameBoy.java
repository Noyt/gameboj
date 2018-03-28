package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    
    
    private RamController workRam;
    private RamController echoRam;
    private Cpu cpu;
    private long cycleGB;
        
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        
        bus = new Bus();
        
        //TODO
        //plutot en attribut non ?
        BootRomController brc = new BootRomController(cartridge);
        
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        
        workRam = new RamController(ram,AddressMap.WORK_RAM_START);
        echoRam = new RamController(ram,AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        cycleGB = 0;
        
        cpu.attachTo(bus);
        workRam.attachTo(bus);
        echoRam.attachTo(bus);
        brc.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public Timer timer() {
        return ;
    }
    
    public void runUntil(long cycle) {
        if(cycleGB > cycle) {
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
