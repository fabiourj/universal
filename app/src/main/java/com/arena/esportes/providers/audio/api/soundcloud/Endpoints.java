package com.arena.esportes.providers.audio.api.soundcloud;

/**
 * Various SoundCloud API endpoints.
 * See <a href="https://github.com/soundcloud/api/wiki/03-Representations">the API docs</a> for the most
 * recent listing.
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface Endpoints {
    String TOKEN = "/oauth2/token";

    String TRACKS              = "/tracks";
    String TRACK_DETAILS       = "/tracks/%d";
    String TRACK_COMMENTS      = "/tracks/%d/comments";
    String TRACK_FAVORITERS    = "/tracks/%d/favoriters";

    String USERS               = "/users";
    String USER_DETAILS        = "/users/%d";
    String USER_FOLLOWINGS     = "/users/%d/followings";
    String USER_FOLLOWERS      = "/users/%d/followers";
    String USER_TRACKS         = "/users/%d/tracks";
    String USER_FAVORITES      = "/users/%d/favorites";
    String USER_PLAYLISTS      = "/users/%d/playlists";

    String MY_DETAILS          = "/me";
    String MY_CONNECTIONS      = "/me/connections";
    String MY_ACTIVITIES       = "/me/activities/tracks";
    String MY_EXCLUSIVE_TRACKS = "/me/activities/tracks/exclusive";
    String MY_NEWS             = "/me/activities/all/own";
    String MY_TRACKS           = "/me/tracks";
    String MY_PLAYLISTS        = "/me/playlists";
    String MY_FAVORITES        = "/me/favorites";
    String MY_FAVORITE         = "/me/favorites/%d";
    String MY_FOLLOWERS        = "/me/followers";
    String MY_FOLLOWER         = "/me/followers/%d";
    String MY_FOLLOWINGS       = "/me/followings";
    String MY_FOLLOWING        = "/me/followings/%d";
    String MY_CONFIRMATION     = "/me/email-confirmations";
    String MY_FRIENDS          = "/me/connections/friends";

    String SUGGESTED_USERS     = "/users/suggested";

    String RESOLVE             = "/resolve";

    String SEND_PASSWORD       = "/passwords/reset-instructions";
    String CONNECT             = "/connect";
    String FACEBOOK_CONNECT    = "/connect/via/facebook";
}
