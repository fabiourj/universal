package com.sherdle.universal.providers.audio.api.soundcloud;

/**
 * Request parameters for various objects.
 */
public interface Params {
    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.2-Resources%3A-tracks">Tracks</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface Track {
        String TITLE         = "track[title]";          // required
        String TYPE          = "track[track_type]";
        String DESCRIPTION   = "track[description]";
        String ASSET_DATA    = "track[asset_data]";
        String ARTWORK_DATA  = "track[artwork_data]";
        String POST_TO       = "track[post_to][][id]";
        String POST_TO_EMPTY = "track[post_to][]";
        String TAG_LIST      = "track[tag_list]";
        String PERMALINK     = "track[permalink]";
        String SHARING       = "track[sharing]";
        String STREAMABLE    = "track[streamable]";
        String DOWNLOADABLE  = "track[downloadable]";
        String GENRE         = "track[genre]";
        String RELEASE       = "track[release]";
        String RELEASE_DAY   = "track[release_day]";
        String RELEASE_MONTH = "track[release_month]";
        String RELEASE_YEAR  = "track[release_year]";
        String PURCHASE_URL  = "track[purchase_url]";
        String LABEL_NAME    = "track[label_name]";
        String LABEL_ID      = "track[label_id]";
        String VIDEO_URL     = "track[video_url]";
        String ISRC          = "track[isrc]";
        String KEY_SIGNATURE = "track[key_signature]";
        String BPM           = "track[bpm]";
        String LICENSE       = "track[license]";
        String SHARED_EMAILS = "track[shared_to][emails][][address]";
        String SHARING_NOTE  = "track[sharing_note]";
        String PUBLIC        = "public";
        String PRIVATE       = "private";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.1-Resources%3A-users">Users</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface User {
        String NAME                  = "user[username]";
        String FULLNAME              = "user[full_name]";
        String DESCRIPTION           = "user[description]";
        String CITY                  = "user[city]";
        String PERMALINK             = "user[permalink]";
        String DISCOGS_NAME          = "user[discogs_name]";
        String MYSPACE_NAME          = "user[myspace_name]";
        String WEBSITE               = "user[website]";
        String WEBSITE_TITLE         = "user[website_title]";
        String EMAIL                 = "user[email]";
        String PASSWORD              = "user[password]";
        String PASSWORD_CONFIRMATION = "user[password_confirmation]";
        String TERMS_OF_USE          = "user[terms_of_use]";
        String AVATAR                = "user[avatar_data]";
    }

    /**
     * <a href="https://github.com/soundcloud/api/wiki/10.5-Resources%3A-comments">Comments</a>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    interface Comment {
        String BODY      = "comment[body]";
        String TIMESTAMP = "comment[timestamp]";
        String REPLY_TO  = "comment[reply_to]";
    }
}
