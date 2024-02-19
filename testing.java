import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.awt.Color;

public class testing {
    public static void main(String[] args) {
        Picture pic = new Picture("3x4.png");

        Color a = pic.get(0, 0);
        Color b = pic.get(0, 2);

        int c = pic.getRGB(0, 0);
        int d = pic.getRGB(0, 2);


        Color c1 = new Color(a.getRed() - b.getRed(), a.getGreen() - b.getGreen(),
                             a.getBlue() - b.getBlue());
        StdOut.println(c1);

        Color c2 = new Color(c - d);
        StdOut.println(c2);

    }
}
