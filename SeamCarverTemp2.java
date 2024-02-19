import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.BitSet;
import java.util.Random;

// finds optimal seams in images
public class SeamCarverTemp2 {
    // stores picture
    private Picture pic;
    // stores the amount of horizontal seams that have been removed
    private int hSeamsRemoved;
    // stores the amount of vertical seams that have been removed
    private int vSeamsRemoved;

    // create a seam carver object based on the given picture
    public SeamCarverTemp2(Picture picture) {
        if (picture == null)
            throw new IllegalArgumentException("null argument in constructor");
        // deep copy of the picture
        pic = new Picture(picture);
        hSeamsRemoved = 0;
        vSeamsRemoved = 0;
    }

    // current picture
    public Picture picture() {
        int origWidth = width();
        int origHeight = height();

        int newWidth = origWidth - vSeamsRemoved;
        int newHeight = origHeight - hSeamsRemoved;

        Picture returnPic = new Picture(newWidth, newHeight);

        // traverses through the picture that has seams removed, skipping pixels
        // that have a color integer of -1
        int newPicOneD = 0;

        for (int i = 0; i < origWidth; i++) {
            for (int j = 0; j < origHeight; j++) {
                int rgb = pic.getRGB(i, j);
                if (rgb >= 0) {
                    int x = x(newPicOneD);
                    int y = y(newPicOneD);
                    returnPic.setRGB(x, y, rgb);
                }
            }
        }

        // resets the picture to the new image
        pic = returnPic;

        // resets the number of horizontal and vertical seams we've removed
        // to 0
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
    public double energy(int x, int y) {
        if (!inBounds(x, y))
            throw new IllegalArgumentException("Outside of prescribed range");

        int width = width();
        int height = height();

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
        int origWidth = width() + vSeamsRemoved;
        int origHeight = height() + hSeamsRemoved;
        int rgb;

        // energy from above
        if (up && right) {
            int yTemp = y - 1;
            rgb = pic.getRGB(x, (yTemp + origWidth) % origWidth);
            while (rgb == 0) {
                yTemp--;
                rgb = pic.getRGB(x, (yTemp + origWidth) % origWidth);
            }
            return rgb;
        }
        // energy from the right
        else if (right) {
            int xTemp = x;
            rgb = pic.getRGB((xTemp + 1) % origWidth, y);
            while (rgb == 0) {
                xTemp++;
                rgb = pic.getRGB((xTemp + 1) % origWidth, y);
            }
            return rgb;
        }
        // energy from the left
        else if (up) {
            int xTemp = x;
            rgb = pic.getRGB((xTemp + origWidth - 1) % origWidth, y);
            while (rgb == 0) {
                xTemp--;
                rgb = pic.getRGB((xTemp + origWidth - 1) % origWidth, y);
            }
            return rgb;
        }
        // energy from below
        else {
            int yTemp = y;
            rgb = pic.getRGB(x, yTemp + 1 % origHeight);
            while (rgb == 0) {
                yTemp++;
                rgb = pic.getRGB(x, (yTemp + 1) % origHeight);
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
        int width = width();
        int height = height();
        int origWidth = width + vSeamsRemoved;
        int origHeight = height + hSeamsRemoved;

        int capacity = origWidth * origHeight;

        // keeps track of distances of shortest paths to each pixel
        double[][] distances = new double[width][height];

        // IndexMinPQ with keys that are the shortest path length
        // and values of the pixel in the picture. To assign values to the 2d
        // array, we use a formula to convert it to a 1d array
        IndexMinPQ<Double> pq = new IndexMinPQ<Double>(capacity);

        // enqueues all of the vertices in the first column into the pq
        for (int i = 0; i < origHeight; i++) {
            if (pic.getRGB(0, i) != 0) {
                double energy = energy(0, i);
                pq.insert(oneDIndex(0, i), energy);
                distances[0][i] = energy;
            }
        }

        // keeps track whether pixel has been dequeued from the array
        BitSet marked = new BitSet(2 * capacity);

        // stores which way to go to previous vertex
        BitSet pixelTo = new BitSet(capacity);

        // stores the current item we have dequeued
        int curr = pq.delMin();
        int xCurr = x(curr);
        int yCurr = y(curr);

        // keeps track that we have already visited the initial vertex
        marked.set(curr * 2 + 1);

        // runs Djikstra's algorithm until we reach the right side of the pic
        while (xCurr < width - 1) {

            // relax upper right
            relax(curr, -1, pq, marked, distances, pixelTo, true);
            // relax direct right
            relax(curr, 0, pq, marked, distances, pixelTo, true);
            // relax lower right
            relax(curr, 1, pq, marked, distances, pixelTo, true);

            // updates to store the index we're currently working with
            curr = pq.delMin();
            xCurr = x(curr);
            yCurr = y(curr);

            // marks that we have given the new vertex
            marked.set(curr * 2 + 1);
        }

        // calculates the seam
        int[] seam = new int[width];

        seam[width - 1] = yCurr;
        for (int i = width - 1; i > 0; i--) {
            if (pixelTo.get(2 * i)) {
                yCurr--;
                seam[i] = yCurr;
            }
            else if (pixelTo.get(2 * i + 1)) {
                yCurr++;
                seam[i] = yCurr;
            }
            else {
                seam[i] = yCurr;
            }
        }

        return seam;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int width = width();
        int height = height();
        int origWidth = width + vSeamsRemoved;
        int origHeight = height + hSeamsRemoved;

        int capacity = width * height;

        // keeps track of distances of shortest paths to each pixel
        double[][] distances = new double[origWidth][origHeight];

        // IndexMinPQ with keys that are the shortest path length
        // and values of the pixel in the picture. To assign values to the 2d
        // array, we use a formula to convert it to a 1d array
        IndexMinPQ<Double> pq = new IndexMinPQ<Double>(capacity);

        // enqueues all of the vertices in the first row into the pq
        // also initializes all the distances of the top row
        for (int i = 0; i < origWidth; i++) {

            if (pic.getRGB(i, 0) != 0) {
                double energy = energy(i, 0);
                pq.insert(oneDIndex(i, 0), energy);
                distances[i][0] = energy;
            }
        }

        // keeps track whether pixel has been dequeued from the array
        BitSet marked = new BitSet(2 * capacity);

        // stores which way to go to previous vertex
        BitSet pixelTo = new BitSet(2 * capacity);

        // stores the current item we have dequeued
        int curr = pq.delMin();
        int xCurr = x(curr);
        int yCurr = y(curr);

        marked.set(2 * curr + 1);
        int mark = 1;

        // runs Djikstra's algorithm until we reach the bottom of the pic

        while (yCurr < height - 1) {

            // relax lower left
            relax(curr, -1, pq, marked, distances,
                  pixelTo, false);
            // relax directly below
            relax(curr, 0, pq, marked, distances,
                  pixelTo, false);
            // relax lower right
            relax(curr, 1, pq, marked, distances,
                  pixelTo, false);

            curr = pq.delMin();
            xCurr = x(curr);
            yCurr = y(curr);

            // marks that we've removed set vertex from pq
            marked.set(2 * curr + 1);

            mark++;
            StdOut.println("marked: " + xCurr + ", " + yCurr);
        }

        // calculates the seam
        int[] seam = new int[height];

        seam[height - 1] = xCurr;
        for (int i = width - 1; i > 0; i--) {
            if (pixelTo.get(2 * i)) {
                xCurr--;
                seam[i] = xCurr;
            }
            else if (pixelTo.get(2 * i + 1)) {
                xCurr++;
                seam[i] = xCurr;
            }
            else {
                seam[i] = xCurr;
            }
        }

        return seam;
    }


    // relax helper method
    private void relax(int fromIndex, int direction, IndexMinPQ<Double> pq,
                       BitSet marked, double[][] distances,
                       BitSet pixelTo, boolean hor) {

        int xFrom = x(fromIndex);
        int yFrom = y(fromIndex);
        // declares index we're relaxing to
        int toIndex;
        if (hor) toIndex = oneDIndex(xFrom + 1, yFrom + direction);
        else toIndex = oneDIndex(xFrom + direction, yFrom + 1);


        // corner case
        if (toIndex < 0) return;

        int xTo = x(toIndex);
        int yTo = y(toIndex);
        // checks that vertex we're relaxing is in bounds
        if (!inBounds(xTo, yTo)) return;
        // if vertex has already been marked, no need to relax it
        if (marked.get(2 * toIndex + 1)) return;

        // case if sp to the toIndex hasn't even been calculated yet
        if (marked.get(2 * toIndex)) {
            double relaxDist = distances[xFrom][yFrom] + energy(xTo, yTo);
            distances[xTo][yTo] = relaxDist;
            pq.insert(toIndex, relaxDist);
            // sets the vertex that it pointed to
            if (direction > 0) pixelTo.set(2 * toIndex + 1);
            else if (direction < 0) pixelTo.set(2 * toIndex);

        }
        // case if sp to the toIndex already has a value
        else {
            // case if relaxed distance is less than distance stored
            double origDist = distances[xTo][yTo];
            double relaxDist = distances[xFrom][yFrom] + energy(xTo, yTo);

            if (relaxDist < origDist) {
                pq.changeKey(toIndex, relaxDist);
                distances[xTo][yTo] = relaxDist;
                // sets the vertex that it pointed to
                if (direction > 0) pixelTo.set(2 * toIndex + 1);
                else if (direction < 0) pixelTo.set(2 * toIndex);
            }
        }
    }


    // helper method that checks if index is within our bounds
    private boolean inBounds(int x, int y) {
        if (x < 0 || x >= width()) return false;
        if (y < 0 || y >= height()) return false;
        return true;
    }

    // helper method
    // formula for converting a pixel into its 1-dimensional array index
    private int oneDIndex(int x, int y) {
        // corner case if x or y is out of bounds
        if (!inBounds(x, y)) return -1;
        return width() * y + x;
    }

    // returns the row of a one-dimensional index
    private int x(int index) {
        return index % width();
    }

    // returns the column of a one-dimensional index
    private int y(int index) {
        return index / width();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new IllegalArgumentException("null argument");

        if (height() == 1)
            throw new IllegalArgumentException("Bro you're deleting the image");

        if (seam.length != width())
            throw new IllegalArgumentException("incorrect seam length");

        // corner case
        if (seam.length == 0) return;

        // sets all the rgb integers of pixels in the seam to -1

        int seamPointer = 0;
        int picPointer = 0;

        while (seamPointer < seam.length) {
            int rgb = pic.getRGB(seam[seamPointer], picPointer);
            if (rgb != 0) {
                pic.setRGB(seam[seamPointer], picPointer, 0);
                seamPointer++;
            }
            picPointer++;
        }

        // increments the amount of horizontal seams removed
        hSeamsRemoved++;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new IllegalArgumentException("null argument");

        if (width() == 1)
            throw new IllegalArgumentException("Bro you're deleting the image");

        if (seam.length != height())
            throw new IllegalArgumentException("incorrect seam length");

        if (seam.length == 0) return;

        // sets all the rgb integers of pixels in the seam to -1

        int seamPointer = 0;
        int picPointer = 0;

        while (seamPointer < seam.length) {
            int rgb = pic.getRGB(seam[seamPointer], picPointer);
            if (rgb != 0) {
                pic.setRGB(picPointer, seam[seamPointer], 0);
                seamPointer++;
            }
            picPointer++;
        }

        // increments the amount of vertical seams removed
        vSeamsRemoved++;

        StdOut.println("vertical seam removed");
    }

    //  unit testing (required)
    public static void main(String[] args) {
        // Picture pic = new Picture(args[0]);
        // int hRemove = Integer.parseInt(args[1]);
        // int vRemove = Integer.parseInt(args[2]);
        //
        // SeamCarver s = new SeamCarver(pic);
        //
        //
        // for (int i = 0; i < hRemove; i++) {
        //     int[] hSeam = s.findHorizontalSeam();
        //     s.removeHorizontalSeam(hSeam);
        // }
        //
        // for (int i = 0; i < vRemove; i++) {
        //     int[] vSeam = s.findVerticalSeam();
        //     s.removeVerticalSeam(vSeam);
        // }
        //
        // Picture p = s.picture();
        // p.show();

        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);

        Picture pic = new Picture(width, height);
        Random rand = new Random();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int a = 255;
                int r = rand.nextInt(255);
                int g = rand.nextInt(255);
                int b = rand.nextInt(255);

                int argb = (a << 24) | (r << 16) | (g << 8) | b;

                pic.setRGB(i, j, argb);
            }
        }

        SeamCarver s = new SeamCarver(pic);

        Stopwatch stop = new Stopwatch();
        s.removeVerticalSeam(s.findVerticalSeam());
        s.removeHorizontalSeam(s.findHorizontalSeam());

        StdOut.println(stop.elapsedTime());
    }
}
