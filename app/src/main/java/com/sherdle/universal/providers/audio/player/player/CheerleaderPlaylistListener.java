package com.sherdle.universal.providers.audio.player.player;

import com.sherdle.universal.providers.audio.api.object.TrackObject;

/**
 * Listener used to catch events performed on the play playlist.
 */
public interface CheerleaderPlaylistListener {

    /**
     * Called when a tracks has been added to the player playlist.
     *
     * @param track track added.
     */
    void onTrackAdded(TrackObject track);


    /**
     * Called when a tracks has been removed from the player playlist.
     *
     * @param track   track removed.
     * @param isEmpty true if the playlist is empty after deletion.
     */
    void onTrackRemoved(TrackObject track, boolean isEmpty);
}
