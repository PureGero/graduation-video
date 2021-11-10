package me.puregero.graduationvideo;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GraduationVideoGenerator extends Thread {

    private final File photosDir;
    private final String file;
    private final int width;
    private final int height;
    private final int duration;
    private final int fps;
    private final BufferedImagePool imagePool;

    private double progress = 0;
    private List<BufferedImage> photos = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private List<Integer> locations = new ArrayList<>();

    public GraduationVideoGenerator(File photosDir, String file, int width, int height, int duration, int fps) {
        this.photosDir = photosDir;
        this.file = file;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.fps = fps;
        imagePool = new BufferedImagePool(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }

    public double getProgress() {
        return progress;
    }

    @Override
    public void run() {
        loadPhotos();

        System.out.println("Generating video...");
        long time = System.currentTimeMillis();

        try (SeekableByteChannel out = NIOUtils.writableFileChannel(file)) {
            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));

            generateFrames(encoder);

            encoder.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Generated video (" + (System.currentTimeMillis() - time) + "ms)");
    }

    private void generateFrames(AWTSequenceEncoder encoder) throws IOException {
        int framesPerStage = duration * fps / 1000 / photos.size() * 2;
        double d = 1.0 / photos.size();
        for (int index = 0; index < photos.size(); index += 2) {
            System.out.println("Rendering photo " + index + " / " + photos.size());

            int finalIndex = index;
            new GraduationVideoStage(photos, names, locations, index, imagePool, framesPerStage, (value) -> progress = d * finalIndex + value * 2 * d).renderFrames(encoder);
        }

        encoder.encodeImage(new GraduationVideoFrameRender(photos, names, locations, photos.size(), imagePool, 0).get());
        progress = 1;
    }

    private void loadPhotos() {
        File[] files = photosDir.listFiles();

        System.out.println("Loading photos...");
        long time = System.currentTimeMillis();

        Queue<CompletableFuture<BufferedImage>> images = new LinkedList<>();

        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().contains(".")) {
                    names.add(file.getName().substring(0, file.getName().lastIndexOf('.')));
                } else {
                    names.add(file.getName());
                }
                images.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return ImageIO.read(file);
                    } catch (IOException e) {
                        return null;
                    }
                }));
            }
        }

        while (!images.isEmpty()) {
            BufferedImage image = images.remove().join();
            if (image != null) {
                photos.add(image);
            } else {
                names.remove(photos.size());
            }
        }

        for (int i = 0; i < photos.size(); i++) {
            locations.add(i);
        }
        Collections.shuffle(locations);

        System.out.println("Loaded photos (" + (System.currentTimeMillis() - time) + "ms)");
    }
}
