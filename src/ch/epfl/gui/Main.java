package ch.epfl.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The main programm of the simulation, a JavaFX application
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 */
public final class Main extends Application {

    private final static int WIDTH = LcdController.LCD_WIDTH * 4;
    private final static int HEIGHT = LcdController.LCD_HEIGHT * 4;

    /**
     * TODO this is used in order to slow down the running of the Gameboy
     */
    private static int delay = 0;

    // KeyMaps
    private static Map<KeyCode, Joypad.Key> keyCode = new HashMap<>() {
        {
            put(KeyCode.RIGHT, Key.RIGHT);
            put(KeyCode.LEFT, Key.LEFT);
            put(KeyCode.UP, Key.UP);
            put(KeyCode.DOWN, Key.DOWN);
        }
    };

    private static Map<String, Joypad.Key> keyString = new HashMap<>() {
        {
            put("a", Key.A);
            put("b", Key.B);
            put(" ", Key.SELECT);
            put("s", Key.START);
        }
    };

    /**
     * The main method of the main class that launches the application. This
     * method is called with the name of the given ROM file when we run the
     * program
     * 
     * @param args
     *            : the given argument that has to be the name of the ROM file
     *            of the Gameboy program
     * 
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Implements the method start of Application. Exits if the parameter raw (a
     * list of Strings) of the stage is not of size 1. The methods creates and
     * runs a GameBoy which the cartridge is obtained from the given ROM file,
     * and updates periodically the image displayed on the screen
     * 
     * @param stage
     *            : the primary stage for the application
     *            
     * @throws //TODO
     * 
     * @see javafx.application#start(Stage)
     */
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        // Create GameBoy
        if (getParameters().getRaw().size() != 1) {
            System.exit(1);
        }

        File romFile = new File(getParameters().getRaw().get(0));

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        // Creates GUI
        Image image = getImage(gb);
        ImageView imageView = new ImageView();
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);
        imageView.setImage(image);

        BorderPane borderPane = new BorderPane(imageView);

        Scene scene = new Scene(borderPane, WIDTH, HEIGHT);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Gameboj");
        stage.show();
        stage.requestFocus();

        long startTime = System.nanoTime();
        // Update GameBoy
        AnimationTimer timer = new AnimationTimer() {

            /**
             * Implements the method handle of AnimationTimer, the timer of the
             * animation. The method simulates the progression of the Gameboy,
             * in function of the time, given in nanosecond.
             * 
             * @param currentNanoTime : the current time in nanosecond units
             * 
             * @see javafx.animation#handle(long)
             */
            @Override
            public void handle(long currentNanoTime) {
                if (delay == 2) {
                    delay = 0;
                    long elapsedTime = (currentNanoTime - startTime);
                    double elapsedSeconds = elapsedTime / 1e9;

                    long cycle = (long) (elapsedSeconds
                            * gb.NUMBER_OF_CYCLES_PER_SECOND);

                    scene.setOnKeyPressed(e -> {
                        Key k = getJoypadKey(e);

                        if (k != null) {
                            gb.joypad().keyPressed(k);
                        }
                    });

                    scene.setOnKeyReleased(e -> {
                        Key k = getJoypadKey(e);

                        if (k != null) {
                            gb.joypad().keyReleased(k);
                        }
                    });

                    gb.runUntil(cycle);
                    imageView.setImage(null);
                    imageView.setImage(getImage(gb));
                }
                delay++;
            }
        };

        timer.start();
    }

    private Key getJoypadKey(KeyEvent e) {
        Key k = null;

        k = keyCode.get(e.getCode());

        if (k == null) {
            k = keyString.get(e.getText());
        }

        return k;
    }

    private static final Image getImage(GameBoy gb) {
        return ImageConverter.convert(gb.lcdController().currentImage());
    }

}
