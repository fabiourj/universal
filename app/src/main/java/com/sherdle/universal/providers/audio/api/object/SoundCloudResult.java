package com.sherdle.universal.providers.audio.api.object;

import java.util.ArrayList;

public class SoundCloudResult {
    private ArrayList<TrackObject> tracks;
    private String nextPageUrl;

    public SoundCloudResult(ArrayList<TrackObject> tracks, String nextPageUrl) {
        this.tracks = tracks;
        this.nextPageUrl = nextPageUrl;
    }

    public ArrayList<TrackObject> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<TrackObject> tracks) {
        this.tracks = tracks;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

}
