package ch.epfl.gui;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public final class Main extends Application {

    private static String ROM_PATH;
    
    public static void main(String[] args) {
        Preconditions.checkArgument(args.length == 1);
        Application.launch(args);
        ROM_PATH = args[0];
    }
    
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        //Create GameBoy
        if(getParameters().getRaw().size() != 1) {
            System.exit(1);
        }
        File romFile = new File(ROM_PATH);
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        
        //Creates GUI
        ImageView imageView = new ImageView();
        Image image = getImage(gb);
        imageView.setFitWidth(image.getWidth()*2);
        imageView.setFitHeight(image.getHeight()*2);
        imageView.setImage(image);
    }
    
    private static final Image getImage(GameBoy gb) {
        return ImageConverter.convert(gb.lcdController().currentImage());
    }
}
