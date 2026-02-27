package edu.neumont.csc150.service;

import edu.neumont.csc150.model.misc.AudioPlayer;

import java.util.ArrayList;
import java.util.List;

public class SoundService implements Injectable {
    private List<AudioPlayer> players;

    @Override
    public void startService() {
        players = new ArrayList<>();
    }

    @Override
    public void stopService() {
        players.forEach(AudioPlayer::stop);
        players = null;
    }

    public AudioPlayer create(String file, boolean loop) {
        AudioPlayer audioPlayer = new AudioPlayer(file);
        players.add(audioPlayer);
        if (loop) audioPlayer.loop();
        return audioPlayer;
    }

    public void stop(AudioPlayer audioPlayer) {
        audioPlayer.stop();
        players.remove(audioPlayer);
    }
}
