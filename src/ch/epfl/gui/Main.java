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
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class Main extends Application {

    private static int WIDTH = LcdController.LCD_WIDTH * 4;
    private static int HEIGHT = LcdController.LCD_HEIGHT * 4;

    // KeyMap
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

    public static void main(String[] args) {
        Application.launch(args);
    }

    int S = 0;
    int M = 0;
    int delay = 0;

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

            @Override
            public void handle(long currentNanoTime) {
                if(delay == 2) {  
                    delay = 0;
                long elapsedTime = (currentNanoTime - startTime);
                    double elapsedSeconds = elapsedTime / 1e9;

                    long cycle = (long) (elapsedSeconds
                            * gb.NUMBER_OF_CYCLES_PER_SECOND);
                    
                    if(elapsedSeconds> S) {
                        System.out.println(elapsedSeconds);
                        S++;
                        System.out.println("nb appels " + M);
                    }
                    
                    M++;
                                    
                    scene.setOnKeyPressed(e -> {
                        Key k = getJoypadKey(e);
                                
                        if (k != null) {
                            gb.joypad().keyPressed(k);
                        }
                    });
                    
                    scene.setOnKeyReleased(e-> {
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
