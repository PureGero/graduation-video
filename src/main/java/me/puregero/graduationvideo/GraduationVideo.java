package me.puregero.graduationvideo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class GraduationVideo extends JFrame {

    private static final int DURATION = 5 * 60 * 1000;
    private static final int FPS = 25;
    private static final String FILE = "video.mp4";

    public static void main(String[] args) {
        new GraduationVideo();
    }

    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JLabel doneLabel;

    public GraduationVideo() {
        super("GraduationVideo");

        addComponents();

        pack();
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        GraduationVideoGenerator generator = new GraduationVideoGenerator(new File("."), FILE, 1920, 1080, DURATION, FPS);
        generator.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                progressBar.setValue((int) (generator.getProgress() * 100));
                progressLabel.setText((int) (generator.getProgress() * 1000) / 10.0 + "%");
                if (generator.getProgress() == 1) {
                    doneLabel.setText("Done!\nSaved to " + FILE);
                }
                repaint();
            }
        }, 50, 50);
    }

    private void addComponents() {
        JPanel pane = new JPanel();
        progressBar = new JProgressBar();
        pane.add(progressBar, BorderLayout.CENTER);
        progressLabel = new JLabel("0.0%", SwingConstants.CENTER);
        pane.add(progressLabel, BorderLayout.CENTER);
        doneLabel = new JLabel("", SwingConstants.CENTER);
        pane.add(doneLabel, BorderLayout.SOUTH);
        pane.setPreferredSize(new Dimension(300, 100));
        getContentPane().add(pane);
    }
}