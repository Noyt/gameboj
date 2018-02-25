package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component{
    
    private Ram ram;
    private int startAddress;
    private int endAddress;
    
    public RamController(Ram ram, int startAddress, int endAddress) {
        this.ram = Objects.requireNonNull(ram);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        //Preconditions.checkArgument((endAddress-startAddress >= 0) && (endAddress-startAddress <= ram.size()));
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }
    
    //TODO
    public RamController(Ram ram, int startAddress) {
        //this(ram, startAddress, 0x100);
        this(ram, startAddress, startAddress + 0x100);
    }
    
    //TODO que faire si address pas entre start et end
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(address < startAddress || address >= endAddress) {
            return NO_DATA;
        }
        
        try{
            
            //return Objects.requireNonNull(ram.read(address-startAddress));
            //if(ram.read(address-startAddress) != 0) {
                return ram.read(address-startAddress);
            //}
            //throw new NullPointerException();
        }
        
        //TODO do we have to catch this exception?
        catch (IndexOutOfBoundsException e) {
            return NO_DATA;
        }
        
        catch (NullPointerException e) {
            return NO_DATA;
        }
    }
    
    //TODO et du coup ici on catch aussi ?
    public void write(int address, int data) {
      Preconditions.checkBits16(address);
      Preconditions.checkBits8(data);
      
      if(address >= startAddress && address < endAddress) {
          //ram.write(address, data);
          ram.write(address-startAddress, data);
      }
    }
}
