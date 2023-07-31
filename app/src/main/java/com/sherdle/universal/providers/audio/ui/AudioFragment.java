package com.sherdle.universal.providers.audio.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.MainActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.inherit.BackPressFragment;
import com.sherdle.universal.inherit.CollapseControllingFragment;
import com.sherdle.universal.providers.Provider;
import com.sherdle.universal.providers.audio.TracksAdapter;
import com.sherdle.universal.providers.audio.api.SoundCloudClient;
import com.sherdle.universal.providers.audio.api.WordpressClient;
import com.sherdle.universal.providers.audio.api.object.SoundCloudResult;
import com.sherdle.universal.providers.audio.api.object.TrackObject;
import com.sherdle.universal.providers.audio.helpers.EndlessRecyclerOnScrollListener;
import com.sherdle.universal.providers.audio.player.player.CheerleaderPlayer;
import com.sherdle.universal.providers.audio.player.player.CheerleaderPlaylistListener;
import com.sherdle.universal.providers.audio.ui.views.PlaybackView;
import com.sherdle.universal.providers.audio.ui.views.TrackView;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.Log;
import com.sherdle.universal.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * This fragment is used to display a list of SoundCloud tracks
 */

public class AudioFragment extends Fragment implements
        PlaybackView.Listener, CheerleaderPlaylistListener, BackPressFragment, CollapseControllingFragment {

    // Static
    private static final int PER_PAGE = 20;

    // sound cloud
    private CheerleaderPlayer mCheerleaderPlayer;

    // tracks widget
    private RecyclerView mRetrieveTracksRecyclerView;
    private TrackView.Listener mRetrieveTracksListener;
    private ArrayList<TrackObject> mRetrievedTracks;
    private TracksAdapter mAdapter;

    // player widget
    private RecyclerView mPlaylistRecyclerView;
    private PlaybackView mPlaybackView;
    private TracksAdapter mPlaylistAdapter;
    private ArrayList<TrackObject> mPlaylistTracks;

    private TrackView.Listener mPlaylistTracksListener;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;

    //Fragment
    private Activity mAct;
    private FrameLayout ll;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ll = (FrameLayout) inflater.inflate(R.layout.fragment_audio,
                container, false);
        setHasOptionsMenu(true);

        mRetrieveTracksRecyclerView = ll.findViewById(R.id.recyclerview);
        mPlaylistRecyclerView = ll.findViewById(R.id.activity_artist_playlist);

        return ll;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        mCheerleaderPlayer = initPlayer();

        initRetrieveTracksRecyclerView();
        initPlaylistTracksRecyclerView();
        setTrackListPadding();

        // check if tracks are already loaded into the player.
        ArrayList<TrackObject> currentsTracks = mCheerleaderPlayer.getTracks();
        if (currentsTracks != null) {
            mPlaylistTracks.addAll(currentsTracks);
        }

        // synchronize the player view with the current player (loaded track, playing state, etc.)
        mPlaybackView.synchronize(mCheerleaderPlayer);
    }

    private CheerleaderPlayer initPlayer(){
        Bundle bundle = getArguments();
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, this.getClass());

        return new CheerleaderPlayer.Builder()
                .from(mAct)
                .with(R.string.soundcloud_id)
                .notificationActivity(new HolderActivity())
                .notificationIcon(R.drawable.ic_radio_playing)
                .notificationBundle(bundle)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCheerleaderPlayer.isClosed())
            mCheerleaderPlayer = initPlayer();

        mCheerleaderPlayer.registerPlayerListener(mPlaybackView);
        mCheerleaderPlayer.registerPlayerListener(mPlaylistAdapter);
        mCheerleaderPlayer.registerPlaylistListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCheerleaderPlayer.unregisterPlayerListener(mPlaybackView);
            mCheerleaderPlayer.unregisterPlayerListener(mPlaylistAdapter);
            mCheerleaderPlayer.unregisterPlaylistListener(this);
        } catch (Exception e) {
            //As a fault in 'un-registering' won't matter, we ignore it.
            Log.v("INFO", "Unable to unregister player listeners");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCheerleaderPlayer.destroy();
    }

    @Override
    public void onTogglePlayPressed() {
        mCheerleaderPlayer.togglePlayback();
    }

    @Override
    public void onPreviousPressed() {
        mCheerleaderPlayer.previous();
    }

    @Override
    public void onNextPressed() {
        mCheerleaderPlayer.next();
    }

    @Override
    public void onSeekToRequested(int milli) {
        mCheerleaderPlayer.seekTo(milli);
    }

    @Override
    public void onTrackAdded(TrackObject track) {
        if (mPlaylistTracks.isEmpty()) {
            mPlaylistRecyclerView.setVisibility(View.VISIBLE);
            mPlaylistRecyclerView.animate().translationY(0);

            int headerListHeight = getResources().getDimensionPixelOffset(R.dimen.playback_view_height);
            mRetrieveTracksRecyclerView.setPadding(0, 0, 0, headerListHeight);
        }
        mPlaylistTracks.add(track);
        mPlaylistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTrackRemoved(TrackObject track, boolean isEmpty) {
        if (mPlaylistTracks.remove(track)) {
            mPlaylistAdapter.notifyDataSetChanged();
        }
        if (isEmpty) {
            mPlaylistRecyclerView.animate().translationY(mPlaybackView.getHeight());
            mPlaylistRecyclerView.setLayoutAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    mPlaylistRecyclerView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.soundcloud_menu, menu);
        ThemeUtils.tintAllIcons(menu, mAct);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:
                mRetrievedTracks.clear();
                mEndlessRecyclerOnScrollListener.reset();
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.play:
                if (mCheerleaderPlayer.isClosed()) return true;
                for (int i = 0; i < mCheerleaderPlayer.getTracks().size(); i++) {
                    mCheerleaderPlayer.removeTrack(i);
                }
                mCheerleaderPlayer.addTracks(mRetrievedTracks);
                if (!mCheerleaderPlayer.isPlaying()) mCheerleaderPlayer.play();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Used to position the track list at the bottom of the screen.
     */
    private void setTrackListPadding() {
        final ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                //Verify that the fragment is still attached
                if (getActivity() == null || !isAdded()) return;

                int headerListHeight = getResources().getDimensionPixelOffset(R.dimen.playback_view_height);
                int topPadding = mPlaylistRecyclerView.getMeasuredHeight() - headerListHeight;
                mPlaylistRecyclerView.setPadding(0, topPadding, 0, 0);
                mPlaylistRecyclerView.setAdapter(mPlaylistAdapter);

                // hide if current play playlist is empty.
                if (mPlaylistTracks.isEmpty()) {
                    mPlaylistRecyclerView.setVisibility(View.GONE);
                    mPlaylistRecyclerView.setTranslationY(headerListHeight);
                } else {
                    mRetrieveTracksRecyclerView.setPadding(0, 0, 0, headerListHeight);
                }
            }
        };

        mPlaylistRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

        //Fixes a bug where layout changes are performed after onGlobalLayout has been called
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPlaylistRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            }
        }, 500);
    }

    /**
     * Used to retrieved the tracks of the artist as well as artist details.
     */
    private void loadTracks(int page) {

        final int from = PER_PAGE * page;

        mAdapter.setFooterView(LayoutInflater.from(mAct)
                .inflate(R.layout.listview_footer, mPlaylistRecyclerView, false));
        mAdapter.notifyDataSetChanged();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                //Verify that the fragment is still attached
                if (getActivity() == null || !isAdded()) return;

                final List<TrackObject> tracks;
                final String[] arguments = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);

                if (!isWordpress()) {
                    SoundCloudClient api = new SoundCloudClient(getResources().getString(R.string.soundcloud_id), getResources().getString(R.string.soundcloud_secret));
                    long idToLoad = Long.parseLong(arguments[0]);
                    SoundCloudResult result = null;
                    if (!isPlaylist()) {
                        result = api.getListTrackObjectsOfUser(idToLoad, from, PER_PAGE);
                    } else {
                        result = api.getListTrackObjectsOfPlaylist(idToLoad, from, PER_PAGE);
                    }

                    tracks = (result == null) ? null : result.getTracks();
                    mEndlessRecyclerOnScrollListener.forceCantLoadMore(result == null || result.getNextPageUrl() == null);
                } else {
                    WordpressClient api = new WordpressClient(arguments[0]);
                    tracks = api.getTracksInCategory(arguments[1], from / PER_PAGE);
                    mEndlessRecyclerOnScrollListener.forceCantLoadMore(from / PER_PAGE >= api.getMaxPages());
                }

                mAct.runOnUiThread(new Runnable() {
                    public void run() {

                        mAdapter.setFooterView(null);

                        if (tracks != null) {
                            mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
                            if (tracks.size() > 0)
                                for (TrackObject newTrack : tracks) {
                                    boolean exists = false;
                                    for (TrackObject o: mRetrievedTracks) {
                                        if (o.getId() == newTrack.getId()) exists = true;
                                    }


                                    if (!exists)
                                        mRetrievedTracks.add(newTrack);
                                }
                               // mRetrievedTracks.addAll(tracks);
                        } else {
                            Helper.noConnection(mAct);
                            mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

    }

    private void showTrackActionsPopup(final TrackObject track, View source) {
        final PopupMenu popmenu = new PopupMenu(mAct, source);
        popmenu.getMenuInflater().inflate(R.menu.soundcloud_track_menu, popmenu.getMenu());

        popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_download:
                        String id = getResources().getString(R.string.soundcloud_id);
                        String secret = getResources().getString(R.string.soundcloud_secret);
                        Executors.newSingleThreadExecutor().execute(() -> {
                            Helper.download(mAct, track.getLinkStream(id, secret));
                        });
                        return true;
                    case R.id.menu_share:
                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("text/plain");

                        // Add data to the intent, the receiving app will decide
                        share.putExtra(Intent.EXTRA_TEXT, track.getPermalinkUrl());

                        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_header)));

                        return true;
                    default:
                        return false;
                }
            }
        });

        popmenu.show();
    }

    private void initRetrieveTracksRecyclerView() {
        mRetrieveTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(TrackObject track) {
                if (mCheerleaderPlayer.isClosed())
                    mCheerleaderPlayer = initPlayer();

                if (mCheerleaderPlayer.getTracks().contains(track)) {
                    mCheerleaderPlayer.play(track);
                } else {
                    boolean playNow = !mCheerleaderPlayer.isPlaying();

                    mCheerleaderPlayer.addTrack(track, playNow);
                    mPlaylistAdapter.notifyDataSetChanged();

                    if (!playNow) {
                        Toast.makeText(mAct, getResources().getString(R.string.toast_track_added), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onMoreClicked(TrackObject track, View source) {
                showTrackActionsPopup(track, source);
            }

        };

        mRetrievedTracks = new ArrayList<>();
        mAdapter = new TracksAdapter(getContext(), mRetrieveTracksListener, mRetrievedTracks);
        mAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        mRetrieveTracksRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager mRetrieveTracksLayoutManager = new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false);
        mRetrieveTracksRecyclerView.setLayoutManager(mRetrieveTracksLayoutManager);
        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mRetrieveTracksLayoutManager, isWordpress() ? 1 : 0) {
            @Override
            public void onLoadMore(final int current_page) {
                mRetrieveTracksRecyclerView.post(new Runnable() {
                    public void run() {
                        loadTracks(current_page);
                    }
                });
            }
        };
        mRetrieveTracksRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    private boolean isWordpress(){
        final String provider = getArguments().getString(MainActivity.FRAGMENT_PROVIDER);
        return provider.equals(Provider.WORDPRESS_AUDIO);
    }

    private boolean isPlaylist(){
        final String[] arguments = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
        boolean isPlaylist = false;
        if (arguments.length > 1
                && arguments[1].equals("playlist")) {
            isPlaylist = true;
        }
        return isPlaylist;
    }

    private void initPlaylistTracksRecyclerView() {
        mPlaylistTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(TrackObject track) {
                mCheerleaderPlayer.play(track);
            }

            @Override
            public void onMoreClicked(TrackObject track, View source) {
                showTrackActionsPopup(track, source);
            }
        };

        mPlaybackView = new PlaybackView(mAct);
        mPlaybackView.setListener(this);

        mPlaylistTracks = new ArrayList<>();
        mPlaylistAdapter = new TracksAdapter(getContext(), mPlaylistTracksListener, mPlaylistTracks);
        mPlaylistAdapter.setHeaderView(mPlaybackView);

        mPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false));

        //Swipe dismiss listner
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        if (viewHolder.getAdapterPosition() == 0) return 0;
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean onMove(
                            final RecyclerView recyclerView,
                            final RecyclerView.ViewHolder viewHolder,
                            final RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            final RecyclerView.ViewHolder viewHolder,
                            final int swipeDir) {
                        //Get the track based on the position (minus the header)
                        TrackObject track = mCheerleaderPlayer.getTracks().get(viewHolder.getAdapterPosition() - 1);
                        if (mCheerleaderPlayer.getTracks().contains(track)) {
                            mCheerleaderPlayer.removeTrack(mPlaylistTracks.indexOf(track));
                        }
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            // Get RecyclerView item from the ViewHolder
                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            if (dX < 0) {
                                p.setColor(ContextCompat.getColor(mAct, R.color.grey));
                                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), p);
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                simpleItemTouchCallback
        );
        itemTouchHelper.attachToRecyclerView(mPlaylistRecyclerView);
    }


    @Override
    public boolean handleBackPress() {
        //If the playlist view is expanded, make sure a backpress closes it
        if (mPlaybackView.getTop() < mPlaylistRecyclerView.getHeight() - mPlaybackView.getHeight() &&
                mPlaylistTracks.size() > 0) { //There should be at least one track in the playlistview for it to be visible at all
            mPlaylistRecyclerView.getLayoutManager().smoothScrollToPosition(mPlaylistRecyclerView, null, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }

    @Override
    public boolean dynamicToolbarElevation() {
        return false;
    }
}
