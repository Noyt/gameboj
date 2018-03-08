package ch.epfl.gameboj.component.cpu;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {

    private RegisterFile<Reg> file;
    // TODO juste de déclarer SP et PC en int ?
    private int SP;
    private int PC;
    private Bus bus;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);

    private long nextNonIdleCycle;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    public Cpu() {

        file = new RegisterFile<Reg>(Reg.values());
        SP = 0;
        PC = 0;
        nextNonIdleCycle = 0;

        // TODO enlever ca c'est tres important c'est pour les tests!!!!!!!!
        file.set(Reg.A, 0xF0);
        file.set(Reg.F, 0xF1);
        file.set(Reg.B, 0xF2);
        file.set(Reg.C, 0xF4);
        file.set(Reg.D, 0xF3);
        file.set(Reg.E, 0xF7);
        file.set(Reg.H, 0xFA);
        file.set(Reg.L, 0xF5);
    }

    @Override
    public void cycle(long cycle) {
        // System.out.println("cycle : " + cycle + " next: " + nextNonIdleCycle
        // + " PC " + PC );

        if (cycle < nextNonIdleCycle) {
            return;
        } else {
            int nextInstruction = read8(PC);

            Opcode instruction = null;

            for (Opcode o : DIRECT_OPCODE_TABLE) {
                if (o.encoding == nextInstruction) {
                    instruction = o;
                }
            }

            dispatch(Objects.requireNonNull(instruction));
        }
    }

    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub
    }

    // TODO quel visibilité?
    private void dispatch(Opcode instruction) {
        switch (instruction.family) {
        case NOP: {
        }
            break;
        case LD_R8_HLR: {
            file.set(extractReg(instruction, 3), read8AtHl());
        }
            break;
        case LD_A_HLRU: {
            file.set(Reg.A, read8AtHl());
            incrementOrDecrementHl(instruction);
        }
            break;
        case LD_A_N8R: {
            file.set(Reg.A, read8(0xFF00 + read8AfterOpcode()));
        }

            break;
        case LD_A_CR: {
            file.set(Reg.A, read8(0xFF00 + file.get(Reg.C)));
        }
            break;
        case LD_A_N16R: {
            file.set(Reg.A, read8(read16AfterOpcode()));
        }
            break;
        case LD_A_BCR: {
            file.set(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_A_DER: {
            file.set(Reg.A, read8(reg16(Reg16.DE)));
        }
            break;
        case LD_R8_N8: {
            file.set(extractReg(instruction, 3), read8AfterOpcode());
        }
            break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(instruction), read16AfterOpcode());
        }
            break;
        case POP_R16: {
            setReg16(extractReg16(instruction), pop16());
        }
            break;
        case LD_HLR_R8: {
            write8AtHl(file.get(extractReg(instruction, 0)));
        }
            break;
        case LD_HLRU_A: {
            write8AtHl(file.get(Reg.A));
            incrementOrDecrementHl(instruction);
        }
            break;
        case LD_N8R_A: {
            write8(0xFF00 + read8AfterOpcode(), file.get(Reg.A));
        }
            break;
        case LD_CR_A: {
            write8(0xFF00 + file.get(Reg.C), file.get(Reg.A));
        }
            break;
        case LD_N16R_A: {
            write8(read16AfterOpcode(), file.get(Reg.A));
        }
            break;
        case LD_BCR_A: {
            write8(reg16(Reg16.BC), file.get(Reg.A));
        }
            break;
        case LD_DER_A: {
            write8(reg16(Reg16.DE), file.get(Reg.A));
        }
            break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        }
            break;
        case LD_N16R_SP: {
            write(read16AfterOpcode(), SP);
        }
            break;
        case LD_R8_R8: {
            Reg r1 = extractReg(instruction, 3);
            Reg r2 = extractReg(instruction, 0);

            if (file.get(r1) != file.get(r2))
                file.set(r1, file.get(r2));
        }
            break;
        case LD_SP_HL: {
            SP = reg16(Reg16.HL);
        }
            break;
        case PUSH_R16: {
            push16(reg16(extractReg16(instruction)));

        }
            break;
        default:
            throw new IllegalArgumentException("TODO"); //TODO
        }

        update(instruction);
    }

    /*
     * ------------------------------ Bus methods -----------------------------
     */
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    // TODO preconditions pour un résultat uniquement 8 bits
    private int read8(int address) {
        Preconditions.checkBits16(address);
        int value = bus.read(address);
        Preconditions.checkBits8(value);
        return value;
    };

    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    };

    private int read8AfterOpcode() { // TESTER
        return read8(PC + 1);
    };

    private int read16(int address) { // TESTER
        
        //TODO important comment faire quand address + 1 déborde de la plage disponible?
        Preconditions.checkBits16(address);

        int low = read8(address);
        int high = read8(address + 1);

        return Bits.make16(high, low);
    };

    private int read16AfterOpcode() { // TESTER
        Preconditions.checkBits16(PC + 1);
        return read16(PC + 1);
    };

    private void write8(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(v);

        bus.write(address, v);
    };

    private void write16(int address, int v) {
        Preconditions.checkBits16(v);

        int high = Bits.extract(v, Byte.SIZE, Byte.SIZE);
        int low = Bits.clip(Byte.SIZE, v);

        write8(address, low);
        write8(address + 1, high);
    };

    private void write8AtHl(int v) {
        Preconditions.checkBits8(v);

        write8(reg16(Reg16.HL), v);
    };

    private void push16(int v) { // TESTER
        Preconditions.checkBits16(v);

        switch (SP) {
        case 1:
            SP = 0xFFFF;
            break;
        case 0:
            SP = 0xFFFE;
            break;
        default:
            SP -= 2;
        }

        write16(SP, v);

    };

    private int pop16() { // TESTER
        SP += 2;
        return read16(SP - 2);
    };

    /*
     * --------------------- Extract information from Opcodes ------------------
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        int reg = Bits.extract(opcode.encoding, startBit, 3);

        switch (reg) {
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b111:
            return Reg.A;
        default:
            throw new IllegalArgumentException("ceci est faux, 110 registre");
        }
    };

    private Reg16 extractReg16(Opcode opcode) {
        int reg = Bits.extract(opcode.encoding, 4, 2);
        switch (reg) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default:
            throw new IllegalArgumentException("TODO");
        }
    }

    private int extractHlIncrement(Opcode opcode) { // TESTER
        boolean reg = Bits.test(opcode.encoding, 4);
        if (reg) {
            return -1;
        }
        return 1;
    };

    private void incrementOrDecrementHl(Opcode opcode) { // TESTER
        setReg16(Reg16.HL, reg16(Reg16.HL) + extractHlIncrement(opcode));
    }

    /*
     * -------------------------- Registers Managements
     * -------------------------
     */
    private int reg16(Reg16 r) {
        Reg r1 = Reg.values()[r.index() * 2];
        Reg r2 = Reg.values()[r.index() * 2 + 1];

        return Bits.make16(file.get(r1), file.get(r2));
    }

    // TODO preconditions sur newV (16/8?)
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);

        if (r == Reg16.AF && Bits.clip(4, newV) > 0) {
            newV = (newV >>> 4) << 4;
        }

        Reg r1 = Reg.values()[r.index() * 2];
        Reg r2 = Reg.values()[r.index() * 2 + 1];

        int highV = Bits.extract(newV, Byte.SIZE, Byte.SIZE);
        int lowV = Bits.clip(Byte.SIZE, newV);

        file.set(r1, highV);
        file.set(r2, lowV);

        // TODO pour les 4 premiers zeros en cas AF, mieux vaut utiliser des
        // méthodes de BIts comme set ou des << >>> ?
    }

    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);

        // TODO est-ce que les 4 bits de poids faibles de SP doivent valoir 0 ??
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }

    }

    private void update(Opcode opcode) { // TESTER
        nextNonIdleCycle += opcode.cycles;
        PC += opcode.totalBytes;
    }

    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) { // TESTER
        Opcode[] allOpcodes = Opcode.values();

        ArrayList<Opcode> opcodesOfAKind = new ArrayList<Opcode>();

        for (Opcode o : allOpcodes) {
            if (o.kind == kind) {
                opcodesOfAKind.add(o);
            }
        }

        return opcodesOfAKind.toArray(new Opcode[opcodesOfAKind.size()]);
    }

    // TODO
    public int[] _testGetPcSpAFBCDEHL() {
        int[] reg = new int[10];

        reg[0] = PC;
        reg[1] = SP;

        Reg[] regTemp = Reg.values();

        for (int i = 2; i < 10; ++i) {
            reg[i] = file.get(regTemp[i - 2]);
        }

        return reg;
    }

}
