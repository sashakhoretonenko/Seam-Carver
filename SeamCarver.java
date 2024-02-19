import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.util.BitSet;

public class SeamCarver {
    // stores the picture
    private Picture pic;
    // number of horizontal seams removed
    private int hSeamsRemoved;
    // number of vertical seams removed
    private int vSeamsRemoved;


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new IllegalArgumentException("null argument in constructor");
        // deep copy of the picture
        pic = new Picture(picture);
        hSeamsRemoved = 0;
        vSeamsRemoved = 0;
    }

    // current picture
    public Picture picture() {
        // these will be the height and width of the new picture
        // remember that the height and width methods return the height and
        // width of the current picture minus the number of seams that have
        // been removed
        int newWidth = width();
        int newHeight = height();
        int newCapacity = newWidth * newHeight;

        Picture returnPic = new Picture(newWidth, newHeight);

        // starts at the top left corner and traverses in a left-right, top-down
        // snake-like motion
        int origIndex = 0;
        int newIndex = 0;

        while (newIndex < newCapacity) {
            int rgb = pic.getRGB(x(origIndex), y(origIndex));
            if (rgb != 0) {
                returnPic.setRGB(x(newIndex), y(newIndex), rgb);
                newIndex++;
            }
            origIndex++;
        }

        // updates the picture
        pic = returnPic;

        // resets the number of seams removed to 0
        hSeamsRemoved = 0;
        vSeamsRemoved = 0;

        return pic;
    }

    // width of current picture
    public int width() {
        return pic.width() - vSeamsRemoved;
    }

    // height of current picture
    public int height() {
        return pic.height() - hSeamsRemoved;
    }

    // energy of pixel at column x and row y
    // assumes that this pixel hasn't been removed
    public double energy(int x, int y) {
        if (!(inBoundsX(x) && inBoundsY(y)))
            throw new IllegalArgumentException("x or y is not in bounds");

        int xAbove = getRGB(x, y, true, true);
        int xBelow = getRGB(x, y, false, false);
        int yRight = getRGB(x, y, false, true);
        int yLeft = getRGB(x, y, true, false);

        return Math.sqrt(energyHelper(xBelow, xAbove) +
                                 energyHelper(yLeft, yRight));
    }

    // private helper method for getting the RGB value
    /* boolean combinations:
                up
             T | F
             ------------
    right T | up  right
          F | left down
     */
    private int getRGB(int x, int y, boolean up, boolean right) {
        int oWidth = pic.width();
        int oHeight = pic.height();

        // finds the energy of the pixel above the pixel given
        if (up && right) {
            int yTemp = y - 1;
            int rgb = pic.getRGB(x, (yTemp + oHeight) % oHeight);
            while (rgb == 0) {
                yTemp--;
                rgb = pic.getRGB(x, (yTemp + oHeight) % oHeight);
            }
            return rgb;
        }
        // energy from the right
        else if (right) {
            int xTemp = x + 1;
            int rgb = pic.getRGB(xTemp % oWidth, y);
            while (rgb == 0) {
                xTemp++;
                rgb = pic.getRGB(xTemp % oWidth, y);
            }
            return rgb;
        }
        // energy from the left
        else if (up) {
            int xTemp = x - 1;
            int rgb = pic.getRGB((xTemp + oWidth) % oWidth, y);
            while (rgb == 0) {
                xTemp--;
                rgb = pic.getRGB((xTemp + oWidth) % oWidth, y);
            }
            return rgb;
        }
        // energy from below
        else {
            int yTemp = y + 1;
            int rgb = pic.getRGB(x, yTemp % oHeight);
            while (rgb == 0) {
                yTemp++;
                rgb = pic.getRGB(x, yTemp % oHeight);
            }
            return rgb;
        }
    }

    // helper method for finding the energy of a pixel
    private int energyHelper(int less, int more) {
        int rLess = (less >> 16) & 0xFF;
        int rMore = (more >> 16) & 0xFF;
        int redDiff = (rMore - rLess) * (rMore - rLess);

        int gLess = (less >> 8) & 0xFF;
        int gMore = (more >> 8) & 0xFF;
        int greenDiff = (gMore - gLess) * (gMore - gLess);

        int bLess = less & 0xFF;
        int bMore = more & 0xFF;
        int blueDiff = (bMore - bLess) * (bMore - bLess);

        return redDiff + greenDiff + blueDiff;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int oWidth = pic.width();
        int oHeight = pic.height();
        int width = width();
        int capacity = oWidth * oHeight;

        // keeps track of distances of shortest paths to each pixel
        double[][] distances = new double[oWidth][oHeight];

        // indices are 1d indices of the 2d array
        // keys are distances of the shortest path currently found to that pixel
        IndexMinPQ<Double> pq = new IndexMinPQ<Double>(capacity);

        // enqueues all of the vertices in the first column into the pq
        for (int i = 0; i < oHeight; i++) {
            if (pic.getRGB(0, i) != 0) {
                double energy = energy(0, i);
                pq.insert(oneDIndex(0, i), energy);
                distances[0][i] = energy;
            }
        }

        // keeps track of which vertices have been marked and relaxed
        // 2 * oneDIndex references whether it has been relaxed yet and
        // 2 * oneDIndex + 1 references whether it has been relaxed yet
        BitSet marked = new BitSet(2 * capacity);

        // stores which way to go to previous vertex
        BitSet pixelTo = new BitSet(2 * oHeight * width);

        // stores the current item we have dequeued
        int curr = pq.delMin();
        int xCurr = x(curr);
        int yCurr = y(curr);

        // sets marked and relaxed of that the dequeued vertex to true
        marked.set(2 * curr);
        marked.set(2 * curr + 1);

        while (xCurr < width) {

        }


    }

    // private helper method for relaxing vertices when finding an hSeam

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return null;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {

    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {

    }

    // returns the row of a one-dimensional index
    private int x(int index) {
        return index % width();
    }

    // returns the column of a one-dimensional index
    private int y(int index) {
        return index / width();
    }

    // checks if x is inbounds
    private boolean inBoundsX(int x) {
        return x >= 0 && x < pic.width();
    }

    // checks if y is inbounds
    private boolean inBoundsY(int y) {
        return y >= 0 && y < pic.height();
    }

    // formula for converting a pixel into its 1-dimensional array index
    private int oneDIndex(int x, int y) {
        return width() * y + x;
    }


    //  unit testing (required)
    public static void main(String[] args) {
        Picture pic = new Picture("3x4.png");
        pic.setRGB(1, 3, 0);
        pic.setRGB(1, 1, 0);
        pic.setRGB(2, 2, 0);

        SeamCarver s = new SeamCarver(pic);

        StdOut.println(s.energy(1, 2));
    }

}
