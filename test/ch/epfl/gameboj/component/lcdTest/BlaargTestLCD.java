package ch.epfl.gameboj.component.lcdTest;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.debug.DebugMain2;

public class BlaargTestLCD {
    @Test
    void test2() throws IOException {

         DebugMain2.main(new String[] {"flappyboy.gb","30000000"});
         // DebugMain2.main(new String[] {"Tetris(JUE) (V1.1) [!].gb","30000000"});
        // DebugMain2.main(new String[] {"01-special.gb", "30000000"});
        // DebugMain2.main(new String[] {"02-interrupts.gb", "30000000"});
        // DebugMain2.main(new String[] {"03-op sp,hl.gb", "30000000"});
        // DebugMain2.main(new String[] {"04-op r,imm.gb", "30000000"});
        // DebugMain2.main(new String[] {"05-op rp.gb", "30000000"});
        // DebugMain2.main(new String[] {"06-ld r,r.gb", "30000000"});
        // DebugMain2.main(new String[] {"07-jr,jp,call,ret,rst.gb",
        // "30000000"});
        // DebugMain2.main(new String[] {"08-misc instrs.gb", "30000000"});
        // DebugMain2.main(new String[] {"09-op r,r.gb", "30000000"});
        // DebugMain2.main(new String[] {"10-bit ops.gb", "30000000"});
        // DebugMain2.main(new String[] {"11-op a,(hl).gb", "30000000"});
        // DebugMain2.main(new String[] {"instr_timing.gb", "30000000"});
         //DebugMain2.main(new String[] { "tasmaniaStory.gb", "30000000" });
    }
}
