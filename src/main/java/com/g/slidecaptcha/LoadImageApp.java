package com.g.slidecaptcha;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * This class demonstrates how to load an Image from an external file
 */
public class LoadImageApp extends Component {
    static final int DEGREE = 85;
    static final int ALPHA = 80;

    static final int WHITE_MASK = 0xff * ALPHA / 100;
    static final int WHITE_MASK_RED = WHITE_MASK << 16;
    static final int WHITE_MASK_GREEN = WHITE_MASK << 8;
    static final int WHITE_MASK_BLUE = WHITE_MASK;

    BufferedImage imgBackground;
    BufferedImage imgPuzzle;
//    BufferedImage imgMask;

    public void paint(Graphics g) {
        g.drawImage(imgBackground, 0, 0, null);
    }

    public LoadImageApp() {
        try {
            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/7d99f0397372459eb8c1dd4e8ddf9c54_tplv-ovu2ybn2i4-2.jpeg"));
            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/dcaa8fa6931345979f8b5726a3e79490_tplv-ovu2ybn2i4-1.png"));
//            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/88c30f87f6e54f5991bd9131f694d05f_tplv-ovu2ybn2i4-2.jpeg"));
//            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/7edbaa9b2489441cbba3e58429e80f6c_tplv-ovu2ybn2i4-1.png"));
//            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/98a5fd3e80d145b9bf211cdf74d31818_tplv-ovu2ybn2i4-2.jpeg"));
//            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/85f30fbab0c849a8a41e391821ae5ece_tplv-ovu2ybn2i4-1.png"));
//            imgBackground = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/52ba3acfcc804f30be790e7a8fcef571_tplv-ovu2ybn2i4-2.jpeg"));
//            imgPuzzle = ImageIO.read(new File("/Users/gaven/Downloads/tk_img/94991b616cb34e5ca3e46f385f066645_tplv-ovu2ybn2i4-1.png"));
//            imgMask = new BufferedImage(imgPuzzle.getWidth(), imgPuzzle.getHeight(), BufferedImage.TYPE_INT_ARGB);

            final long start = System.currentTimeMillis();
            List<Point> listMask = extract(imgPuzzle);
            final long t1 = System.currentTimeMillis();
            System.out.println("transform, t1 = " + (t1 - start));

            find(imgBackground, imgPuzzle, listMask);
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

    public void find(BufferedImage imgBackground, BufferedImage imgPuzzle, List<Point> listMask) {
        final int gWidth = imgBackground.getWidth();
        final int gHeight = imgBackground.getHeight();

        final int mWidth = imgPuzzle.getWidth();
        final int mHeight = imgPuzzle.getHeight();

        final int xEnd = gWidth - mWidth;
        final int yEnd = gHeight - mHeight;

        for (int x = 0; x < xEnd; x++) {
            for (int y = 0; y < yEnd; y++) {
                if (match(imgBackground, x, y, listMask)) {
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

    private boolean match(BufferedImage imgBackground, int x0, int y0, List<Point> listMask) {
        int total = listMask.size();
        int match = 0;

        for (Point p : listMask) {
            if (whiteMatch(imgBackground.getRGB(x0 + p.getX(), y0 + p.getY()))) {
                match++;
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

    public List<Point> extract(BufferedImage imgPuzzle) {
//        Graphics2D g = imgMask.createGraphics();

        List<Point> listMask = new ArrayList<>();

        int width = imgPuzzle.getWidth();
        int height = imgPuzzle.getHeight();

        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                if (isWhite(x, y) && isEdge(x, y, width, height)) {
//                    g.setComposite(AlphaComposite.Src);
//                    g.setColor(new Color(0xffffffff, true));
//                    g.fillRect(x, y, 1, 1);
                    listMask.add(new Point(x, y));
                }
            }
        }

        return listMask;
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
