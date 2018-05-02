package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Clocked, Component {

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    public static final int BG_DIMENSION = 256;

    public static final int MODE2_CYCLES = 20;
    public static final int MODE3_CYCLES = 43;
    public static final int MODE0_CYCLES = 51;
    public static final int MODE1_NB_LINES = 10;

    public static final int TILES_CHOICES_PER_IMAGE = 256;
    public static final int OCTETS_PER_TILE = 16;

    private final Cpu cpu;
    private final RegisterFile<Reg> regs;
    private final RamController videoRam;

    private long nextNonIdleCycle;
    private int lcdOnCycle;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum LCDCBit implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STATBit implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    }

    private enum Mode {
        M0, M1, M2, M3
    }

    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        regs = new RegisterFile<Reg>(Reg.values());
        videoRam = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE),
                AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);
        nextNonIdleCycle = 0;
        lcdOnCycle = 0;
        nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
    }

    @Override
    public int read(int address) {

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            int index = address - AddressMap.REGS_LCDC_START;
            return regs.get(Reg.values()[index]);
        }

        return videoRam.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {

            int index = address - AddressMap.REGS_LCDC_START;

            Reg r = Reg.values()[index];

            switch (r) {
            case STAT:
                int oldState = regs.get(Reg.STAT);
                regs.set(Reg.STAT, (regs.get(Reg.STAT) & 0b00000111)
                        | (data & 0b11111000));
                checkSTAT(oldState);
                break;

            case LY:
            case LYC:
                regs.set(r, data);
                checkLY_LYC();
                break;

            case LCDC:
                regs.set(Reg.LCDC, data);
                checkLCDC();
                break;
            default:
                regs.set(r, data);
            }
        } else {
            videoRam.write(address, data);
        }
    }

    @Override
    public void cycle(long cycle) {

        boolean TODO = false;
//        if ((cycle > 16000 && cycle < 17700)
//                || (cycle > 33800 && cycle < 35200)) {
//            System.out.println("current cycle " + cycle + ", nextNon  "
//                    + nextNonIdleCycle + ", lcdOn " + lcdOnCycle);
//            TODO = true;
//        }

        if (nextNonIdleCycle == Long.MAX_VALUE
                && testLCDCBit(LCDCBit.LCD_STATUS)) {
            lcdOnCycle = 0;
            reallyCycle(TODO);
        }

        if (cycle < nextNonIdleCycle) {
            ++lcdOnCycle;
            return;
        } else {
            reallyCycle(TODO);
        }
        ++lcdOnCycle;
    }

    public LcdImage currentImage() {
        if (currentImage == null) {
            return new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
        }
        return currentImage;
    }

    private void reallyCycle(boolean TODO) {
        switch (lcdOnCycle) {

        case MODE0_CYCLES + MODE2_CYCLES + MODE3_CYCLES:
        case 0:
            if (getMode() != Mode.M1) {
                if (regs.get(Reg.LY) == LCD_HEIGHT) {
                    setMode(Mode.M1);
                    currentImage = nextImageBuilder.build();
                } else
                    setMode(Mode.M2);
            } else {
                if (regs.get(Reg.LY) == 0) {
                    setMode(Mode.M2);
                    nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
                }
            }

            lcdOnCycle = 0;
            nextNonIdleCycle += MODE2_CYCLES;
            break;

        case MODE2_CYCLES:
            if (getMode() != Mode.M1)
                setMode(Mode.M3);

            nextNonIdleCycle += MODE3_CYCLES;
            computeLine(TODO);
            break;

        case MODE2_CYCLES + MODE3_CYCLES:
            if (getMode() != Mode.M1)
                setMode(Mode.M0);
            nextNonIdleCycle += MODE0_CYCLES;
            break;
        }
    }

    private void computeLine(boolean TODO) {
        int line = regs.get(Reg.LY);

        if (TODO)
            System.out.println("computing line " + line);

        if (line < LCD_HEIGHT) {
            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(
                    LCD_WIDTH);

            // Image de fond
            int SCXTile = regs.get(Reg.SCX) / Byte.SIZE;
            int SCYTile = regs.get(Reg.SCY) / Byte.SIZE;

            int slot = testLCDCBit(LCDCBit.BG_AREA) ? 1 : 0; 
            for (int i = 0; i < LCD_WIDTH / Byte.SIZE; ++i) {
                int tileIndexInRam = tileIndexInRam(i, line)
                        + AddressMap.BG_DISPLAY_DATA[slot];

                int tileName = videoRam.read(tileIndexInRam);

                int tileLineAddress = getTileLineAddress(line, tileName);
                int lsb = Bits.reverse8(videoRam.read(tileLineAddress));
                int msb = Bits.reverse8(videoRam.read(tileLineAddress+1));
                

                lineBuilder.setBytes(i, msb, lsb);
            }

            nextImageBuilder.setLine(line, lineBuilder.build());
        }
        updateLYForNewLine();
    }

    private int tileIndexInRam(int xOctetInLcd, int yBitInLcd) {

        int tileXInImage = xOctetInLcd + regs.get(Reg.SCX) / Byte.SIZE;
        int tileYInImage = (yBitInLcd + regs.get(Reg.SCY)) / Byte.SIZE;

        int nbTileInALine = BG_DIMENSION / Byte.SIZE;

        return tileYInImage * nbTileInALine + tileXInImage;
    }

    private int getTileLineAddress(int line, int tileName) {
        int tileAddress;
        int tileInterval = TILES_CHOICES_PER_IMAGE / 2;

        if (tileName < tileInterval) {
            tileAddress = testLCDCBit(LCDCBit.TILE_SOURCE)
                    ? AddressMap.TILE_SOURCE[1]
                    : (AddressMap.TILE_SOURCE[0] + tileInterval * OCTETS_PER_TILE);
        } else {
            tileAddress = AddressMap.TILE_SOURCE[0];
        }

        tileAddress += (tileName % tileInterval) * OCTETS_PER_TILE
                + ((line % (OCTETS_PER_TILE/2)) * 2);
        return tileAddress;
    }

    private void updateLYForNewLine() {
        int tmp = regs.get(Reg.LY);
        if (tmp == LCD_HEIGHT + MODE1_NB_LINES - 1) {
            regs.set(Reg.LY, 0);
        } else {
            regs.set(Reg.LY, tmp + 1);
        }
        checkLY_LYC();
    }

    private void checkLY_LYC() {
        if (regs.get(Reg.LY) == regs.get(Reg.LYC)) {
            setSTATBit(STATBit.LYC_EQ_LY, true);
            if (testSTATBit(STATBit.INT_LYC))
                cpu.requestInterrupt(Interrupt.LCD_STAT);

        } else {
            setSTATBit(STATBit.LYC_EQ_LY, false);
        }
    }

    private void checkSTAT(int oldSTAT) {

        boolean tmp = false;

        if (getMode() == Mode.M0 && getMode(oldSTAT) != Mode.M0
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE0))
            tmp = true;

        if (getMode() == Mode.M2 && getMode(oldSTAT) != Mode.M2
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE2))
            tmp = true;

        if (getMode() == Mode.M1 && getMode(oldSTAT) != Mode.M1
                && Bits.test(regs.get(Reg.STAT), STATBit.INT_MODE1)) {
            tmp = true;
            cpu.requestInterrupt(Interrupt.VBLANK);
        }

        if (tmp)
            cpu.requestInterrupt(Interrupt.LCD_STAT);

    }

    private void checkLCDC() {
        if (testLCDCBit(LCDCBit.LCD_STATUS)) {
            setMode(Mode.M0);
            regs.set(Reg.LY, 0);
            checkLY_LYC();
            nextNonIdleCycle = Long.MAX_VALUE;
        }
    }

    private void setSTATBit(STATBit bit, boolean v) {
        regs.setBit(Reg.STAT, bit, v);
    }

    private boolean testSTATBit(STATBit bit) {
        return regs.testBit(Reg.STAT, bit);
    }

    private boolean testLCDCBit(LCDCBit bit) {
        return regs.testBit(Reg.LCDC, bit);
    }

    private Mode getMode(int STAT) {
        Preconditions.checkBits8(STAT);
        int mode = (Bits.test(STAT, STATBit.MODE1) ? 1 : 0) * 2
                + (Bits.test(STAT, STATBit.MODE0) ? 1 : 0);

        return Mode.values()[mode];
    }

    private Mode getMode() {
        return getMode(regs.get(Reg.STAT));
    }

    private void setMode(Mode m) {

        int mode = m.ordinal();

        setSTATBit(STATBit.MODE0, (mode % 2) == 1);
        setSTATBit(STATBit.MODE1, (mode / 2) == 1);

        if (m == Mode.M1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
    }
}
