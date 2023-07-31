package com.sherdle.universal.providers.social.instagram;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This activity is used to display a list of instagram photos
 */

public class InstagramFragment extends Fragment  implements InfiniteRecyclerViewAdapter.LoadMoreListener, CollapseControllingFragment {

	private RecyclerView postsListView = null;
	private ArrayList<SocialPost> postsList;
	private SocialPostAdapter postsListAdapter = null;

	private Activity mAct;
	private RelativeLayout ll;

	private String nextpageurl;
	String username;
	String instagram_access_token;
	Boolean isLoading = false;

	private static String API_URL = "https://graph.facebook.com/v3.1/";
	private static String API_URL_END = "/media?fields=caption,id,ig_id,comments_count,timestamp,permalink,owner{profile_picture_url,name},media_url,media_type,thumbnail_url,like_count,comments{text,username},username,children{media_url,media_type}&access_token=";

	private static final SimpleDateFormat INSTAGRAM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());

    @SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		instagram_access_token = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[1];

		postsListView = ll.findViewById(R.id.list);
		postsList = new ArrayList<>();
		postsListAdapter = new SocialPostAdapter(getContext(), postsList, this);
		postsListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		postsListView.setAdapter(postsListAdapter);
		postsListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		refreshItems();
	}


	public void updateList(ArrayList<SocialPost> photosList) {
		if (photosList.size() > 0) {
			this.postsList.addAll(photosList);
		}

		if (nextpageurl == null || photosList.size() == 0)
			postsListAdapter.setHasMore(false);
		postsListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);

	}

	@Override
	public void onMoreRequested() {
		if (!isLoading && nextpageurl != null) {
			new DownloadFilesTask(false).execute();
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

	private class DownloadFilesTask extends AsyncTask<String, Integer, ArrayList<SocialPost>> {

		boolean initialload;

		DownloadFilesTask(boolean firstload) {
			this.initialload = firstload;
		}

		@Override
		protected void onPreExecute() {
			if (isLoading) {
				this.cancel(true);
			} else {
				isLoading = true;
			}
			if (initialload) {
				nextpageurl = (API_URL + username + API_URL_END  + instagram_access_token);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<SocialPost> result) {
			if (null != result && result.size() > 0) {
				updateList(result);
			} else {
				Helper.noConnection(mAct);
				postsListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}

			isLoading = false;
		}

		@Override
		protected ArrayList<SocialPost> doInBackground(String... params) {
			//Getting data from url and parsing JSON
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}

	public ArrayList<SocialPost> parseJson(JSONObject json) {
		ArrayList<SocialPost> postsList = new ArrayList<>();

		try {
            if (json.has("paging") && json.getJSONObject("paging").has("next"))
                nextpageurl = json.getJSONObject("paging").getString("next");
            else
                nextpageurl = null;

			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
				 try {
					 JSONObject postJson = dataJsonArray.getJSONObject(i);
					 SocialPost post = new SocialPost(SocialPost.PostSource.Instagram);
					 post.id = postJson.getString("ig_id");
					 post.username = postJson.getString("username");
					 post.profilePhotoUrl = postJson.getJSONObject("owner").getString("profile_picture_url");
					 if (postJson.has("caption") && !postJson.isNull("caption")){
						 post.caption = postJson.getString("caption");
					 }
					 post.createdTime = INSTAGRAM_DATE_FORMAT.parse(postJson.getString("timestamp"));
					 post.likesCount = postJson.getInt("like_count");
					 post.link = postJson.getString("permalink");
					 if (postJson.has("comments"))
						post.commentsJson = postJson.getJSONObject("comments").toString();

					 switch(postJson.getString("media_type")) {
						 case "CAROUSEL_ALBUM":
						 case "IMAGE":
							 post.postType = SocialPost.PostType.IMAGE;
							 break;
						 case "VIDEO":
							 post.postType = SocialPost.PostType.VIDEO;
							 break;
						 default:
							 post.postType = SocialPost.PostType.TEXT;
							 break;
					 }

					 if (post.postType == SocialPost.PostType.VIDEO) {
						 post.videoUrl = postJson.getString("media_url");
						 post.imageUrls.add(postJson.getString("thumbnail_url"));
					 } else {
						 if (postJson.has("children") && postJson.getJSONObject("children").getJSONArray("data").length() > 0) {

							 JSONArray childPhotos = postJson.getJSONObject("children").getJSONArray("data");
							 for (int a = 0; a < childPhotos.length(); a++) {
								 JSONObject childPhoto = childPhotos.getJSONObject(a);
								 if (!childPhoto.getString("media_type").equals("IMAGE")){
									continue;
								 }
								 post.imageUrls.add(childPhoto.getString("media_url"));
							 }
						 } else {
							 post.imageUrls.add(postJson.getString("media_url"));
						 }
					 }

					 post.commentsOrRetweetCount = postJson.getInt("comments_count");

					 // Add to array list
					 postsList.add(post);

				 } catch (Exception e) {
					 Log.printStackTrace(e);
				 }
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
		}

        return postsList;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);
		ThemeUtils.tintAllIcons(menu, mAct);
	}

	public void refreshItems(){
		postsList.clear();
		postsListAdapter.setHasMore(true);
		postsListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		new DownloadFilesTask(true).execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
			if (!isLoading) {
				refreshItems();
			} else {
				Toast.makeText(mAct, getString(R.string.already_loading),
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
