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
        long startTime = System.currentTimeMillis();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                progressBar.setValue((int) (generator.getProgress() * 100));
                progressLabel.setText((int) (generator.getProgress() * 1000) / 10.0 + "%");
                if (generator.getProgress() == 1) {
                    doneLabel.setText("Done!\nSaved to " + FILE);
                } else if (generator.getProgress() > 0) {
                    doneLabel.setText("Estimated time remaining: " + prettyTime((long) ((System.currentTimeMillis() - startTime) / generator.getProgress() * (1 - generator.getProgress()))));
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

    private String p(long n, String s){
        if(n != 1)return n + " " + s + "s";
        return n + " " + s;
    }

    private String prettyTime(long millis){
        long seconds = millis/1000;
        if(seconds >= 60*60*24)
            return p(seconds/60/60/24,"day") + " " + p((seconds/60/60)%24,"hr");
        if(seconds >= 60*60)
            return p(seconds/60/60,"hr") + " " + p((seconds/60)%60,"min");
        if(seconds >= 60)
            return p(seconds/60,"min") + " " + p((seconds)%60,"sec");
        return p(seconds,"sec");
    }
}