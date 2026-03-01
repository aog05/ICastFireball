package edu.neumont.csc150.model.misc;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class AudioPlayer {
    private AudioInputStream audioStream;
    private Clip clip;

    public AudioPlayer(String filePath) {
        try {
            audioStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (Exception e) {
            System.out.println("Error while using AudioStream");
            throw new RuntimeException(e);
        }
    }

    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    public void pause() {
        clip.stop();
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        pause();
        clip.close();
    }

    public void setVolume(float volume) {
        volume = Math.min(Math.max(0, volume), 1);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20 * (float) Math.log10(volume));
    }
}
