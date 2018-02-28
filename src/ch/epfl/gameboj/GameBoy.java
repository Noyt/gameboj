package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    
    private RamController workRam;
    
    private static final int WORK_RAM_SIZE = 8192;
    private static final int ECHO_RAM_SIZE = 7680;
    
    public GameBoy() {
        bus = new Bus();
        
        workRam = new RamController(new Ram(WORK_RAM_SIZE), 0xE000);
    }

}
