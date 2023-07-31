package com.sherdle.universal.providers.social.twitter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sherdle.universal.MainActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.inherit.CollapseControllingFragment;
import com.sherdle.universal.providers.social.SocialPost;
import com.sherdle.universal.providers.social.SocialPostAdapter;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.Log;
import com.sherdle.universal.util.ThemeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *  This activity is used to display a list of tweets
 */

public class TweetsFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener, CollapseControllingFragment {

	private RecyclerView listView;
	private SocialPostAdapter tweetAdapter;
	private ArrayList<SocialPost> tweets;
	
	private Activity mAct;
	private RelativeLayout ll;
	
	String searchValue;
	String latesttweetid;
	String perpage = "25";

	Boolean isLoading = true;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);
		setHasOptionsMenu(true);
		
		searchValue =  this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		listView = ll.findViewById(R.id.list);

		tweets = new ArrayList<>();
		tweetAdapter = new SocialPostAdapter(getContext(), tweets, this);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		listView.setAdapter(tweetAdapter);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

	    return ll;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		refreshItems();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	if (!isLoading){
	    		refreshItems();
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	public void refreshItems(){
		isLoading = true;
		latesttweetid = null;

		tweets.clear();
		tweetAdapter.setHasMore(true);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		new SearchTweetsTask().execute(searchValue);
	}
	
	public void updateList(ArrayList<SocialPost> result) {
		if (result.size() > 0) {
			tweets.addAll(result);
		}

		if (result.size() == 0)
			tweetAdapter.setHasMore(false);
		tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
	}

	@Override
	public void onMoreRequested() {
		if (!isLoading){
			new SearchTweetsTask().execute(searchValue);
		}
	}

	@Override
	public boolean supportsCollapse() {
		return true;
	}

	@Override
	public boolean dynamicToolbarElevation() {
		return false;
	}

	//Connect to twitter api and get values.
	private class SearchTweetsTask extends AsyncTask<String, Void, ArrayList<SocialPost>>{

		private String URL_VALUE;
		private final String URL_BASE = "https://api.twitter.com";
		private final String URL_TIMELINE = URL_BASE + "/1.1/statuses/user_timeline.json?" +
				"count="+perpage+"&" +
				"tweet_mode=extended&" +
				"exclude_replies=true&" +
				"include_rts=1&" +
				"screen_name=";
		private final String URL_SEARCH = URL_BASE + "/1.1/search/tweets.json?count="+perpage+"&q=";
		private final String URL_PARAM = "&max_id=";
		private final String URL_AUTH = URL_BASE + "/oauth2/token";

		private final String CONSUMER_KEY = getResources().getString(R.string.twitter_api_consumer_key);
		private final String CONSUMER_SECRET = getResources().getString(R.string.twitter_api_consumer_secret_key);

		private String authenticateApp(){

			HttpURLConnection connection = null;
			StringBuilder reply = null;

			try {
				URL url = new URL(URL_AUTH);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);

				// Encoding keys
				String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
				String authorisation = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
				String parameter = "grant_type=client_credentials";

				// Sending credentials
				connection.addRequestProperty("Authorization", authorisation);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				connection.connect();
				
				// sending parameters to method
				try (OutputStream os = connection.getOutputStream()) {
					os.write(parameter.getBytes());
					os.flush();
					os.close();

					try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
						String line;
						reply = new StringBuilder();

						while ((line = br.readLine()) != null) {
							reply.append(line);
						}

						Log.d("Post response", String.valueOf(connection.getResponseCode()));
						Log.d("Json response - tokenk", reply.toString());
					}
				}

			} catch (Exception e) {
				Log.e("INFO", "Exception: " + e.toString());
				
			}finally{
				if (connection != null) {
					connection.disconnect();
				}
			}
			if (reply == null) return null;
			return reply.toString();
		}
		

		//Showing the progressdialog while loading data in background
		@Override
		protected void onPreExecute(){
			super.onPreExecute();

			isLoading = true;
		}


		//Get the latest tweets from the timeline of the user
		@Override
		protected ArrayList<SocialPost> doInBackground(String... param) {

			String searchValue = param[0];
			Boolean search = false;
			if (searchValue.startsWith("?")){
				URL_VALUE = URL_SEARCH;
				try {
					searchValue = URLEncoder.encode(searchValue.substring(1), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
				search = true;
			} else {
				URL_VALUE = URL_TIMELINE;
			}
			ArrayList<SocialPost> result = null;
			HttpURLConnection connection = null;

			try {
				URL url;
				if (null != latesttweetid && !latesttweetid.equals("")) {
					Long fromid = Long.parseLong(latesttweetid) - 1;
					url = new URL(URL_VALUE + searchValue + URL_PARAM + fromid);
				}else {
					url = new URL(URL_VALUE + searchValue);
				}
				Log.v("INFO", "Requesting: " + url.toString());
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				String jsonString = authenticateApp();
				JSONObject jsonAccess = new JSONObject(jsonString);
				String tokenHolder = jsonAccess.getString("token_type") + " " + 
						jsonAccess.getString("access_token");

				connection.setRequestProperty("Authorization", tokenHolder);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.connect();

				// retrieve tweets from api
				String line;
				StringBuilder reply = new StringBuilder();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					while ((line = br.readLine()) != null) {
						reply.append(line);
					}
				}

				Log.d("GET response", String.valueOf(connection.getResponseCode()));
				Log.d("JSON response", reply.toString());
				
				JSONArray jsonArray;
				JSONObject jsonObject;
				
				if (search){
					JSONObject obj = new JSONObject(reply.toString());
					jsonArray = obj.getJSONArray("statuses");
				} else {	
					jsonArray = new JSONArray(reply.toString());
				}

				result = new ArrayList<>();

				for (int i = 0; i < jsonArray.length(); i++) {
					
					jsonObject = (JSONObject) jsonArray.get(i);
					SocialPost tweet = new SocialPost(SocialPost.PostSource.Twitter);

					tweet.username = jsonObject.getJSONObject("user").getString("name");
					String username = jsonObject.getJSONObject("user").getString("screen_name");
					tweet.profilePhotoUrl = jsonObject.getJSONObject("user").getString("profile_image_url").replace("_normal", "");
					tweet.caption = jsonObject.has("full_text") ? jsonObject.getString("full_text") : jsonObject.getString("text");
					tweet.likesCount = jsonObject.getInt("favorite_count");
					tweet.commentsOrRetweetCount = jsonObject.getInt("retweet_count");
					tweet.createdTime = parseDate(jsonObject.getString("created_at"));
					tweet.id = jsonObject.getString("id");
					tweet.link = ("http://twitter.com/" + username + "/status/" + tweet.id);

					String firstEntityType = "";
					if (jsonObject.has("extended_entities")
							&& jsonObject.getJSONObject("extended_entities").has("media")
							&& jsonObject.getJSONObject("extended_entities").getJSONArray("media").length() > 0) {
						JSONArray mediaArray = jsonObject.getJSONObject("extended_entities").getJSONArray("media");
						firstEntityType = mediaArray.getJSONObject(0).getString("type");

						for (int j = 0; j < mediaArray.length(); j++) {
							JSONObject mediaItem = mediaArray.getJSONObject(j);
							tweet.imageUrls.add(mediaItem.getString("media_url"));
						}
					}
					switch(firstEntityType) {
						case "animated_gif":
						case "photo":
							tweet.postType = SocialPost.PostType.IMAGE;
							break;
						case "video":
							tweet.postType = SocialPost.PostType.VIDEO;
							tweet.videoUrl = jsonObject.getJSONObject("extended_entities")
									.getJSONArray("media").getJSONObject(0)
									.getJSONObject("video_info").getJSONArray("variants")
									.getJSONObject(0).getString("url");
							break;
						default:
							tweet.postType = SocialPost.PostType.TEXT;
							break;
					}

					latesttweetid = jsonObject.getString("id");

					result.add(i, tweet);
				}

			} catch (Exception e) {
				Log.printStackTrace(e);
				Log.e("INFO", "Exception: GET " + e.toString());

			}finally {
				if(connection != null){
					connection.disconnect();
				}
			}
			return result;
		}

		//Populate listview with tweets after background task has been completed. If results are empty
		//then show error toast.
		@Override
		protected void onPostExecute(ArrayList<SocialPost> result){
			isLoading = false;
			if (null != result) {
				updateList(result);
			} else {
				Helper.noConnection(mAct);
				tweetAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}
		}

	}

	@SuppressLint("SimpleDateFormat")
	private Date parseDate(String date){
		String data = date.replaceFirst("(\\s[+|-]\\d{4})", "");
		TimeZone tzUTC = TimeZone.getTimeZone("UTC");
		SimpleDateFormat formatEntry = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
		formatEntry.setTimeZone(tzUTC);

		try {
			return formatEntry.parse(data);
		} catch (ParseException e) {
			Log.e("Error parsing data", e.toString());
			return null;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.refresh_menu, menu);

		ThemeUtils.tintAllIcons(menu, mAct);
	}
	

}
