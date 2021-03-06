/*************************************************************************
 *
 * ATOS CONFIDENTIAL
 * __________________
 *
 *  Copyright (2020) Atos Spain SA
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Atos Spain SA and other companies of the Atos group.
 * The intellectual and technical concepts contained
 * herein are proprietary to Atos Spain SA
 * and other companies of the Atos group and may be covered by Spanish regulations
 * and are protected by copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Atos Spain SA.
 */
package eu.h2020.helios_social.modules.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_MESSAGE = "eu.h2020.helios_social.mudules.videoplayer.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "eu.h2020.helios_social.mudules.videoplayer.MESSAGE";
    private PlaybackStateListener playbackStateListener;
    private static final String TAG = VideoPlayerActivity.class.getName();

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private Uri url;
    private String extension;
    private boolean isShowedSystemUi;

    private Intent intent;

    private static final int MY_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        intent = getIntent();

        if (intent.getAction() != null) {
            url = intent.getData();
        } else {
            try{
                String uri = intent.getStringExtra("URI");
                url = Uri.parse(uri);
            }catch (Exception ex){
                Log.i("VideoPlayer", "Error Parsing Intent URI: " + ex.getMessage());
            }
        }

        if(url == null){
            url = Uri.parse(getString(R.string.hls_uri));

        }

        //playerView = findViewById(R.id.video_view);
        playerView = findViewById(R.id.video_view);

        playbackStateListener = new PlaybackStateListener();
    }

    //Create an Exoplayer instance:
    private void initializePlayer() {

        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        }

        playerView.setPlayer(player);
        playerView.setOnClickListener(this);

        Uri uri = Uri.parse(String.valueOf(url));
        String uri_string = uri.toString();
        if(uri_string.contains(".")) {
            extension = uri_string.substring(uri_string.lastIndexOf(".") + 1);
        } else {
            extension = "other";
        }
        MediaSource mediaSource = buildMediaSource(uri, extension);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.addListener(playbackStateListener);
        player.prepare(mediaSource, false, false);
    }

    //Create a media source:
    private MediaSource buildMediaSource(Uri uri, String extension) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");

        @C.ContentType int type = Util.inferContentType(uri, extension);

        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if (player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        isShowedSystemUi = false;
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        isShowedSystemUi = true;
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (isShowedSystemUi) {
            hideSystemUi();
        } else {
            showSystemUI();
        }
    }

    private class PlaybackStateListener implements Player.EventListener{

        @Override
        public void onPlayerStateChanged(boolean playWhenReady,
                                         int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    Intent result = new Intent("com.helios.RESULT_ACTION");
                    setResult(VideoPlayerActivity.RESULT_OK, result);
                    finish();
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK) {
            playbackPosition = data.getLongExtra(VideoPlayer360Activity.BUNDLE_POSITION_PLAYBACK, 0);
        }
    }

    public void changeTo360(View view) {
        Intent intent = new Intent(this, VideoPlayer360Activity.class);
        intent.putExtra(EXTRA_MESSAGE, url.toString());
        intent.putExtra("POSITION", player.getCurrentPosition());
        startActivityForResult(intent, MY_REQUEST_CODE);
    }
}