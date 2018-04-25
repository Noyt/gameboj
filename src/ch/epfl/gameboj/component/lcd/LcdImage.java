package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;

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
    
    public int get(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        
        int msb = lines.get(y).msb().testBit(x) ? 1 : 0;
        int lsb = lines.get(y).lsb().testBit(y) ? 1 : 0;
        
        return (msb << 1) + lsb;
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
        
        private List<LcdImageLine> lines;
        private int height;
        private int width;
        
        public Builder(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IndexOutOfBoundsException();
            }
            
            this.height = height;
            this.width = width;
            lines = new ArrayList<>();
            int i = 0;
            while (i < height) {
                LcdImageLine.Builder b = new LcdImageLine.Builder(width);
                lines.add(b.build());
                i++;
            }
        }
        
        public Builder setLine(int index, LcdImageLine newLine) {
            checkIfBuiltAlready();
            System.out.println("index " + index + " height " + height);
            Objects.checkIndex(index, height);
            Preconditions.checkArgument(newLine.size() == width);
            lines.set(index, newLine);
            return this;
        }
        
        public LcdImage build() {
            checkIfBuiltAlready();
            List<LcdImageLine> temp = lines;
            temp = lines;
            lines = null;
            return new LcdImage(width, height, temp);
        }
        
        private void checkIfBuiltAlready() {
            if (lines == null) {
                throw new IllegalStateException();
            }
        }
    }
}
