package com.sherdle.universal.providers.videos.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.sherdle.universal.Config;
import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.attachmentviewer.ui.VideoPlayerActivity;
import com.sherdle.universal.comments.CommentsActivity;
import com.sherdle.universal.providers.videos.api.object.Video;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import vimeoextractor.OnVimeoExtractionListener;
import vimeoextractor.VimeoExtractor;
import vimeoextractor.VimeoVideo;

/**
 * This is class gets the videos from youtube and parses the result
 */
public class VimeoClient implements VideoProvider {

    private static int PER_PAGE = 20;

    private static String API_BASE = "https://api.vimeo.com";
    private static String API_TYPE_ALBUM = "/albums/";
    private static String API_TYPE_USER = "/users/";

    private static String TYPE_ALBUM = "album";
    private static String TYPE_USER = "user";

    private VideosCallback callback;
    private String[] params;
    private WeakReference<Activity> activityReference;

    private String currentType;
    private int currentPage;
    private boolean hasNextPage;
    private String accessToken;

    public VimeoClient(String[] params, Activity activity, VideosCallback callback) {
        this.activityReference = new WeakReference<>(activity);
        this.params = params;
        this.callback = callback;

        currentType = getTypeBasedOnParameters();
    }

    @Override
    public void requestVideos(String pageToken, String searchQuery) {

        if (pageToken == null) {
            currentPage = 1;
        } else {
            currentPage = Integer.parseInt(pageToken);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                accessToken = activityReference.get().getResources().getString(R.string.vimeo_access_token);

                ArrayList<Video> result = null;
                if (currentType.equals(TYPE_ALBUM)) {
                    String retrievalUrl = API_BASE + API_TYPE_ALBUM  + getIdBasedOnParameters() + "/videos?per_page=" + PER_PAGE + "&page=" + currentPage+ "&access_token=" + accessToken;
                    if (searchQuery != null) {
                        retrievalUrl += "&query=" + searchQuery;
                    }
                    result = getVideos(retrievalUrl);
                } else if (currentType.equals(TYPE_USER)) {
                    String retrievalUrl = API_BASE + API_TYPE_USER  + getIdBasedOnParameters() + "/videos?per_page=" + PER_PAGE + "&page=" + currentPage+ "&access_token=" + accessToken;
                    if (searchQuery != null) {
                        retrievalUrl += "&query=" + searchQuery;
                    }
                    result = getVideos(retrievalUrl);
                }

                final ArrayList<Video> videos = result;

                if (activityReference.get() == null) return;
                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {

                        if (videos != null) {
                            boolean canLoadMore = hasNextPage;
                            callback.completed(videos, canLoadMore, Integer.toString(currentPage + 1));
                        } else {
                            callback.failed();
                        }
                    }
                });

            }
        });

    }

    private ArrayList<Video>  getVideos(String apiUrl) {
        ArrayList<Video> videos = null;
        // Making HTTP request

        JSONObject json = Helper.getJSONObjectFromUrl(apiUrl);

        if (json == null) {
            return null;
        }

        try {
            videos = new ArrayList<>();

            hasNextPage = json.has("paging") && !json.getJSONObject("paging").isNull("next");

            JSONArray jsonArray = json.getJSONArray("data");

            // Create a list to store the videos in
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String title = jsonObject.getString("name");
                    Date updated = formatData(jsonObject.getString("created_time"));
                    String description = jsonObject.getString("description");
                    String user = jsonObject.getJSONObject("user").getString("name");
                    String uri  = jsonObject.getString("uri");
                    String id = uri.replaceAll("/videos/", "");

                    String thumbUrl = null;
                    String image = null;
                    JSONArray sizes = jsonObject.getJSONObject("pictures").getJSONArray("sizes");
                    for (int j = 0; j < sizes.length(); j++) {
                        JSONObject size = (JSONObject) sizes.get(j);
                        if (size.getInt("width") == 200) {
                            thumbUrl = size.getString("link");
                        } else if (size.getInt("width") == 1920) {
                            image = size.getString("link");
                        }

                        if (image != null && thumbUrl != null) break;
                    }

                    String shareUrl = jsonObject.getString("link");

                    Video video = new Video(title, id, updated, description, thumbUrl, image, user, shareUrl);
                    videos.add(video);
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
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
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
        return true;
    }

    @Override
    public boolean isYoutubeLive() {
        return false;
    }

    private String getTypeBasedOnParameters() {
        if (params.length < 2 || (
                !params[1].equals(TYPE_ALBUM) &&
                        !params[1].equals(TYPE_USER))) {
            throw new RuntimeException("Your vimeo configuration is incorrect, please check your documentation");
        }

        return params[1];
    }

    public String getIdBasedOnParameters() {
        if (params.length < 2) {
            throw new RuntimeException("Your vimeo configuration is incorrect, please check your documentation");
        }

        return params[0];
    }

    public static void playVideo(Video video, Context context){
        VimeoExtractor.getInstance().fetchVideoWithURL(video.getLink(), null, new OnVimeoExtractionListener() {
            @Override
            public void onSuccess(VimeoVideo video) {
                String hdStream = video.getStreams().get(video.isHD() ? "1080p" : "480p");
                VideoPlayerActivity.startActivity(context, hdStream);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.i("INFO", "Unable to retrieve vimeo video url for native player. Now opening in browser as fallback");
                openExternally(video, context);
            }
        });
    }

    public static void openExternally(Video video, Context context){
        HolderActivity.startWebViewActivity(context, video.getLink(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);
    }
}