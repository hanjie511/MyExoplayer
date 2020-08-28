package com.example.hj.myexoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final Handler mHandler = new Handler();
    private ScheduledFuture<?> mScheduleFuture;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private MediaBrowserCompat mediaBrowser;
    private Button play_stop_btn;
    private Button previous_btn;
    private Button next_btn;
    private TextView nowTime;
    private TextView musicName;
    private TextView totalTime;
    private SeekBar progressBar;
    private Button stop_btn;
    private ImageView imageView_musicActivity;
    private PlaybackStateCompat mLastPlaybackState;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
          //  Log.i("message-----------:","PlaybackStateCompat发生改变");
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
          //  Log.i("message-----------:","MediaMetadataCompat发生改变");
            if (metadata != null) {
                updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
            }
        }
    };

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    System.out.println("mConnectionCallback:"+"开始连接");
                    if(mediaBrowser.isConnected()){
                        String mediaId=mediaBrowser.getRoot();
                        System.out.println("-----------------------------mediaId:"+mediaId);
                        try {
                            connectToSession(mediaBrowser.getSessionToken());
                        } catch (RemoteException e) {
                            System.out.println("mConnectionCallback:"+"连接失败");
                        }
                    }
                }

                @Override
                public void onConnectionFailed() {
                    Log.i("mConnectionCallback", "连接失败");
                    System.out.println("mConnectionCallback:"+"连接失败");
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initView();
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mediaBrowser!=null){
            mediaBrowser.connect();
        }
    }

    private void initView(){
        play_stop_btn=findViewById(R.id.palyBtn);
        play_stop_btn.setOnClickListener(this);
        previous_btn=findViewById(R.id.previouBtn);
        previous_btn.setOnClickListener(this);
        stop_btn=findViewById(R.id.pauseBtn);
        stop_btn.setOnClickListener(this);
        next_btn=findViewById(R.id.nextBtn);
        next_btn.setOnClickListener(this);
        nowTime=findViewById(R.id.startTime);
        musicName=findViewById(R.id.musicName);
        totalTime=findViewById(R.id.endTime);
        progressBar=findViewById(R.id.seekBar);
        imageView_musicActivity=findViewById(R.id.imageView_musicActivity);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaController.getTransportControls().seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.palyBtn:
                stop_btn.setVisibility(View.VISIBLE);
                play_stop_btn.setVisibility(View.GONE);
                mediaController.getTransportControls().play();
                break;
            case R.id.previouBtn:
                mediaController.getTransportControls().skipToPrevious();
                break;
            case R.id.nextBtn:
                mediaController.getTransportControls().skipToNext();
                break;
            case R.id.pauseBtn:
                stop_btn.setVisibility(View.GONE);
                play_stop_btn.setVisibility(View.VISIBLE);
                mediaController.getTransportControls().pause();
                break;
        }
    }
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        mediaController = new MediaControllerCompat(
                MusicActivity.this, token);
//        if (mediaController.getMetadata() == null) {
//            finish();
//            return;
//        }
        MediaControllerCompat.setMediaController(MusicActivity.this, mediaController);
        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }
    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                stop_btn.setVisibility(VISIBLE);
                play_stop_btn.setVisibility(INVISIBLE);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                stop_btn.setVisibility(INVISIBLE);
                play_stop_btn.setVisibility(VISIBLE);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                stop_btn.setVisibility(INVISIBLE);
                play_stop_btn.setVisibility(VISIBLE);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                stop_btn.setVisibility(INVISIBLE);
                play_stop_btn.setVisibility(VISIBLE);
                stopSeekbarUpdate();
                break;
            default:
        }
    }
    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        progressBar.setMax(duration);
        totalTime.setText(DateUtils.formatElapsedTime(duration/1000));
        Glide.with(MusicActivity.this).load(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))).into(imageView_musicActivity);
    }
    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        progressBar.setProgress((int) currentPosition);
        nowTime.setText(""+DateUtils.formatElapsedTime(currentPosition/1000));
    }
    private void updateMediaDescription(MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
        musicName.setText(description.getTitle());
    }
    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(MusicActivity.this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mCallback);
        }
    }
}