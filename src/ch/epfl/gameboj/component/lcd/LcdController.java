package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
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
    public static final int IMAGE_DIMENSION = 256;

    public static final int MODE2_CYCLES = 20;
    public static final int MODE3_CYCLES = 43;
    public static final int MODE0_CYCLES = 51;
    public static final int MODE1_NB_LINES = 10;

    public static final int TILES_CHOICES_PER_IMAGE = 256;
    public static final int OCTETS_PER_TILE = 16;

    private final Cpu cpu;
    private final RegisterFile<Reg> regs;
    private final RamController videoRam;
    private final RamController OAM;
    private Bus bus;

    private long nextNonIdleCycle;
    private int lcdOnCycle;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private int winY;
    
    private int copySource;
    private int copyDestination;

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

    private enum ImageType {
        BACKGROUND, WINDOW
    }

    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        regs = new RegisterFile<Reg>(Reg.values());
        videoRam = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE),
                AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);
        OAM = new RamController(new Ram(AddressMap.OAM_RAM_SIZE),
                AddressMap.OAM_START, AddressMap.OAM_END);
        nextNonIdleCycle = 0;
        lcdOnCycle = 0;
        nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
        copyDestination = AddressMap.OAM_END;
    }

    @Override
    public int read(int address) {

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            int index = address - AddressMap.REGS_LCDC_START;
            return regs.get(Reg.values()[index]);
        } else if (address >= AddressMap.OAM_START
                && address < AddressMap.OAM_END) {
            return OAM.read(address);
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
                break;

            case LYC:
                regs.set(r, data);
                checkLY_LYC();
                break;

            case LCDC:
                regs.set(Reg.LCDC, data);
                checkLCDC();
                break;
            
            case DMA:
                copySource = data << Byte.SIZE;
                copyDestination = AddressMap.OAM_START;

            default:
                regs.set(r, data);
            }
        } else if (address >= AddressMap.OAM_START
                && address < AddressMap.OAM_END) {
            OAM.write(address, data);
        } else {
            videoRam.write(address, data);
        }
    }

    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    @Override
    public void cycle(long cycle) {

        // if ((cycle > 16000 && cycle < 17700)
        // || (cycle > 33800 && cycle < 35200)) {
        // System.out.println("current cycle " + cycle + ", nextNon "
        // + nextNonIdleCycle + ", lcdOn " + lcdOnCycle);
        // }

        if (nextNonIdleCycle == Long.MAX_VALUE
                && testLCDCBit(LCDCBit.LCD_STATUS)) {
            lcdOnCycle = 0;
            reallyCycle();
        }

        if (cycle < nextNonIdleCycle) {
            ++lcdOnCycle;
            return;
        } else {
            reallyCycle();
        }
        
        if(copyDestination != AddressMap.OAM_END) {
            //TODO OAM ou bus ?
            OAM.write(copyDestination, bus.read(copySource));
            copySource++;
            copyDestination++;
        }
        
        ++lcdOnCycle;
    }

    public LcdImage currentImage() {
        if (currentImage == null) {
            return new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
        }
        return currentImage;
    }

    private void reallyCycle() {
        switch (lcdOnCycle) {

        case MODE0_CYCLES + MODE2_CYCLES + MODE3_CYCLES:
        case 0:
            if (getMode() != Mode.M1) {
                if (regs.get(Reg.LY) == LCD_HEIGHT) {
                    setMode(Mode.M1);
                    currentImage = nextImageBuilder.build();
                    winY = 0;
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
            computeLine();
            break;

        case MODE2_CYCLES + MODE3_CYCLES:
            if (getMode() != Mode.M1)
                setMode(Mode.M0);
            nextNonIdleCycle += MODE0_CYCLES;
            break;
        }
    }

    private void computeLine() {
        int bitLineInLCD = regs.get(Reg.LY);
        int adjustedWX = regs.get(Reg.WX) - 7;
        if (bitLineInLCD < LCD_HEIGHT) {

            int bitLine = (bitLineInLCD + regs.get(Reg.SCY)) % IMAGE_DIMENSION;

            LcdImageLine lcdLineFromBG = backgroundLine(bitLine)
                    .extractWrapped(regs.get(Reg.SCX), LCD_WIDTH);

            if (testLCDCBit(LCDCBit.WIN)
                    && (adjustedWX >= 0 && adjustedWX < LCD_WIDTH)
                    && regs.get(Reg.WY) <= bitLineInLCD) {

                LcdImageLine adjustedWindowLine = windowLine(winY)
                        .shift(adjustedWX).extractWrapped(0, LCD_WIDTH);

                nextImageBuilder.setLine(bitLineInLCD,
                        lcdLineFromBG.join(adjustedWindowLine, adjustedWX)
                                .mapColors(regs.get(Reg.BGP)));
                winY++;
            } else {
                nextImageBuilder.setLine(bitLineInLCD,
                        lcdLineFromBG.mapColors(regs.get(Reg.BGP)));
            }
        }
        updateLYForNewLine();
    }

    private LcdImageLine backgroundLine(int bitLine) {
        // return extractLine(bitLine, ImageType.BACKGROUND);
        return extractLine(bitLine, true);
    }

    private LcdImageLine windowLine(int bitLine) {
        // return extractLine(bitLine, ImageType.WINDOW);
        return extractLine(bitLine, false);
    }

    private LcdImageLine extractLine(int bitLine, boolean type) {
        int tileLine = bitLine / Byte.SIZE;

        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(
                IMAGE_DIMENSION);

        int slot = testLCDCBit(type ? LCDCBit.BG_AREA : LCDCBit.WIN_AREA) ? 1
                : 0;
        //
        // switch (type) {
        // case BACKGROUND:
        // slot = testLCDCBit(LCDCBit.BG_AREA) ? 1 : 0;
        // case WINDOW:
        // slot = testLCDCBit(LCDCBit.WIN_AREA) ? 1 : 0;
        // default:
        // // TODO que mettre ici ?
        // // slot = testLCDCBit(LCDCBit.BG_AREA) ? 1 : 0;
        // }

        for (int i = 0; i < IMAGE_DIMENSION / Byte.SIZE; ++i) {

            int tileIndexInRam = tileIndexInRam(i, tileLine)
                    + AddressMap.BG_DISPLAY_DATA[slot];

            int tileName = videoRam.read(tileIndexInRam);

            int tileLineAddress = getTileLineAddress(bitLine, tileName);
            int lsb = Bits.reverse8(videoRam.read(tileLineAddress));
            int msb = Bits.reverse8(videoRam.read(tileLineAddress + 1));

            lineBuilder.setBytes(i, msb, lsb);
        }

        return lineBuilder.build();
    }

    private int tileIndexInRam(int tileX, int tileY) {

        int nbTileInALine = IMAGE_DIMENSION / Byte.SIZE;

        return tileY * nbTileInALine + tileX;
    }

    private int getTileLineAddress(int line, int tileName) {
        int tileAddress;
        int tileInterval = TILES_CHOICES_PER_IMAGE / 2;

        if (tileName < tileInterval) {
            tileAddress = testLCDCBit(LCDCBit.TILE_SOURCE)
                    ? AddressMap.TILE_SOURCE[1]
                    : (AddressMap.TILE_SOURCE[0]
                            + tileInterval * OCTETS_PER_TILE);
        } else {
            tileAddress = AddressMap.TILE_SOURCE[0];
        }

        tileAddress += (tileName % tileInterval) * OCTETS_PER_TILE
                + ((line % (OCTETS_PER_TILE / 2)) * 2);
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
