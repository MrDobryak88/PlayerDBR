package com.example.musicplayer.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private List<Track> tracks;
    private int currentTrackIndex;

    public Playlist() {
        this.tracks = new ArrayList<>();
        this.currentTrackIndex = -1;
    }

    public void addTrack(Track track) {
        tracks.add(track);
        if (currentTrackIndex == -1) {
            currentTrackIndex = 0;
        }
    }

    public void removeTrack(Track track) {
        int index = tracks.indexOf(track);
        if (index != -1) {
            tracks.remove(index);
            if (currentTrackIndex >= index) {
                currentTrackIndex--;
                if (currentTrackIndex < 0 && !tracks.isEmpty()) {
                    currentTrackIndex = 0;
                }
            }
        }
    }

    public Track getCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < tracks.size()) {
            return tracks.get(currentTrackIndex);
        }
        return null;
    }

    public Track nextTrack() {
        if (tracks.isEmpty()) return null;

        currentTrackIndex++;
        if (currentTrackIndex >= tracks.size()) {
            currentTrackIndex = 0;
        }
        return tracks.get(currentTrackIndex);
    }

    public Track prevTrack() {
        if (tracks.isEmpty()) return null;

        currentTrackIndex--;
        if (currentTrackIndex < 0) {
            currentTrackIndex = tracks.size() - 1;
        }
        return tracks.get(currentTrackIndex);
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < tracks.size()) {
            currentTrackIndex = index;
        }
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
}