package snakenladder;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BackgroundMusic {
    private static BackgroundMusic instance;
    private Clip clip;
    private boolean isPlaying = false;

    private BackgroundMusic() {
        try {
            File soundFile = new File("res/bg_music.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BackgroundMusic getInstance() {
        if (instance == null) {
            instance = new BackgroundMusic();
        }
        return instance;
    }

    public void play() {
        if (clip != null && !isPlaying) {
            clip.start();
            isPlaying = true;
        }
    }

    public void stop() {
        if (clip != null && isPlaying) {
            clip.stop();
            isPlaying = false;
        }
    }

    public void toggle() {
        if (isPlaying) {
            stop();
        } else {
            play();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
    public static void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}