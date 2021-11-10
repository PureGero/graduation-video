package me.puregero.graduationvideo;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

public class BufferedImagePool {

    private final int width;
    private final int height;
    private final int args;

    private Queue<BufferedImage> pool = new LinkedList<>();

    public BufferedImagePool(int width, int height, int args) {
        this.width = width;
        this.height = height;
        this.args = args;
    }

    public synchronized BufferedImage alloc() {
        if (pool.isEmpty()) {
            return new BufferedImage(width, height, args);
        }
        return pool.remove();
    }

    public synchronized void free(BufferedImage image) {
        pool.add(image);
    }

}
