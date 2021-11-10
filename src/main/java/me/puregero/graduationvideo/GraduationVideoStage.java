package me.puregero.graduationvideo;

import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GraduationVideoStage {

    private final List<BufferedImage> photos;
    private final List<String> names;
    private final List<Integer> locations;
    private final int index;
    private final BufferedImagePool imagePool;
    private final int frameCount;
    private final Consumer<Double> setProgress;

    public GraduationVideoStage(List<BufferedImage> photos, List<String> names, List<Integer> locations, int index, BufferedImagePool imagePool, int frameCount, Consumer<Double> setProgress) {
        this.photos = photos;
        this.names = names;
        this.locations = locations;
        this.index = index;
        this.imagePool = imagePool;
        this.frameCount = frameCount;
        this.setProgress = setProgress;
    }

    public void renderFrames(AWTSequenceEncoder encoder) throws IOException {
        Queue<CompletableFuture<BufferedImage>> frames = new LinkedList<>();

        int count = 0;
        for (double step = 0; step < 2; step += 2.0 / frameCount) {
            frames.add(CompletableFuture.supplyAsync(new GraduationVideoFrameRender(photos, names, locations, index, imagePool, step)));
            count++;
        }

        int done = 0;
        while (!frames.isEmpty()) {
            BufferedImage frame = frames.remove().join();
            encoder.encodeImage(frame);
            imagePool.free(frame);
            setProgress.accept((double) ++done / count);
        }
    }
}
