package ch.epfl.gameboj.component.cpu;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;

public final class Cpu implements Component, Clocked {

    private RegisterFile<Reg> file;
    private int SP;
    private int PC;
    private Bus bus;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);

    private long nextNonIdleCycle;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    public Cpu() {

        file = new RegisterFile<Reg>(Reg.values());
        SP = 0;
        PC = 0;
        nextNonIdleCycle = 0;

        // // TODO enlever ca c'est tres important c'est pour les tests!!!!!!!!
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

        if (cycle < nextNonIdleCycle) {
            return;
        } else {
            int nextInstruction = read8(PC);

            Opcode instruction = null;

            if (nextInstruction != 0xCB) {
                for (Opcode o : DIRECT_OPCODE_TABLE) {
                    if (o.encoding == nextInstruction) {
                        instruction = o;
                    }
                }
            } else {
                nextInstruction = read8(++PC);
                for (Opcode o : PREFIXED_OPCODE_TABLE) {
                    if (o.encoding == nextInstruction) {
                        instruction = o;
                    }
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
            file.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
        }

            break;
        case LD_A_CR: {
            file.set(Reg.A, read8(AddressMap.REGS_START + file.get(Reg.C)));
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
            write8(AddressMap.REGS_START + read8AfterOpcode(), file.get(Reg.A));
        }
            break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + file.get(Reg.C), file.get(Reg.A));
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
            write16(read16AfterOpcode(), SP);
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

        case ADD_A_R8: {
            setRegFlags(Reg.A,
                    Alu.add(file.get(Reg.A),
                            file.get(extractReg(instruction, 0)),
                            !combineCAndBit3(instruction)));
        }
            break;
        case ADD_A_N8: {
            setRegFlags(Reg.A, Alu.add(file.get(Reg.A), read8AfterOpcode(),
                    !combineCAndBit3(instruction)));
        }
            break;
        case ADD_A_HLR: {
            setRegFlags(Reg.A, Alu.add(file.get(Reg.A), read8AtHl(),
                    !combineCAndBit3(instruction)));
        }
            break;
        case INC_R8: {
            Reg reg = extractReg(instruction, 3);
            int valueFlags = Alu.add(file.get(reg), 1);
            file.set(reg, Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case INC_HLR: {
            int valueFlags = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case INC_R16SP: {
            Reg16 reg = extractReg16(instruction);

            if (reg == Reg16.AF) {
                SP += 1;
            } else {
                setReg16SP(reg, Alu.unpackValue(Alu.add16H(reg16(reg), 1)));
            }
        }
            break;
        case ADD_HL_R16SP: {
            Reg16 reg = extractReg16(instruction);
            int valueFlags = Alu.add16H(reg16(Reg16.HL),
                    reg == Reg16.AF ? SP : reg16(reg));
            setReg16SP(Reg16.HL, Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;
        case LD_HLSP_S8: {
            int signedValue = Bits.clip(16,
                    Bits.signExtend8(read8AfterOpcode()));
            int valueFlags = Alu.add16L(SP, signedValue);

            if (Bits.test(instruction.encoding, 4)) {
                setReg16(Reg16.HL, Alu.unpackValue(valueFlags));
            } else {
                setReg16SP(Reg16.AF, Alu.unpackValue(valueFlags));
            }
            combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;

        // Subtract
        case SUB_A_R8: {// TESTER
            setRegFlags(Reg.A,
                    Alu.sub(file.get(Reg.A),
                            file.get(extractReg(instruction, 0)),
                            !combineCAndBit3(instruction)));
        }
            break;
        case SUB_A_N8: {// TESTER
            setRegFlags(Reg.A, Alu.sub(file.get(Reg.A), read8AfterOpcode(),
                    !combineCAndBit3(instruction)));
        }
            break;
        case SUB_A_HLR: {// TESTER
            setRegFlags(Reg.A, Alu.sub(file.get(Reg.A), read8AtHl(),
                    !combineCAndBit3(instruction)));
        }
            break;
        case DEC_R8: {// TESTER
            Reg reg = extractReg(instruction, 3);
            int valueFlags = Alu.sub(file.get(reg), 1);
            file.set(reg, Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case DEC_HLR: {// TESTER
            int valueFlags = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(valueFlags));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case CP_A_R8: {// TESTER
            int valueFlags = Alu.sub(file.get(Reg.A),
                    file.get(extractReg(instruction, 0)));
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;
        case CP_A_N8: {// TESTER
            int valueFlags = Alu.sub(file.get(Reg.A), read8AfterOpcode());
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU,
                    FlagSrc.ALU);

        }
            break;
        case CP_A_HLR: {// TESTER
            int valueFlags = Alu.sub(file.get(Reg.A), read8AtHl());
            combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;
        case DEC_R16SP: {// TESTER
            Reg16 reg = extractReg16(instruction);
            setReg16SP(reg,
                    Alu.unpackValue(Alu.add16H(reg16(reg), Bits.clip(16, -1))));
        }
            break;

        // Sophie
        // And, or, xor, complement
        case AND_A_N8: {
            setRegFlags(Reg.A, Alu.and(file.get(Reg.A), read8AfterOpcode()));
        }
            break;
        case AND_A_R8: {
            setRegFlags(Reg.A, Alu.and(file.get(Reg.A),
                    file.get(extractReg(instruction, 0))));
        }
            break;
        case AND_A_HLR: {
            setRegFlags(Reg.A, Alu.and(file.get(Reg.A), read8AtHl()));
        }
            break;
        case OR_A_R8: {
            setRegFlags(Reg.A, Alu.or(file.get(Reg.A),
                    file.get(extractReg(instruction, 0))));
        }
            break;
        case OR_A_N8: {
            setRegFlags(Reg.A, Alu.or(file.get(Reg.A), read8AfterOpcode()));
        }
            break;
        case OR_A_HLR: {
            setRegFlags(Reg.A, Alu.or(file.get(Reg.A), read8AtHl()));
        }
            break;
        case XOR_A_R8: {
            setRegFlags(Reg.A, Alu.xor(file.get(Reg.A),
                    file.get(extractReg(instruction, 0))));
        }
            break;
        case XOR_A_N8: {
            setRegFlags(Reg.A, Alu.xor(file.get(Reg.A), read8AfterOpcode()));
        }
            break;
        case XOR_A_HLR: {
            setRegFlags(Reg.A, Alu.xor(file.get(Reg.A), read8AtHl()));
        }
            break;
        case CPL: {
            file.set(Reg.A, Bits.complement8(file.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            setRegFlags(Reg.A,
                    Alu.rotate(extractRotDir(instruction), file.get(Reg.A)));
            combineAluFlags(0, FlagSrc.V0, FlagSrc.CPU, FlagSrc.CPU,
                    FlagSrc.CPU);
        }
            break;
        case ROTA: {
            setRegFlags(Reg.A, Alu.rotate(extractRotDir(instruction),
                    file.get(Reg.A), c()));
            combineAluFlags(0, FlagSrc.V0, FlagSrc.CPU, FlagSrc.CPU,
                    FlagSrc.CPU);
        }
            break;
        case ROTC_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg,
                    Alu.rotate(extractRotDir(instruction), file.get(reg)));
        }
            break;
        case ROT_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg,
                    Alu.rotate(extractRotDir(instruction), file.get(reg), c()));
        }
            break;
        case ROTC_HLR: {
            write8AtHlAndSetFlags(
                    Alu.rotate(extractRotDir(instruction), read8AtHl()));
        }
            break;
        case ROT_HLR: {
            write8AtHlAndSetFlags(
                    Alu.rotate(extractRotDir(instruction), read8AtHl(), c()));
        }
            break;
        case SWAP_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg, Alu.swap(file.get(reg)));
        }
            break;
        case SWAP_HLR: {
            write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
        }
            break;
        case SLA_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg, Alu.shiftLeft(file.get(reg)));
        }
            break;
        case SRA_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg, Alu.shiftRightA(file.get(reg)));
        }
            break;
        case SRL_R8: {
            Reg reg = extractReg(instruction, 0);
            setRegFlags(reg, Alu.shiftRightA(file.get(reg)));
        }
            break;
        case SLA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftLeft(read8AtHl()));
        }
            break;
        case SRA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightA(read8AtHl()));
        }
            break;
        case SRL_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightL(read8AtHl()));
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            combineAluFlags(
                    Alu.testBit(file.get(extractReg(instruction, 0)),
                            extractBitIndex(instruction)),
                    FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case BIT_U3_HLR: {
            combineAluFlags(
                    Alu.testBit(read8AtHl(), extractBitIndex(instruction)),
                    FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case CHG_U3_R8: {
            Reg reg = extractReg(instruction, 0);
            file.set(reg, Bits.set(file.get(reg), extractBitIndex(instruction),
                    extractOneOrZero(instruction)));
        }
            break;
        case CHG_U3_HLR: {
            write8AtHl(Bits.set(read8AtHl(), extractBitIndex(instruction),
                    extractOneOrZero(instruction)));
        }
            break;

        // Misc. ALU
        case DAA: {
            int aluResult = Alu.bcdAdjust(file.get(Reg.A), n(), h(), c());
            setRegFromAlu(Reg.A, aluResult);
            combineAluFlags(aluResult, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case SCCF: {
            FlagSrc C = combineCAndBit3(instruction) ? FlagSrc.V1 : FlagSrc.V0;
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, C);
        }
            break;

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

    private int read8(int address) {
        Preconditions.checkBits16(address);
        int value = bus.read(address);
        Preconditions.checkBits8(value);
        return value;
    };

    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    };

    private int read8AfterOpcode() {
        return read8(PC + 1);
    };

    private int read16(int address) {

        int low = read8(address);
        int high = read8(address + 1);

        return Bits.make16(high, low);
    };

    private int read16AfterOpcode() {
        return read16(PC + 1);
    };

    private void write8(int address, int v) {
        bus.write(address, v);
    };

    private void write16(int address, int v) {
        int high = Bits.extract(v, Byte.SIZE, Byte.SIZE);
        int low = Bits.clip(Byte.SIZE, v);

        write8(address, low);
        write8(address + 1, high);
    };

    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    };

    private void push16(int v) {
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

    private int pop16() {

        switch (SP) {
        case 0xFFFE:
            SP = 0;
            return read16(0xFFFE);
        case 0xFFFF:
            SP = 1;
            return read16(0xFFFF);
        default:
            SP += 2;
            return read16(SP - 2);
        }
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

    private int extractHlIncrement(Opcode opcode) {
        boolean reg = Bits.test(opcode.encoding, 4);
        if (reg) {
            return -1;
        }
        return 1;
    };

    private void incrementOrDecrementHl(Opcode opcode) {
        int newValue = reg16(Reg16.HL) + extractHlIncrement(opcode);
        setReg16(Reg16.HL, Bits.clip(Short.SIZE, newValue));
    }

    /*
     * --------------------- Registers Managements -------------------------
     */
    private int reg16(Reg16 r) {
        Reg r1 = Reg.values()[r.index() * 2];
        Reg r2 = Reg.values()[r.index() * 2 + 1];

        return Bits.make16(file.get(r1), file.get(r2));
    }

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
    }

    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }

    }

    private void update(Opcode opcode) {
        nextNonIdleCycle += opcode.cycles;
        PC += opcode.totalBytes;
    }

    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] allOpcodes = Opcode.values();

        ArrayList<Opcode> opcodesOfAKind = new ArrayList<Opcode>();

        for (Opcode o : allOpcodes) {
            if (o.kind == kind) {
                opcodesOfAKind.add(o);
            }
        }

        return opcodesOfAKind.toArray(new Opcode[opcodesOfAKind.size()]);
    }

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

    // ---Flags ToolBox----

    private void setRegFromAlu(Reg r, int vf) {
        file.set(r, Alu.unpackValue(vf));
    }

    private void setFlags(int valueFlags) {
        file.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
        int aluFlags = Alu.unpackFlags(vf);
        int cpuFlags = file.get(Reg.F);

        int maskZ = flagMask(Bits.test(cpuFlags, 7), Bits.test(aluFlags, 7), z,
                7);
        int maskN = flagMask(Bits.test(cpuFlags, 6), Bits.test(aluFlags, 6), n,
                6);
        int maskH = flagMask(Bits.test(cpuFlags, 5), Bits.test(aluFlags, 5), h,
                5);
        int maskC = flagMask(Bits.test(cpuFlags, 4), Bits.test(aluFlags, 4), c,
                4);

        file.set(Reg.F, maskZ | maskN | maskH | maskC);
    }

    private int flagMask(boolean cpuFlag, boolean aluFlag, FlagSrc i,
            int index) {
        Preconditions.checkArgument(index > 3 && index < 8);

        int bit = 3;

        switch (i) {
        case V0:
            bit = 0;
            break;
        case V1:
            bit = 1;
            break;
        case ALU:
            bit = aluFlag ? 1 : 0;
            break;
        case CPU:
            bit = cpuFlag ? 1 : 0;
            break;
        }

        if (bit == 1) {
            return Bits.mask(index);
        }
        return 0;
    }

    // --- instruction informations ---

    private RotDir extractRotDir(Opcode instruction) {
        if (Bits.test(instruction.encoding, 3)) {
            return RotDir.RIGHT;
        }
        return RotDir.LEFT;
    }

    private int extractBitIndex(Opcode instruction) {
        return Bits.extract(instruction.encoding, 3, 3);
    }

    private boolean extractOneOrZero(Opcode instruction) {
        return Bits.test(instruction.encoding, 6);
    }

    private boolean combineCAndBit3(Opcode instruction) {
        return !(Bits.test(instruction.encoding, 3) && c());
    }

    private boolean z() {
        return Bits.test(file.get(Reg.F), 7);
    }

    private boolean n() {
        return Bits.test(file.get(Reg.F), 6);
    }

    private boolean h() {
        return Bits.test(file.get(Reg.F), 5);
    }

    private boolean c() {
        return Bits.test(file.get(Reg.F), 4);
    }
}
