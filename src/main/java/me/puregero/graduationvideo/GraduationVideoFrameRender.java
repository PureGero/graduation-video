package me.puregero.graduationvideo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class GraduationVideoFrameRender implements Supplier<BufferedImage> {

    private static final double PHOTO_RATIO = 2 / 3.0;

    private final List<BufferedImage> photos;
    private final List<String> names;
    private final List<Integer> locations;
    private final int index;
    private final BufferedImagePool imagePool;
    private final double step;

    public GraduationVideoFrameRender(List<BufferedImage> photos, List<String> names, List<Integer> locations, int index, BufferedImagePool imagePool, double step) {
        this.photos = photos;
        this.names = names;
        this.locations = locations;
        this.index = index;
        this.imagePool = imagePool;
        this.step = step;
    }

    private int rows;
    private int cols;
    private int imageHeight;
    private int imageWidth;
    private int width;
    private int height;

    @Override
    public BufferedImage get() {
        BufferedImage frame = imagePool.alloc();
        width = frame.getWidth();
        height = frame.getHeight();

        calculateRowsAndCols();

        Graphics2D g = frame.createGraphics();

        GradientPaint gp = new GradientPaint(0, 0, new Color(215, 253, 209),
                                             0, height, new Color(196, 251, 166));
        g.setPaint(gp);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < index; i++) {
            Point p = getLocation(locations.get(i));
            BufferedImage image = photos.get(i);
            if (image.getHeight() * PHOTO_RATIO > image.getWidth()) {
                g.drawImage(image, p.x, p.y, p.x + imageWidth, p.y + imageHeight, 0, 0, image.getWidth(), (int) (image.getWidth() * (1/PHOTO_RATIO) / 2), null);
            } else {
                g.drawImage(image, p.x, p.y, p.x + imageWidth, p.y + imageHeight, image.getWidth() / 2 - (int) (image.getHeight() * PHOTO_RATIO / 2), 0, image.getWidth() / 2 + (int) (image.getHeight() * PHOTO_RATIO / 2), image.getHeight(), null);
            }
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 72 * height / 1080));

        if (index + 1 < photos.size()) {
            synchronized (locations) {
                if (getLocation(locations.get(index)).x > getLocation(locations.get(index + 1)).x) {
                    Collections.swap(locations, index, index + 1);
                }
            }
        }

        if (index < photos.size()) {
            BufferedImage image = photos.get(index);
            String name = names.get(index);
            int position = locations.get(index);
            int h = height * 3 / 5;
            int w = h * image.getWidth() / image.getHeight();
            int dw = step > 1 ? 0 : (int) ((1 - step) * width / 2);
            int cx = width / 4 - dw;

            if (step < 1) {
                g.drawImage(image, cx - w / 2, height / 8, w, h, null);
                g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, height * 7 / 8);
            } else {
                Point p = getLocation(position);
                double s = step - 1;
                int fromX = cx - w / 2;
                int fromY = height / 8;
                int toX = p.x;
                int toY = p.y;
                int x = (int) (fromX + (toX - fromX) * s);
                int y = (int) (fromY + (toY - fromY) * s);
                int nw = (int) (w + (imageWidth - w) * s);
                int nh = (int) (h + (imageHeight - h) * s);
                if (image.getHeight() * PHOTO_RATIO > image.getWidth()) {
                    g.drawImage(image, x, y, x + nw, y + nh, 0, 0, image.getWidth(), image.getWidth() * nh / nw, null);
                } else {
                    g.drawImage(image, x, y, x + nw, y + nh, image.getWidth() / 2 - (image.getHeight() * nw / nh / 2), 0, image.getWidth() / 2 + (image.getHeight() * nw / nh / 2), image.getHeight(), null);
                }
                g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, height * 7 / 8 + (int) (s * height * 2 / 8));
            }
        }

        if (index + 1 < photos.size()) {
            BufferedImage image = photos.get(index + 1);
            String name = names.get(index + 1);
            int position = locations.get(index + 1);
            int h = height * 3 / 5;
            int w = h * image.getWidth() / image.getHeight();
            int dw = step > 1 ? 0 : (int) ((1 - step) * width / 2);
            int cx = width * 3 / 4 + dw;

            if (step < 1) {
                g.drawImage(image, cx - w / 2, height / 8, w, h, null);
                g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, height * 7 / 8);
            } else {
                Point p = getLocation(position);
                double s = step - 1;
                int fromX = cx - w / 2;
                int fromY = height / 8;
                int toX = p.x;
                int toY = p.y;
                int x = (int) (fromX + (toX - fromX) * s);
                int y = (int) (fromY + (toY - fromY) * s);
                int nw = (int) (w + (imageWidth - w) * s);
                int nh = (int) (h + (imageHeight - h) * s);
                if (image.getHeight() * PHOTO_RATIO > image.getWidth()) {
                    g.drawImage(image, x, y, x + nw, y + nh, 0, 0, image.getWidth(), image.getWidth() * nh / nw, null);
                } else {
                    g.drawImage(image, x, y, x + nw, y + nh, image.getWidth() / 2 - (image.getHeight() * nw / nh / 2), 0, image.getWidth() / 2 + (image.getHeight() * nw / nh / 2), image.getHeight(), null);
                }
                g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, height * 7 / 8 + (int) (s * height * 2 / 8));
            }
        }

        return frame;
    }

    private void calculateRowsAndCols() {
        int area = width * height;
        int scale = (int) (Math.sqrt(PHOTO_RATIO * area / photos.size()) * (1/PHOTO_RATIO));
        rows = height / scale + 1;
        imageHeight = height / rows;
        cols = (int) (width / (height / rows * PHOTO_RATIO));
        imageWidth = width / cols;
    }

    private Point getLocation(int location) {
        int rowsInUse = photos.size() / cols + 1;
        Point p = new Point((location % cols) * imageWidth + (width - cols * imageWidth) / 2, (location / cols) * imageHeight + (rows - rowsInUse) * imageHeight / 2);
        if ((location / cols) == (photos.size() / cols)) {
            int photosInLastRow = cols - (rowsInUse * cols - photos.size());
            int lx = (int) (imageWidth * cols / 2 + imageWidth * photosInLastRow / 2.0);
            p.x = lx - (photos.size() - location) * imageWidth;
        }
        return p;
    }

}
