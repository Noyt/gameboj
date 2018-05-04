package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * A whole Image
 * 
 * @author Arnaud Robert (287964)
 * @author Sophie Du Couedic (260007)
 * 
 */
public final class LcdImage {

    private final List<LcdImageLine> lines;

    private final int width;
    private final int height;

    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        Preconditions.checkArgument(width > 0 && width % 32 == 0);
        Preconditions.checkArgument(height > 0);

        this.width = width;
        this.height = height;

        this.lines = new ArrayList<>();

        for (LcdImageLine line : lines) {
            Preconditions.checkArgument(line.size() == width);
            this.lines.add(line);
        }
    }

    /**
     * Getter for the width of the image
     * 
     * @return the width of the image
     */
    public int width() {
        return width;
    }

    /**
     * Getter for the height of the image
     * 
     * @return the height of the image
     */
    public int height() {
        return height;
    }

    /**
     * Returns the color of a pixel whose index is (x,y) in the form of an
     * integer between 0 and 3
     * 
     * @param x
     *            horizontal axis index
     * @param y
     *            vertical axis index
     * @return the pixel's color
     */
    public int get(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);

        int msb = lines.get(y).msb().testBit(x) ? 1 : 0;
        int lsb = lines.get(y).lsb().testBit(x) ? 1 : 0;

        return (msb << 1) + lsb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
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

    /**
     * Builder pattern
     * 
     * @author Arnaud Robert (287964)
     * @author Sophie Du Couedic (260007)
     * 
     */
    public static final class Builder {

        private List<LcdImageLine> lines;
        private int height;
        private int width;

        /**
         * Creates an LcdImage builder
         * 
         * @param width
         *            the width of the image
         * @param height
         *            the height of the image
         * @throws IndexOutOfBoundsException
         *             if width or height are less than or equal to 0
         */
        public Builder(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IndexOutOfBoundsException();
            }

            this.width = width;
            this.height = height;
            lines = new ArrayList<>();

            int i = 0;
            while (i < height) {
                LcdImageLine.Builder b = new LcdImageLine.Builder(width);
                lines.add(b.build());
                i++;
            }
        }

        /**
         * Changes the line at the given index by replacing it by the line given
         * as parameter. This method can not be used if the builder has already
         * built an image
         * 
         * @param index
         *            the index of the line to change
         * @param newLine
         *            the line's replacement
         * @throws IllegalStateException
         *             if the builder has already built an image
         * @return the current instance of the builder
         */
        public Builder setLine(int index, LcdImageLine newLine) {
            checkIfBuiltAlready();
            Objects.checkIndex(index, height);

            Preconditions.checkArgument(newLine.size() == width);
            lines.set(index, newLine);
            return this;
        }

        /**
         * Builds the LcdImage
         * 
         * @throws IllegalStateException
         *             if the builder has already built an image
         * @return the LcdImage whose construction is now finished
         */
        public LcdImage build() {
            checkIfBuiltAlready();
            LcdImage result = new LcdImage(width, height, lines);
            lines = null;
            return result;
        }

        public void checkIfBuiltAlready() { // TODO
            if (lines == null) {

                System.out.println("already built");
                throw new IllegalStateException();
            }
        }
    }
}
