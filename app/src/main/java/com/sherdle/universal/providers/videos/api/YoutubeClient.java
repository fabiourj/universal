package com.sherdle.universal.providers.videos.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.Toast;

import com.sherdle.universal.Config;
import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.comments.CommentsActivity;
import com.sherdle.universal.providers.videos.api.object.Video;
import com.sherdle.universal.providers.videos.player.YouTubePlayerActivity;
import com.sherdle.universal.providers.videos.ui.VideoDetailActivity;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This is class gets the videos from youtube and parses the result
 */
public class YoutubeClient implements VideoProvider {

    private static int PER_PAGE = 20;

    private static String API_BASE = "https://www.googleapis.com/youtube/v3";
    private static String API_TYPE_SEARCH = "/search";
    private static String API_TYPE_PLAYLIST = "/playlistItems";

    private static String TYPE_PLAYLIST = "playlist";
    private static String TYPE_USER = "channel";
    private static String TYPE_LIVE = "live";

    private VideosCallback callback;
    private String[] params;
    private WeakReference<Activity> activityReference;

    private String currentType;
    private String nextPageToken;
    private String serverKey;

    public YoutubeClient(String[] params, Activity activity, VideosCallback callback) {
        this.activityReference = new WeakReference<>(activity);
        this.params = params;
        this.callback = callback;

        currentType = getTypeBasedOnParameters();
    }

    @Override
    public void requestVideos(String pageToken, String searchQuery) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                serverKey = getAPIBasedOnParameters();

                ArrayList<Video> result = null;
                if (searchQuery != null) {
                    result = getSearchVideos(searchQuery, getIdBasedOnParameters(), pageToken);
                } else if (currentType.equals(TYPE_PLAYLIST)) {
                    result = getPlaylistVideos(getIdBasedOnParameters(), pageToken);
                } else if (currentType.equals(TYPE_LIVE)) {
                    result = getLiveVideos(getIdBasedOnParameters(), pageToken);
                } else if (currentType.equals(TYPE_USER)) {
                    result = getUserVideos(getIdBasedOnParameters(), pageToken);
                }

                final ArrayList<Video> videos = result;

                if (activityReference.get() == null) return;
                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {

                        if (videos != null) {
                            boolean canLoadMore = nextPageToken != null && videos.size() > 0;
                            callback.completed(videos, canLoadMore, nextPageToken);
                            nextPageToken = null;

                        } else {
                            callback.failed();
                        }
                    }
                });

            }
        });

    }

    private ArrayList<Video> getLiveVideos(String channelId, String pageToken) {
        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&type=video&channelId=" + channelId + "&eventType=live&maxResults=" + PER_PAGE + "&key=" + serverKey;

        if (pageToken != null)
            retrievalUrl += ("&pageToken=" + pageToken);

        return getVideos(retrievalUrl);
    }

    private ArrayList<Video>  getUserVideos(String username, String pageToken) {
        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&order=date&channelId=" + username + "&maxResults=" + PER_PAGE + "&key=" + serverKey;
        if (pageToken != null)
            retrievalUrl += ("&pageToken=" + pageToken);

        return getVideos(retrievalUrl);
    }

    private ArrayList<Video>  getPlaylistVideos(String username, String pageToken) {
        String retrievalUrl = API_BASE + API_TYPE_PLAYLIST + "?part=snippet&playlistId=" + username + "&maxResults=" + PER_PAGE + "&key=" + serverKey;
        if (pageToken != null)
            retrievalUrl += ("&pageToken=" + pageToken);

        return getVideos(retrievalUrl);
    }

    private ArrayList<Video>  getSearchVideos(String query, String channel, String pageToken) {//start video retrieval process
        //Decode the parameter
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //We know for a fact that this encoding is supported
        }

        String retrievalUrl = API_BASE + API_TYPE_SEARCH + "?part=snippet&type=video&channelId=" + channel + "&q=" + query + "&maxResults=" + PER_PAGE + "&key=" + serverKey;

        if (pageToken != null)
            retrievalUrl += ("&pageToken=" + pageToken);

        return getVideos(retrievalUrl);
    }

    private ArrayList<Video>  getVideos(String apiUrl) {
        ArrayList<Video> videos = null;
        String pagetoken = null;
        // Making HTTP request

        JSONObject json = Helper.getJSONObjectFromUrl(apiUrl);

        if (json == null) {
            return null;
        }

        try {
            if (json.getString("kind").contains("youtube")) {
                videos = new ArrayList<>();
            }

            if (json.has("nextPageToken"))
                nextPageToken = json.getString("nextPageToken");

            JSONArray jsonArray = json.getJSONArray("items");

            // Create a list to store the videos in
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonSnippet = jsonArray.getJSONObject(i).getJSONObject("snippet");
                    String title = Html.fromHtml(jsonSnippet.getString("title")).toString();
                    Date updated = formatData(jsonSnippet.getString("publishedAt"));
                    String description = jsonSnippet.getString("description");
                    String channel = jsonSnippet.getString("channelTitle");
                    String id;
                    try {
                        id = jsonSnippet.getJSONObject("resourceId").getString("videoId");
                    } catch (Exception e) {
                        id = jsonObject.getJSONObject("id").getString("videoId");
                    }
                    // For a sharper thumbnail change sq to hq, this will make the app slower though
                    String thumbUrl = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                    String image = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                    String shareUrl = "http://youtube.com/watch?v=" + id;
                    // save the video to the list
                    videos.add(new Video(title, id, updated, description, thumbUrl, image, channel, shareUrl));
                } catch (JSONException e) {
                    Log.v("INFO", "JSONException: " + e);
                }
            }

        } catch (JSONException e) {
            Log.v("INFO", "JSONException: " + e);
        }
        return videos;
    }

    @SuppressLint("SimpleDateFormat")
    private static Date formatData(String data) {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = parser.parse(data);
        } catch (ParseException e) {
            Log.printStackTrace(e);
        }

        return date;
    }

    @Override
    public void requestVideos(String pageToken) {
        requestVideos(pageToken, null);
    }

    @Override
    public boolean supportsSearch() {
        return !getTypeBasedOnParameters().equals(TYPE_PLAYLIST);
    }

    @Override
    public boolean isYoutubeLive() {
        return currentType.equals(TYPE_LIVE);
    }

    private String getTypeBasedOnParameters() {
        if ((
                !params[1].equals(TYPE_LIVE) &&
                        !params[1].equals(TYPE_USER) &&
                        !params[1].equals(TYPE_PLAYLIST))) {
            throw new RuntimeException("Your youtube configuration is incorrect, please check your documentation");
        }

        return params[1];
    }

    public String getIdBasedOnParameters() {
        if (params.length < 3) {
            throw new RuntimeException("Your youtube configuration is incorrect, please check your documentation");
        }

        return params[0];
    }

    public String getAPIBasedOnParameters() {
        if (params.length < 3) {
            throw new RuntimeException("Your youtube configuration is incorrect, please check your documentation");
        }

        return params[2];
    }

    public static void playVideo(Video video, String apiKey, Context context){
        Intent intent = new Intent(context,
                YouTubePlayerActivity.class);
        intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.getId());
        intent.putExtra(YouTubePlayerActivity.EXTRA_API_KEY, apiKey);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public static void openExternally(Video video, Context context){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse("vnd.youtube:" + video.getId()));
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            HolderActivity.startWebViewActivity(context, "http://www.youtube.com/watch?v=" + video.getId(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);

        }
    }

    public static void openComments(Video video, String api, Context context){
        // Start NewActivity.class
        Intent commentIntent = new Intent(context,
                CommentsActivity.class);
        commentIntent.putExtra(CommentsActivity.DATA_TYPE,
                CommentsActivity.YOUTUBE);
        commentIntent.putExtra(CommentsActivity.DATA_ID,
                video.getId());
        commentIntent.putExtra(CommentsActivity.DATA_KEY, api);
        context.startActivity(commentIntent);
    }
}