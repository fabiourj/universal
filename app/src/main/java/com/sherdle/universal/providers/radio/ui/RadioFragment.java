package com.sherdle.universal.providers.radio.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sherdle.universal.Config;
import com.sherdle.universal.MainActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.inherit.CollapseControllingFragment;
import com.sherdle.universal.inherit.PermissionsFragment;
import com.sherdle.universal.providers.radio.RadioStream;
import com.sherdle.universal.providers.radio.StaticEventDistributor;
import com.sherdle.universal.providers.radio.metadata.Metadata;
import com.sherdle.universal.providers.radio.parser.UrlParser;
import com.sherdle.universal.providers.radio.player.PlaybackStatus;
import com.sherdle.universal.providers.radio.player.RadioManager;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 *  This fragment is used to listen to a radio station
 */
public class RadioFragment extends Fragment implements
        OnClickListener, PermissionsFragment, CollapseControllingFragment, StaticEventDistributor.EventListener {

    private RadioManager radioManager;
    private String[] arguments;
    private RadioStream stream;
    private Activity mAct;

    private Target logoTarget;

    //Layouts
    private AudioVisualization audioVisualization;
    private ImageView albumArtView;
    private ImageView radioLogo;
    private RelativeLayout layout;
    private ProgressBar loadingIndicator;
    private FloatingActionButton buttonPlayPause;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (RelativeLayout) inflater.inflate(R.layout.fragment_radio, container, false);

        initializeUIElements();

        //Get the arguments and 'parse' them
        arguments = RadioFragment.this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);

        if (arguments.length > 1) {
            radioLogo.setVisibility(View.VISIBLE);
            logoTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    radioLogo.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.get().load(arguments[1]).into(logoTarget);
        }

        //Initialize visualizer or imageview for album art
        if (!Config.VISUALIZER_ENABLED){
            albumArtView.setVisibility(View.VISIBLE);
            albumArtView.setImageResource(Config.BACKGROUND_IMAGE_ID);
        }

	    return layout;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		
		Helper.isOnlineShowDialog(mAct);

        //Get the radioManager
        radioManager = RadioManager.with();

        loadingIndicator.setVisibility(View.VISIBLE);
        //Obtain the actual radio url
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                stream = new RadioStream(UrlParser.getUrl(arguments[0]), arguments);
                if (radioLogo.getVisibility() == View.VISIBLE)
                    stream.setLogoBitmap(((BitmapDrawable) radioLogo.getDrawable()).getBitmap());
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingIndicator.setVisibility(View.INVISIBLE);
                        updateButtons();

                        //AutoPlay
                        if (RadioFragment.this.isMenuVisible() && radioManager.isBound() && !radioManager.isPlaying())
                           startStopPlaying();
                    }
                });
            }

        });

        if (isPlaying()){
            onAudioSessionId(RadioManager.getService().getAudioSessionId());
        }

    }

    @Override
    public void onEvent(String status){

        switch (status){
            case PlaybackStatus.LOADING:

                loadingIndicator.setVisibility(View.VISIBLE);

                break;
            case PlaybackStatus.ERROR:
                makeSnackbar(R.string.error_retry);
            case PlaybackStatus.STOPPED:
            case PlaybackStatus.PLAYING:
            case PlaybackStatus.PAUSED:
                loadingIndicator.setVisibility(View.INVISIBLE);

                break;
        }

        updateButtons();
    }

    @Override
    public void onAudioSessionId(Integer i) {
        if (Config.VISUALIZER_ENABLED) {
            try {
                VisualizerDbmHandler vizualizerHandler = DbmHandler.Factory.newVisualizerHandler(getContext(), i);
                audioVisualization.linkTo(vizualizerHandler);
                audioVisualization.onResume();
            } catch (Exception e){
                Log.printStackTrace(e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        StaticEventDistributor.registerAsListener(this);
    }

    @Override
    public void onStop() {
        StaticEventDistributor.unregisterAsListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (!radioManager.isPlaying())
            radioManager.unbind(getContext());

        audioVisualization.release();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        audioVisualization.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateButtons();
        radioManager.bind(getContext());

        if (audioVisualization != null)
            audioVisualization.onResume();
    }


    private void initializeUIElements() {
        loadingIndicator = layout.findViewById(R.id.progressBar);
        loadingIndicator.setMax(100);
        loadingIndicator.setVisibility(View.VISIBLE);

        albumArtView = layout.findViewById(R.id.albumArt);
        audioVisualization = layout.findViewById(R.id.visualizer_view);
        radioLogo = layout.findViewById(R.id.radio_logo);

        buttonPlayPause = layout.findViewById(R.id.btn_play_pause);
        buttonPlayPause.setOnClickListener(this);

        updateButtons();
    }

    public void updateButtons(){
        if (isPlaying() || loadingIndicator.getVisibility() == View.VISIBLE){
            //If another stream is playing, show this in the layout
            if (RadioManager.getService() != null && RadioManager.getService().getStream() != null
                    && stream != null && stream.getUrl() != null
                    && !stream.getUrl().equals(RadioManager.getService().getStream().getUrl())) {
                buttonPlayPause.setImageResource(R.drawable.exomedia_ic_play_arrow_white);
                layout.findViewById(R.id.already_playing_tooltip).setVisibility(View.VISIBLE);
                //If this stream is playing, adjust the buttons accordingly
            } else {
                if (RadioManager.getService() != null && RadioManager.getService().getMetaData() != null){
                    onMetaDataReceived(RadioManager.getService().getMetaData(), null);
                }

                buttonPlayPause.setImageResource(R.drawable.exomedia_ic_pause_white);
                layout.findViewById(R.id.already_playing_tooltip).setVisibility(View.GONE);
            }
        } else {
            //If this stream is paused, adjust the buttons accordingly
            buttonPlayPause.setImageResource(R.drawable.exomedia_ic_play_arrow_white);
            layout.findViewById(R.id.already_playing_tooltip).setVisibility(View.GONE);

            updateMediaInfoFromBackground(null, null);
        }
    }

    @Override
    public void onClick(View v) {
        if (!isPlaying()) {
            if (stream != null) {
                startStopPlaying();

                //Check the sound level
                AudioManager am = (AudioManager) mAct.getSystemService(Context.AUDIO_SERVICE);
                int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volume_level < 2) {
                    makeSnackbar(R.string.volume_low);
                }
            } else {
                //The loading of urlToPlay should happen almost instantly, so this code should never be reached
                makeSnackbar(R.string.error_retry_later);
            }
        } else  {
            startStopPlaying();
        }
    }

    private void startStopPlaying() {
        //Start the radio playing
        radioManager.playOrPause(stream);

        //Update the UI
        updateButtons();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    //@param info - the text to be updated. Giving a null string will hide the info.
    public void updateMediaInfoFromBackground(String info, Bitmap image) {
        TextView nowPlayingTitle = layout.findViewById(R.id.now_playing_title);
        TextView nowPlaying = layout.findViewById(R.id.now_playing);

        if (info != null)
            nowPlaying.setText(info);

        if (info != null && nowPlayingTitle.getVisibility() == View.GONE){
            nowPlayingTitle.setVisibility(View.VISIBLE);
            nowPlaying.setVisibility(View.VISIBLE);
        } else if (info == null){
            nowPlayingTitle.setVisibility(View.GONE);
            nowPlaying.setVisibility(View.GONE);
        }

        if (!Config.VISUALIZER_ENABLED){
            if (image != null) {
                albumArtView.setImageBitmap(image);
            } else {
                albumArtView.setImageResource(Config.BACKGROUND_IMAGE_ID);
            }
        }

    }

    @Override
    public String[] requiredPermissions() {
        if (Config.VISUALIZER_ENABLED)
            return new String[]{Manifest.permission.RECORD_AUDIO};
        else
            return new String[]{};
    }

    @Override
    public void onMetaDataReceived(Metadata meta, Bitmap image) {
        //Update the mediainfo shown above the controls
        String artistAndSong = null;
        if (meta != null &&  meta.getArtist() != null)
            artistAndSong = meta.getArtist() + " - " + meta.getSong();
        updateMediaInfoFromBackground(artistAndSong, image);
    }

    private boolean isPlaying(){
        return (null != radioManager && null != RadioManager.getService() && RadioManager.getService().isPlaying());
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }

    @Override
    public boolean dynamicToolbarElevation() {
        return false;
    }

    private void makeSnackbar(int text){
        Snackbar bar = Snackbar.make(buttonPlayPause, text, Snackbar.LENGTH_SHORT);
        bar.show();
        ((TextView) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).
                setTextColor(getResources().getColor(R.color.white));
    }
}