package com.sherdle.universal.providers.social.facebook;

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
import com.sherdle.universal.attachmentviewer.model.Attachment;
import com.sherdle.universal.inherit.CollapseControllingFragment;
import com.sherdle.universal.providers.social.SocialPost;
import com.sherdle.universal.providers.social.SocialPostAdapter;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.Log;
import com.sherdle.universal.util.ThemeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * This fragment is used to display a list of facebook posts
 */

public class FacebookFragment extends Fragment implements InfiniteRecyclerViewAdapter.LoadMoreListener, CollapseControllingFragment {

	private RecyclerView listView = null;
	private ArrayList<SocialPost> postsList;
	private SocialPostAdapter postListAdapter = null;

	private Activity mAct;

	private RelativeLayout ll;

	String nextpageurl;
	String username;
	String facebook_access_token;
	Boolean isLoading = false;

//////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static String API_URL_BEGIN = "https://graph.facebook.com/v17.0/";
///////////////////////////////////////////////////////////////////////////////////////////////////////////	

//	private static String API_URL_BEGIN = "https://graph.facebook.com/v7.0/";
	private static String API_URL_MIDDLE = "/posts/?access_token=";
	private static String API_URL_END = "&date_format=U&fields=comments.limit(50).summary(1),likes.limit(0).summary(1),from,picture,message,story,id,created_time,full_picture,attachments{title,url_unshimmed,media,media_type,subattachments}&limit=10";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		facebook_access_token = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[1];

		listView = ll.findViewById(R.id.list);
		postsList = new ArrayList<>();
		postListAdapter = new SocialPostAdapter(getContext(), postsList, this);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
		listView.setAdapter(postListAdapter);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		refreshItems();
	}


	public void updateList(ArrayList<SocialPost> posts) {
		if (posts.size() > 0) {
			postsList.addAll(posts);
		}

		if (nextpageurl == null)
			postListAdapter.setHasMore(false);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
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
				nextpageurl = (API_URL_BEGIN + username + API_URL_MIDDLE  + facebook_access_token + API_URL_END);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<SocialPost> posts) {

			if (null != posts && posts.size() > 0) {
				updateList(posts);
			} else if (posts == null){
				Helper.noConnection(mAct);

				postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
			}

			isLoading = false;
		}

		@Override
		protected ArrayList<SocialPost> doInBackground(String... params) {
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}


	public ArrayList<SocialPost> parseJson(JSONObject json) {
		ArrayList<SocialPost> postList = new ArrayList<>();
		try {
			if (json.has("paging") && json.getJSONObject("paging").has("next"))
				nextpageurl = json.getJSONObject("paging").getString("next");
			else
				nextpageurl = null;

			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
            	 try {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 SocialPost post = new SocialPost(SocialPost.PostSource.Facebook);
                 post.id = photoJson.getString("id");
                 post.link = "https://www.facebook.com/" + post.id;
                 post.postType = SocialPost.PostType.TEXT;

				 if (photoJson.has("attachments")
						 && photoJson.getJSONObject("attachments").has("data") &&
						 photoJson.getJSONObject("attachments").getJSONArray("data").length() > 0) {
					 JSONObject firstAttachment = photoJson.getJSONObject("attachments").getJSONArray("data").getJSONObject(0);

					 if (firstAttachment.has("media") && firstAttachment.getJSONObject("media").has("source"))
						 post.link = firstAttachment.getJSONObject("media").getString("source");

					 switch(firstAttachment.getString("media_type")) {
						 case "photo":
						 case "album":
							 post.postType = SocialPost.PostType.IMAGE;
							 break;
						 case "video":
							 post.postType = SocialPost.PostType.VIDEO;
							 break;
						 default:
							 post.postType = SocialPost.PostType.TEXT;
							 break;
					 }

					 if (post.postType == SocialPost.PostType.VIDEO) {
						 if (firstAttachment.has("media") && firstAttachment.getJSONObject("media").has("source")) {
							 post.videoUrl = firstAttachment.getJSONObject("media").getString("source");
						 } else if (photoJson.has("source")){
							 post.videoUrl = photoJson.getString("source");
						 } else {
						 	post.postType = SocialPost.PostType.TEXT;
						 }
					 }
				 }

                 post.username = photoJson.getJSONObject("from").getString("name");
                 post.profilePhotoUrl = "https://graph.facebook.com/" + photoJson.getJSONObject("from").getString("id") + "/picture?type=large";
                 post.createdTime = new Date(photoJson.getLong("created_time") * 1000);
                 post.likesCount = photoJson.getJSONObject("likes").getJSONObject("summary").getInt("total_count");

                 if (photoJson.has("message")){
                	 post.caption = photoJson.getString("message");
                 } else if (photoJson.has("story")){
                	 post.caption = photoJson.getString("story");
                 } else {
                	 post.caption = "";
                 }

                 if (photoJson.has("attachments")
						 && photoJson.getJSONObject("attachments").has("data")
				 		&& photoJson.getJSONObject("attachments").getJSONArray("data").length() > 0) {
                 	JSONArray attachments = photoJson.getJSONObject("attachments").getJSONArray("data");

                 	ArrayList<String> allMediaUrls = new ArrayList<>();
					 for (int j = 0; j < attachments.length(); j++) {
						 JSONObject attachment = attachments.getJSONObject(j);

						 if (attachment.has("subattachments")
								 && attachment.getJSONObject("subattachments").has("data")){
							 JSONArray subAttachments = attachment.getJSONObject("subattachments").getJSONArray("data");
							 for (int k = 0; k < subAttachments.length(); k++) {
								 JSONObject subAttachment = subAttachments.getJSONObject(k);
								 if (subAttachment.has("media")
										 && subAttachment.getJSONObject("media").has("image")){
								 	allMediaUrls.add(subAttachment.getJSONObject("media").getJSONObject("image").getString("src"));
								 }
							 }
						 } else if (attachment.has("media")
								 && attachment.getJSONObject("media").has("image")){
							 allMediaUrls.add(attachment.getJSONObject("media").getJSONObject("image").getString("src"));
						 }
					 }
					 post.imageUrls.addAll(allMediaUrls);
                 } else if (photoJson.has("full_picture")){
                	 post.imageUrls.add(photoJson.getString("full_picture"));
                 }

                 post.commentsOrRetweetCount = photoJson.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
                 post.commentsJson = photoJson.getJSONObject("comments").toString();

                 // Add to array list
                 postList.add(post);
            	 } catch (Exception e) {
         			Log.e("INFO", "Item " + i +" skipped because of exception");
         			Log.printStackTrace(e);
         		}
			}

			return postList;
		} catch (Exception e) {
			Log.printStackTrace(e);

			return null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);
		ThemeUtils.tintAllIcons(menu, mAct);
	}

	public void refreshItems(){
		postsList.clear();
		postListAdapter.setHasMore(true);
		postListAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
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
