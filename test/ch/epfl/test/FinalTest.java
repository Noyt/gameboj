package ch.epfl.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import ch.epfl.gui.Main;

public class FinalTest {
    @Test
    void test() throws IOException {
        Main.main(new String[] {"tasmaniaStory.gb"});
        //Main.main(new String[] { "finalflappyboy.gb"});
    }
}
