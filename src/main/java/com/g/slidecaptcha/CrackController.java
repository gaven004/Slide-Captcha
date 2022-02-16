package com.g.slidecaptcha;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrackController {
    private static final Logger log = LoggerFactory.getLogger(CrackController.class);

    static final int MARGIN = 3; // 边缘宽度

    static final int DEGREE = 85;
    static final int ALPHA = 80;

    static final int WHITE_MASK = 0xff * ALPHA / 100;
    static final int WHITE_MASK_RED = WHITE_MASK << 16;
    static final int WHITE_MASK_GREEN = WHITE_MASK << 8;
    static final int WHITE_MASK_BLUE = WHITE_MASK;

    static final Point ERROR = new Point(-1, -1);

    @PostMapping("/crack")
    public Point crack(@RequestBody Request request) {
        log.info("request: {}", request);

        BufferedImage imgBackground;
        BufferedImage imgPuzzle;

        try {
            log.debug("Load image");
            imgBackground = ImageIO.read(new URL(request.getBackground()));
            imgPuzzle = ImageIO.read(new URL(request.getPuzzle()));

            List<Point> listMask = extract(imgPuzzle);

            Point point = find(imgBackground, imgPuzzle, listMask);
            log.info("response: {}", point);

            return point;
        } catch (IOException e) {
            log.warn("Error", e);
        }

        return ERROR;
    }

    static List<Point> extract(BufferedImage imgPuzzle) {
        log.debug("Extract outline");

        List<Point> listMask = new ArrayList<>();

        int width = imgPuzzle.getWidth();
        int height = imgPuzzle.getHeight();

        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                if (isWhite(imgPuzzle.getRGB(x, y)) &&
                        isEdge(imgPuzzle, x, y, width, height)) {
                    listMask.add(new Point(x, y));
                }
            }
        }

        return listMask;
    }

    static boolean isTransparent(int pixel) {
        return (pixel >> 24) < 0xf0;
    }

    static boolean isWhite(int pixel) {
        return pixel == 0xffffffff;
    }

    static boolean isEdge(BufferedImage imgPuzzle, int x, int y, int width, int height) {
        if (x < MARGIN) {
            return true;
        }

        if (x + MARGIN > width) {
            return true;
        }

        if (y < MARGIN) {
            return true;
        }

        if (y + MARGIN > height) {
            return true;
        }

        for (int i = -MARGIN; i <= MARGIN; i++) {
            for (int j = -MARGIN; j <= MARGIN; j++) {
                if (isTransparent(imgPuzzle.getRGB(x + i, y + j))) {
                    return true;
                }
            }
        }

        return false;
    }

    static Point find(BufferedImage imgBackground, BufferedImage imgPuzzle, List<Point> listMask) {
        log.debug("Find puzzle");

        final int gWidth = imgBackground.getWidth();
        final int gHeight = imgBackground.getHeight();

        final int mWidth = imgPuzzle.getWidth();
        final int mHeight = imgPuzzle.getHeight();

        final int xEnd = gWidth - mWidth;
        final int yEnd = gHeight - mHeight;

        for (int x = 0; x < xEnd; x++) {
            for (int y = 0; y < yEnd; y++) {
                if (match(imgBackground, x, y, listMask)) {
                    log.info("Found: x = {}, y = {}", x, y);
                    return new Point(x, y);
                }
            }
        }

        log.warn("Not found");
        return ERROR;
    }

    static boolean match(BufferedImage imgBackground, int x0, int y0, List<Point> listMask) {
        int total = listMask.size();
        int match = 0;

        for (Point p : listMask) {
            if (whiteMatch(imgBackground.getRGB(x0 + p.getX(), y0 + p.getY()))) {
                match++;
            }
        }

        return match * 100 > total * DEGREE;
    }

    static boolean whiteMatch(int rgb) {
        int r = rgb & 0xFF0000;
        int g = rgb & 0xFF00;
        int b = rgb & 0xFF;
        return r >= WHITE_MASK_RED && g >= WHITE_MASK_GREEN && b >= WHITE_MASK_BLUE;
    }
}
