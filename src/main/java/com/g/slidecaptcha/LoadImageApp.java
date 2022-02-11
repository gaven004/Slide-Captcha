package com.g.slidecaptcha;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

/**
 * This class demonstrates how to load an Image from an external file
 */
public class LoadImageApp extends Component {
    static class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    static final int DEGREE = 85;
    static final int ALPHA = 80;
    static final int WHITE_MASK = 0xff * ALPHA / 100;
    static final int WHITE_MASK_RED = WHITE_MASK << 16;
    static final int WHITE_MASK_GREEN = WHITE_MASK << 8;
    static final int WHITE_MASK_BLUE = WHITE_MASK;

    BufferedImage imgBackground;
    BufferedImage imgPuzzle;
    BufferedImage imgMask;

    public void paint(Graphics g) {
        g.drawImage(imgBackground, 0, 0, null);
    }

    public LoadImageApp() {
        try {
//            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/98a5fd3e80d145b9bf211cdf74d31818_tplv-ovu2ybn2i4-2.jpeg"));
//            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/85f30fbab0c849a8a41e391821ae5ece_tplv-ovu2ybn2i4-1.png"));
            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/52ba3acfcc804f30be790e7a8fcef571_tplv-ovu2ybn2i4-2.jpeg"));
            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/94991b616cb34e5ca3e46f385f066645_tplv-ovu2ybn2i4-1.png"));
            imgMask = new BufferedImage(imgPuzzle.getWidth(), imgPuzzle.getHeight(), BufferedImage.TYPE_INT_ARGB);

            final long start = System.currentTimeMillis();
            extract(imgPuzzle);
            final long t1 = System.currentTimeMillis();
            System.out.println("transform, t1 = " + (t1 - start));

            find(imgBackground, imgMask);
            final long t2 = System.currentTimeMillis();
            System.out.println("find, t2 = " + (t2 - t1));
        } catch (IOException e) {
        }
    }

    public void find(BufferedImage imgBackground, BufferedImage imgMask) {
        final int gWidth = imgBackground.getWidth();
        final int gHeight = imgBackground.getHeight();

        final int mWidth = imgMask.getWidth();
        final int mHeight = imgMask.getHeight();

        final int xEnd = gWidth - mWidth;
        final int yEnd = gHeight - mHeight;

        for (int x = 0; x < xEnd; x++) {
            for (int y = 0; y < yEnd; y++) {
                if (match(imgBackground, imgMask, x, y)) {
                    System.out.println("x = " + x + ", y = " + y);
                    mark(imgBackground, x, y, mWidth, mHeight);
                    return;
                }
            }
        }

        System.out.println("Not found");
    }

    private void mark(BufferedImage imgBackground, int x, int y, int mWidth, int mHeight) {
        Graphics2D g = imgBackground.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0x33333333, true));
        g.drawRect(x, y, mWidth, mHeight);
    }

    private boolean match(BufferedImage imgBackground, BufferedImage imgMask, int x0, int y0) {
        final int mWidth = imgMask.getWidth();
        final int mHeight = imgMask.getHeight();

        int total = 0;
        int match = 0;

        for (int x = 0; x < mWidth; x++) {
            for (int y = 0; y < mHeight; y++) {
                if (imgMask.getRGB(x, y) == 0xffffffff) {
                    total++;
                    if (whiteMatch(imgBackground.getRGB(x0 + x, y0 + y))) {
                        match++;
                    }
                }
            }
        }

        return match * 100 > total * DEGREE;
    }

    private boolean whiteMatch(int rgb) {
        int r = rgb & 0xFF0000;
        int g = rgb & 0xFF00;
        int b = rgb & 0xFF;
        return r >= WHITE_MASK_RED && g >= WHITE_MASK_GREEN && b >= WHITE_MASK_BLUE;
    }

    public void extract(BufferedImage imgMask) {
        Graphics2D g = imgMask.createGraphics();

        int width = imgPuzzle.getWidth();
        int height = imgPuzzle.getHeight();

        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                if (isWhite(x, y) && isEdge(x, y, width, height)) {
                    g.setComposite(AlphaComposite.Src);
                    g.setColor(new Color(0xffffffff, true));
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    public boolean isWhite(int x, int y) {
        int pixel = imgPuzzle.getRGB(x, y);
        return pixel == 0xffffffff;
    }

    public boolean isEdge(int x, int y, int width, int height) {
        int m = 3; // 边缘宽度

        if (x < m) {
            return true;
        }

        if (x + m > width) {
            return true;
        }

        if (y < m) {
            return true;
        }

        if (y + m > height) {
            return true;
        }

        for (int i = -m; i <= m; i++) {
            for (int j = -m; j <= m; j++) {
                if (isTransparent(x + i, y + j)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isTransparent(int x, int y) {
        int pixel = imgPuzzle.getRGB(x, y);
        return (pixel >> 24) < 0xf0;
    }

    public Dimension getPreferredSize() {
        if (imgBackground == null) {
            return new Dimension(100, 100);
        } else {
            return new Dimension(imgBackground.getWidth(null), imgBackground.getHeight(null));
        }
    }

    public static void main(String[] args) {

        JFrame f = new JFrame("Load Image Sample");

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.add(new LoadImageApp());
        f.pack();
        f.setVisible(true);
    }
}
