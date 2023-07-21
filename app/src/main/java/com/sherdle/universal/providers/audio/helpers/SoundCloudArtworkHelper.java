package com.sherdle.universal.providers.audio.helpers;

import com.sherdle.universal.providers.audio.api.object.TrackObject;

/**
 * Used to encapsulate artwork url format.
 */
public final class SoundCloudArtworkHelper {

    /**
     * Artwork format : 16x16.
     */
    public static final String MINI = "mini";

    /**
     * Artwork format : 20x20.
     */
    public static final String TINY = "tiny";

    /**
     * Artwork format : 32x32.
     */
    public static final String SMALL = "small";

    /**
     * Artwork format : 47x47.
     */
    public static final String BADGE = "badge";

    /**
     * Artwork format : 100x100.
     */
    public static final String LARGE = "large";

    /**
     * Artwork format : 300x300.
     */
    public static final String XLARGE = "t300x300";

    /**
     * Artwork format : 400x400.
     */
    public static final String XXLARGE = "crop";

    /**
     * Artwork format : 500x500.
     */
    public static final String XXXLARGE = "t500x500";

    /**
     * Non instantiable class.
     */
    private SoundCloudArtworkHelper() {

    }

    /**
     * Retrieve the artwork url of a track pointing to the requested size.
     *
     * @param track track from which artwork url should be returned.
     * @param size  wished size.
     * @return artwork url or null if no artwork are available.
     */
    public static String getArtworkUrl(TrackObject track, String size) {
        String defaultUrl = track.getArtworkUrl();
        String alternativeUrl = track.getAvatarUrl();
        if (defaultUrl == null || defaultUrl.equals("null")) {
            if (alternativeUrl == null || alternativeUrl.equals("null")){
                return null;
            } else {
                defaultUrl = alternativeUrl;
            }
        }
        switch (size) {
            case MINI:
            case TINY:
            case SMALL:
            case BADGE:
            case LARGE:
            case XLARGE:
            case XXLARGE:
            case XXXLARGE:
                return defaultUrl.replace(LARGE, size);
            default:
                return defaultUrl;
        }
    }

}
