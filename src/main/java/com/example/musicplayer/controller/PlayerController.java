// PlayerController.java
package com.example.musicplayer.controller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import com.example.musicplayer.model.Playlist;
import com.example.musicplayer.model.Track;

public class PlayerController {
    private Playlist playlist;
    private MediaPlayer mediaPlayer;

    public PlayerController(Playlist playlist) {
        this.playlist = playlist;
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            Track currentTrack = playlist.getCurrentTrack();
            if (currentTrack != null) {
                Media media = new Media(currentTrack.getFilePath());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void next() {
        Track nextTrack = playlist.nextTrack();
        if (nextTrack != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(nextTrack.getFilePath());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    public void prev() {
        Track prevTrack = playlist.prevTrack();
        if (prevTrack != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(prevTrack.getFilePath());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        } else {
            play();
        }
    }

    public void setPlaybackRate(double rate) {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(rate);
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}