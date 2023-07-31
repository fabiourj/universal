package com.sherdle.universal.providers.audio.api;

import com.sherdle.universal.providers.audio.api.object.CommentObject;
import com.sherdle.universal.providers.audio.api.object.SoundCloudResult;
import com.sherdle.universal.providers.audio.api.soundcloud.ApiWrapper;
import com.sherdle.universal.providers.audio.api.soundcloud.Env;
import com.sherdle.universal.providers.audio.api.soundcloud.Request;
import com.sherdle.universal.providers.audio.api.soundcloud.Token;
import com.sherdle.universal.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

public class SoundCloudClient {

	private final ApiWrapper wrapper;

    //Constants
    public static final String BASEURL = "https://api.soundcloud.com/";
    public static final String USER="users";
    public static final String TRACKS="tracks";
    public static final String COMMENTS="comments";
    public static final String PLAYLISTS="playlists";

    public static final String FORMAT_PARTITIONING="?linked_partitioning=1";
	public static final String FORMAT_OFFSET="&offset=%1$s&limit=%2$s";
    public static final String FORMAT_FILTER_QUERY="&q=%1$s";

    public SoundCloudClient(String clientId, String secret) {

		wrapper = new ApiWrapper(clientId, secret,
				null, null, Env.LIVE);
		wrapper.debugRequests = true;
	}
	
	public SoundCloudResult getListTrackObjectsByQuery(String query, int offset, int limit){

		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(TRACKS);
		builder.append(FORMAT_PARTITIONING);
		builder.append(String.format(FORMAT_FILTER_QUERY, query));
		builder.append(String.format(FORMAT_OFFSET, offset, limit));

		String url = builder.toString();

		try {
			wrapper.clientCredentials(Token.SCOPE_DEFAULT);

			HttpResponse response = wrapper.get(Request.to(url));
			String jsonString = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = new JSONObject(jsonString);
			return SoundCloudParser.parsingListTrackObject(jsonObject, this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public SoundCloudResult getListTrackObjectsOfUser(long userId, int offset, int limit){
		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(USER+"/");
		builder.append(userId +"/");
		builder.append(TRACKS);
		builder.append(FORMAT_PARTITIONING);
		builder.append(String.format(FORMAT_OFFSET, offset, limit));

		String url = builder.toString();

		try {
			Token t = wrapper.clientCredentials(null);
			wrapper.setToken(t);

			HttpResponse response = wrapper.get(Request.to(url));
			String jsonString = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = new JSONObject(jsonString);
			return SoundCloudParser.parsingListTrackObject(jsonObject, this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
    }

    public SoundCloudResult getListTrackObjectsOfPlaylist(long playListID, int offset, int limit){
        StringBuilder builder = new StringBuilder();
        builder.append(BASEURL);
        builder.append(PLAYLISTS+"/");
        builder.append(playListID +"/");
        builder.append(TRACKS);
		builder.append(FORMAT_PARTITIONING);
        builder.append(String.format(FORMAT_OFFSET, offset, limit));

        String url = builder.toString();

		try {
			Token t = wrapper.clientCredentials(null);
			wrapper.setToken(t);

			HttpResponse response = wrapper.get(Request.to(url));
			String jsonString = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = new JSONObject(jsonString);
			return SoundCloudParser.parsingListTrackObject(jsonObject, this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
    }

    public String getStreamUrl(long trackId){
		String url = BASEURL + TRACKS + "/" + trackId + "/stream";

		try {
			Token t = wrapper.clientCredentials(null);
			wrapper.setToken(t);

			HttpResponse response = wrapper.get(Request.to(url));
			String jsonString = EntityUtils.toString(response.getEntity());

			JSONObject stream = new JSONObject(jsonString);
			return stream.getString("location");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public ArrayList<CommentObject> getListCommentObject(long trackId){
		StringBuilder builder = new StringBuilder();
		builder.append(BASEURL);
		builder.append(TRACKS+"/");
		builder.append(trackId +"/");
		builder.append(COMMENTS);

        String url = builder.toString();

		try {
			Token t = wrapper.clientCredentials(null);
			wrapper.setToken(t);

			HttpResponse response = wrapper.get(Request.to(url));
			String jsonString = EntityUtils.toString(response.getEntity());
			JSONArray jsonObject = new JSONArray(jsonString);
			return SoundCloudParser.parsingListCommentObject(jsonObject);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

        return SoundCloudParser.parsingListCommentObject(Helper.getJSONArrayFromUrl(url));

    }

}
