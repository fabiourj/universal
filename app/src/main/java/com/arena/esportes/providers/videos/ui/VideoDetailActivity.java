package com.arena.esportes.providers.videos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.arena.esportes.Config;
import com.arena.esportes.R;
import com.arena.esportes.providers.Provider;
import com.arena.esportes.providers.fav.FavDbAdapter;
import com.arena.esportes.providers.videos.PicassoVideoThumbnailHandler;
import com.arena.esportes.providers.videos.api.VimeoClient;
import com.arena.esportes.providers.videos.api.WordpressClient;
import com.arena.esportes.providers.videos.api.YoutubeClient;
import com.arena.esportes.providers.videos.api.object.Video;
import com.arena.esportes.util.DetailActivity;
import com.arena.esportes.util.Helper;
import com.arena.esportes.util.WebHelper;
import com.squareup.picasso.Picasso;

/**
 * This activity is used to display the details of a video
 */

public class VideoDetailActivity extends DetailActivity {

	private FavDbAdapter mDbHelper;
	private TextView mPresentation;
	private Video video;
	private String provider;

    public static final String EXTRA_VIDEO = "videoitem";
	public static final String EXTRA_PROVIDER = "provider";
	public static final String EXTRA_PARAMS = "params";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Use the general detaillayout and set the viewstub for video layout
		setContentView(R.layout.activity_details);
		ViewStub stub = findViewById(R.id.layout_stub);
		stub.setLayoutResource(R.layout.activity_video_detail);
		View inflated = stub.inflate();

		mToolbar = findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mPresentation = findViewById(R.id.youtubetitle);
		TextView detailsDescription = findViewById(R.id.youtubedescription);
		TextView detailsSubTitle = findViewById(R.id.youtubesubtitle);

		provider = getIntent().getStringExtra(EXTRA_PROVIDER);
		String[] params = getIntent().getStringArrayExtra(EXTRA_PARAMS);

        video = (Video) getIntent().getSerializableExtra(EXTRA_VIDEO);
        if (video.getApiParams() == null) video.setApiParams(params);

        detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                WebHelper.getTextViewFontSize(this));

		mPresentation.setText(video.getTitle());

		detailsDescription.setText(provider.equals(Provider.YOUTUBE) || provider.equals(Provider.VIMEO) ? video.getDescription() : Html.fromHtml(video.getDescription()));

        String dateString = DateUtils.getRelativeDateTimeString(this, video.getUpdated().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();

        String subText = getResources().getString(R.string.video_subtitle_start) +
				dateString +
				getResources().getString(R.string.video_subtitle_end) + 
				video.getChannel();
		detailsSubTitle.setText(subText);

//////ADMOB BANNER//////////////////////////////////////////////////////////////////////////////
/*
		if (Config.ADMOB_YOUTUBE && provider.equals(Provider.YOUTUBE))
			Helper.admobLoader(this, findViewById(R.id.adView));
		else
			findViewById(R.id.adView).setVisibility(View.GONE);
*/
/////////////////////////////////////////////////////////////////////////////////////////
		thumb = findViewById(R.id.image);
		coolblue = findViewById(R.id.coolblue);

		Picasso pic = PicassoVideoThumbnailHandler.picassoWithVideoSupport(this);
		if (video.getImage() != null) {
			pic.load(video.getImage()).into(thumb);
			setUpHeader(video.getImage());
		} else {
			pic.load(video.getDirectVideoUrl()).into(thumb);
			setUpHeader(video.getDirectVideoUrl());
		}

		FloatingActionButton btnPlay = findViewById(R.id.playbutton);
		btnPlay.bringToFront();
		// Listening to button event
		btnPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				if (provider.equals(Provider.YOUTUBE))
					YoutubeClient.playVideo(video, video.getApiParams()[2],VideoDetailActivity.this);
				else if (provider.equals(Provider.VIMEO))
					VimeoClient.playVideo(video, VideoDetailActivity.this);
				else
					WordpressClient.playVideo(video, VideoDetailActivity.this);
			}
		});

		Button btnFav = findViewById(R.id.favorite);

		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(VideoDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent(video.getTitle(), video, provider)) {
					// Item is new
					mDbHelper.addFavorite(video.getTitle(), video, provider);
					Toast toast = Toast
							.makeText(VideoDetailActivity.this, getResources()
									.getString(R.string.favorite_success),
									Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(
							VideoDetailActivity.this,
							getResources().getString(
									R.string.favorite_duplicate),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		Button btnComment = findViewById(R.id.comments);
		if (provider.equals(Provider.VIMEO)) btnComment.setVisibility(View.GONE);
		btnComment.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				if (provider.equals(Provider.YOUTUBE))
					YoutubeClient.openComments(video, params[2], VideoDetailActivity.this);
				else
					WordpressClient.openComments(video, VideoDetailActivity.this, params);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_share:
			String applicationName = getResources()
					.getString(R.string.app_name);
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);

			String urlvalue = getResources().getString(
					R.string.video_share_begin);
			String seenvalue = getResources().getString(
					R.string.video_share_middle);
			String appvalue = getResources()
					.getString(R.string.video_share_end);
			// this is the text that will be shared
			sendIntent.putExtra(Intent.EXTRA_TEXT, (urlvalue
					+ video.getLink() + seenvalue
					+ applicationName + appvalue));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, video.getTitle());
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources()
					.getString(R.string.share_header)));

			return true;
		case R.id.menu_view:
			if (provider.equals(Provider.YOUTUBE))
				YoutubeClient.openExternally(video, this);
			else if (provider.equals(Provider.VIMEO))
				VimeoClient.openExternally(video, this);
			else
				WordpressClient.openExternally(video, this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.youtube_detail_menu, menu);
		return true;
	}

}
