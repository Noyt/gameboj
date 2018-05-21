package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Clocked, Component {

    /**
     * The width and the height of the LCD screen in pixels
     */
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    
    private static final int IMAGE_DIMENSION = 256;

    private static final int MODE2_CYCLES = 20;
    private static final int MODE3_CYCLES = 43;
    private static final int MODE0_CYCLES = 51;
    private static final int MODE1_NB_LINES = 10;

    private static final int TILES_CHOICES_PER_IMAGE = 256;
    private static final int TILE_DIMENSION = 8;
    private static final int OCTETS_PER_TILE = 16;

    private static final int NUMBER_OF_SPRITES = 40;
    private static final int NUMBER_OF_OCTETS_PER_SPRITE = AddressMap.OAM_RAM_SIZE
            / NUMBER_OF_SPRITES;
    private static final int MAX_NUMBER_OF_SPRITES_PER_LINE = 10;
    private static final int Y_AXIS_DELAY = 16;
    private static final int X_AXIS_DELAY = 8;

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
        BACKGROUND, WINDOW, SPRITE_BG, SPRITE_FG
    }

    private enum SpriteAttribute {
        Y, X, TILE, SPECIAL
    }

    private enum SPECIALBit implements Bit {
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
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
                regs.set(Reg.STAT, (regs.get(Reg.STAT) & 0b00000111)
                        | (data & 0b11111000));
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

        if (copyDestination != AddressMap.OAM_END) {
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

        case 0:
        case MODE0_CYCLES + MODE2_CYCLES + MODE3_CYCLES:
            if (regs.get(Reg.LY) == 0) {
                setMode(Mode.M2);
                nextImageBuilder = new Builder(LCD_WIDTH, LCD_HEIGHT);
            }
            if (getMode() != Mode.M1) {
                if (regs.get(Reg.LY) == LCD_HEIGHT) {
                    setMode(Mode.M1);
                    currentImage = nextImageBuilder.build();
                    winY = 0;
                } else
                    setMode(Mode.M2);
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
        int adjustedWX = Math.max(regs.get(Reg.WX) - 7, 0); // TODO mettre 7
                                                            // dans une
        // constante
        if (bitLineInLCD < LCD_HEIGHT) {

            int bitLine = (bitLineInLCD + regs.get(Reg.SCY)) % IMAGE_DIMENSION;

            LcdImageLine finalLine = new LcdImageLine.Builder(LCD_WIDTH)
                    .build();

            // Background management

            if (testLCDCBit(LCDCBit.BG)) {
                finalLine = backgroundLine(bitLine)
                        .extractWrapped(regs.get(Reg.SCX), LCD_WIDTH);
            }

            // Window management

            if (testLCDCBit(LCDCBit.WIN)
                    && (adjustedWX >= 0 && adjustedWX < LCD_WIDTH)
                    && regs.get(Reg.WY) <= bitLineInLCD) {
                LcdImageLine lineOfZeros = new LcdImageLine.Builder(
                        IMAGE_DIMENSION).build();
                LcdImageLine adjustedWindowLine = lineOfZeros
                        .below(windowLine(winY),
                                new BitVector(IMAGE_DIMENSION, true))
                        .shift(adjustedWX).extractWrapped(0, LCD_WIDTH);

                finalLine = finalLine.join(adjustedWindowLine, adjustedWX);
                winY++;
            }

            BitVector BGWINOpacity = finalLine.opacity();
            LcdImageLine BGSprites = new LcdImageLine.Builder(LCD_WIDTH)
                    .build();
            BitVector BGSpritesOpacity = new BitVector(LCD_WIDTH);

            // Sprites management

            if (testLCDCBit(LCDCBit.OBJ)) {

                int[] allSprites = spritesIntersectingLine(bitLineInLCD);

                BGSprites = backGroundSprites(bitLineInLCD, allSprites);
                BGSpritesOpacity = BGSprites.opacity();

                LcdImageLine FGSprites = foreGroundSprites(bitLineInLCD,
                        allSprites);

                finalLine = finalLine.below(FGSprites);
            }

            // This prevents background sprites and background/window image bits to be
            // both transparents
            BitVector bothTransparents = BGSpritesOpacity.or(BGWINOpacity)
                    .not();
            finalLine = BGSprites.below(finalLine,
                    bothTransparents.or(BGWINOpacity));

            if (!finalLine.opacity().equals(new BitVector(LCD_WIDTH, true)))
                throw new Error();

            nextImageBuilder.setLine(bitLineInLCD, finalLine);
        }
        updateLYForNewLine();
    }

    private LcdImageLine backgroundLine(int bitLine) {
        return extractLine(bitLine, ImageType.BACKGROUND)
                .mapColors(regs.get(Reg.BGP));
    }

    private LcdImageLine windowLine(int bitLine) {
        return extractLine(bitLine, ImageType.WINDOW);
    }

    private LcdImageLine extractLine(int bitLine, ImageType type) {
        int lineOfTheTile = bitLine / Byte.SIZE;

        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(
                IMAGE_DIMENSION);

        int slot;

        switch (type) {
        case BACKGROUND:
            slot = testLCDCBit(LCDCBit.BG_AREA) ? 1 : 0;
            break;
        case WINDOW:
            slot = testLCDCBit(LCDCBit.WIN_AREA) ? 1 : 0;
            break;
        default:
            throw new Error();
        }

        for (int i = 0; i < IMAGE_DIMENSION / Byte.SIZE; ++i) {

            int tileIndexInRam = tileIndexInRam(i, lineOfTheTile)
                    + AddressMap.BG_DISPLAY_DATA[slot];

            int tileName = videoRam.read(tileIndexInRam);
            int lineInTheTile = bitLine % (OCTETS_PER_TILE / 2);

            int lsb = getTileLineLsb(lineInTheTile, tileName);
            int msb = getTileLineMsb(lineInTheTile, tileName);

            lineBuilder.setBytes(i, msb, lsb);
        }

        return lineBuilder.build();
    }

    private int tileIndexInRam(int tileX, int tileY) {

        int nbTileInALine = IMAGE_DIMENSION / Byte.SIZE;

        return tileY * nbTileInALine + tileX;
    }

    private int getTileLineAddress(int line, int tileName, boolean isSprite) {
        Preconditions.checkArgument(line >= 0 && line < TILE_DIMENSION * 2);

        if (line >= 0 && line < TILE_DIMENSION) {
            int tileAddress;
            int tileInterval = TILES_CHOICES_PER_IMAGE / 2;

            if (tileName < tileInterval) {
                tileAddress = testLCDCBit(LCDCBit.TILE_SOURCE) || isSprite
                        ? AddressMap.TILE_SOURCE[1]
                        : (AddressMap.TILE_SOURCE[0]
                                + tileInterval * OCTETS_PER_TILE);
            } else {
                tileAddress = AddressMap.TILE_SOURCE[0];
            }

            tileAddress += (tileName % tileInterval) * OCTETS_PER_TILE
                    + line * 2;
            return tileAddress;
        } else {
            return getTileLineAddress(line - 8, tileName + 1, true);
        }
    }

    private int getTileLineMsb(int lineInTheTile, int tileName) {
        return getTileLineMsb(lineInTheTile, tileName, false);
    }

    private int getTileLineMsb(int lineInTheTile, int tileName,
            boolean isSprite) {
        return Bits.reverse8(videoRam.read(
                getTileLineAddress(lineInTheTile, tileName, isSprite) + 1));
    }

    private int getTileLineLsb(int line, int tileName) {
        return getTileLineLsb(line, tileName, false);
    }

    private int getTileLineLsb(int line, int tileName, boolean isSprite) {
        return Bits.reverse8(
                videoRam.read(getTileLineAddress(line, tileName, isSprite)));
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
        if (!testLCDCBit(LCDCBit.LCD_STATUS)) {
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
        int oldMode = regs.get(Reg.STAT);
        int mode = m.ordinal();

        setSTATBit(STATBit.MODE0, (mode % 2) == 1);
        setSTATBit(STATBit.MODE1, (mode / 2) == 1);

        if (m == Mode.M1) {
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
        checkSTAT(oldMode);
    }

    private int[] spritesIntersectingLine(int lcdLine) {
        int[] sprites = new int[MAX_NUMBER_OF_SPRITES_PER_LINE];
        Arrays.fill(sprites, Integer.MAX_VALUE);
        int j = 0;
        for (int index = 0; index < NUMBER_OF_SPRITES
                && j < MAX_NUMBER_OF_SPRITES_PER_LINE; index++) {
            int yCood = getAttribute(index, SpriteAttribute.Y) - Y_AXIS_DELAY;
            if (lcdLine >= yCood && lcdLine < yCood + TILE_DIMENSION
                    * (testLCDCBit(LCDCBit.OBJ_SIZE) ? 2 : 1)) {
                int xCood = getAttribute(index, SpriteAttribute.X);
                sprites[j] = Bits.make16(xCood, index);
                j++;
            }
        }
        Arrays.sort(sprites);

        for (int i = 0; i < sprites.length; i++) {
            if (sprites[i] != Integer.MAX_VALUE)
                sprites[i] = Bits.clip(Byte.SIZE, sprites[i]);
        }

        return sprites;
    }

    private int getAttribute(int spriteIndex, SpriteAttribute att) {
        int address = AddressMap.OAM_START
                + spriteIndex * NUMBER_OF_OCTETS_PER_SPRITE;
        switch (att) {
        case Y:
            return OAM.read(address);
        case X:
            return OAM.read(address + 1);
        case TILE:
            return OAM.read(address + 2);
        case SPECIAL:
            return OAM.read(address + 3);
        default:
            throw new Error();
        }
    }

    private boolean testSPECIALbit(int spriteIndex, SPECIALBit bit) {
        return Bits.test(getAttribute(spriteIndex, SpriteAttribute.SPECIAL),
                bit);
    }

    private int[] depthSprites(int[] allSprites, boolean bg) {
        int[] sprites = new int[MAX_NUMBER_OF_SPRITES_PER_LINE];
        Arrays.fill(sprites, Integer.MAX_VALUE);
        int j = 0;
        for (int index : allSprites) {
            if (index != Integer.MAX_VALUE
                    && testSPECIALbit(index, SPECIALBit.BEHIND_BG) == bg) {
                sprites[j] = index;
                ++j;
            }
        }
        return sprites;
    }

    private LcdImageLine backGroundSprites(int bitLineInLcd, int[] allSprites) {
        return combinedSprites(bitLineInLcd, allSprites, true);
    }

    private LcdImageLine foreGroundSprites(int bitLineInLcd, int[] allSprites) {
        return combinedSprites(bitLineInLcd, allSprites, false);
    }

    private LcdImageLine combinedSprites(int bitLineInLcd, int[] allSprites,
            boolean bg) {
        int[] sprites = depthSprites(allSprites, bg);

        LcdImageLine combinedSprites = new LcdImageLine.Builder(LCD_WIDTH)
                .build();
        for (int sprite : sprites) {
            if (sprite != Integer.MAX_VALUE)
                combinedSprites = individualSprite(sprite, bitLineInLcd)
                        .below(combinedSprites);
        }
        return combinedSprites;
    }

    private LcdImageLine individualSprite(int spriteIndex, int lineInLcd) {
        LcdImageLine.Builder b = new LcdImageLine.Builder(LCD_WIDTH);
        int lineInTheTile = lineInLcd
                - getAttribute(spriteIndex, SpriteAttribute.Y) + Y_AXIS_DELAY;

        if (testSPECIALbit(spriteIndex, SPECIALBit.FLIP_V)) {
            lineInTheTile = (testLCDCBit(LCDCBit.OBJ_SIZE) ? 2 * TILE_DIMENSION
                    : TILE_DIMENSION) - 1 - lineInTheTile;
        }

        int msb = getTileLineMsb(lineInTheTile,
                getAttribute(spriteIndex, SpriteAttribute.TILE), true);
        int lsb = getTileLineLsb(lineInTheTile,
                getAttribute(spriteIndex, SpriteAttribute.TILE), true);

        if (testSPECIALbit(spriteIndex, SPECIALBit.FLIP_H)) {
            msb = Bits.reverse8(msb);
            lsb = Bits.reverse8(lsb);
        }
        b.setBytes(0, msb, lsb);

        int palette = testSPECIALbit(spriteIndex, SPECIALBit.PALETTE)
                ? regs.get(Reg.OBP1)
                : regs.get(Reg.OBP0);

        return b.build().mapColors(palette).shift(
                getAttribute(spriteIndex, SpriteAttribute.X) - X_AXIS_DELAY);
    }
}
