package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.gameboj.Preconditions;

public final class LcdImage {

    List<LcdImageLine> lines;
    
    int width;
    int height;
    
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        //TODO est-ce qu'il faut tester que la taille de chaque ligne est la même?
        
        Preconditions.checkArgument(width > 0 && width%32 ==0);
        Preconditions.checkArgument(height > 0 && height%32 == 0);
        
        this.width = width;
        this.height = height;
        
        this.lines = new ArrayList<>(lines);
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public boolean equals(Object that) {
        Preconditions.checkArgument(that instanceof LcdImage);

        LcdImage tmp = (LcdImage) that;
        if (width() != tmp.width || height != tmp.height) {
            return false;
        }

        for (int i = 0; i < height; ++i) {
            if (!lines.get(i).equals(tmp.lines.get(i))) {
                return false;
            }
        }
      
        return true;
    }
    
    public static final class Builder {
        
    }
}
