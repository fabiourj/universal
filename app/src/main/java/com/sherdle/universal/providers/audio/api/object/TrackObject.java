package com.sherdle.universal.providers.audio.api.object;

import androidx.annotation.WorkerThread;

import com.sherdle.universal.providers.audio.api.SoundCloudClient;

import java.io.Serializable;
import java.util.Date;

public class TrackObject implements Serializable {
	private final long id;
	private final Date createdDate;
	private final long userId;
	private long duration;
	private final String sharing;
	private final String tagList;
	private final String genre;
	private final String title;
	private final String description;
	private final String username;
	private final String avatarUrl;
	private final String permalinkUrl;
	private final String artworkUrl;
	private final String waveForm;
	private final long playbackCount;
	private final long favoriteCount;
	private final long commentCount;
	private String streamUrl;

    private boolean streamAble;

	public TrackObject(long id, Date createdDate, long userId, long duration, String sharing, String tagList, String genre, String title, String description, String username, String avatarUrl, String permalinkUrl, String artworkUrl, String waveForm, long playbackCount, long favoriteCount, long commentCount, String streamUrl) {
		super();
		this.id = id;
		this.createdDate = createdDate;
		this.userId = userId;
		this.duration = duration;
		this.sharing = sharing;
		this.tagList = tagList;
		this.genre = genre;
		this.title = title;
		this.description = description;
		this.username = username;
		this.avatarUrl = avatarUrl;
		this.permalinkUrl = permalinkUrl;
		this.artworkUrl = artworkUrl;
		this.waveForm = waveForm;
		this.playbackCount = playbackCount;
		this.favoriteCount = favoriteCount;
		this.commentCount = commentCount;
		this.streamUrl = streamUrl;
	}


	public long getId() {
		return id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public long getUserId() {
		return userId;
	}

	public long getDuration() {
		return duration;
	}

	public String getSharing() {
		return sharing;
	}

	public String getTagList() {
		return tagList;
	}

	public String getGenre() {
		return genre;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getPermalinkUrl() {
		return permalinkUrl;
	}

	public String getArtworkUrl() {
		return artworkUrl;
	}

	public String getWaveForm() {
		return waveForm;
	}

	public long getPlaybackCount() {
		return playbackCount;
	}

	public long getFavoriteCount() {
		return favoriteCount;
	}

	public long getCommentCount() {
		return commentCount;
	}

	@WorkerThread
	public String getLinkStream(String clientId, String clientSecret) {
		if (streamUrl != null) return streamUrl;

		streamUrl = new SoundCloudClient(clientId, clientSecret).getStreamUrl(this.id);
		return streamUrl;
	}

    public boolean isStreamAble() {
        return streamAble;
    }

    public void setStreamAble(boolean streamAble) {
        this.streamAble = streamAble;
    }

	public void setDuration(long duration){
		this.duration = duration;
	}

}
