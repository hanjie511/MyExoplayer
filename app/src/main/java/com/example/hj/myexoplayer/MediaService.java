package com.example.hj.myexoplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

public class MediaService extends MediaBrowserServiceCompat {

    private List<MediaMetadataCompat> misic_list = Samples.getPlayList();
    SimpleExoPlayer simpleExoPlayer;
    PlayerNotificationManager playerNotificationManager;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat playbackState;
    private Context context;
    private MediaNotificationManager mediaNotificationManager;
    private QueueManager queueManager;
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("123", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaSession=new MediaSessionCompat(this,"MediaService");
        queueManager=new QueueManager();
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
                .build();
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackState);
        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mediaSession.getSessionToken());
        try {
            mediaNotificationManager=new MediaNotificationManager(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功

    }
    private void playMusic(MediaMetadataCompat mediaMetadataCompat, final Context context){
        if(simpleExoPlayer!=null){
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
        simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "MyExoplayer"));
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        simpleExoPlayer.addListener(new ExoplayerEvnetListener());
        simpleExoPlayer.prepare(concatenatingMediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
        mediaNotificationManager.startNotification();
    }
    private MediaSessionCompat.Callback mediaSessionCallback=new MediaSessionCompat.Callback() {
        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPlay() {
            if (simpleExoPlayer != null) {
                simpleExoPlayer.setPlayWhenReady(true);
            }else{
                playMusic(queueManager.getCurrentMediaMetadata(),context);
            }
        }

        @Override
        public void onPause() {
            if (simpleExoPlayer != null) {
                simpleExoPlayer.setPlayWhenReady(false);
            }
        }

        @Override
        public void onSkipToNext() {
            playMusic(queueManager.getNextMediaMetadata(),context);
        }

        @Override
        public void onSkipToPrevious() {
            playMusic(queueManager.getPreviousMediaMetadata(),context);
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSeekTo(long pos) {
            if (simpleExoPlayer != null) {
                simpleExoPlayer.seekTo(pos);
            }
        }
    };
    private class ExoplayerEvnetListener implements ExoPlayer.EventListener{
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                case ExoPlayer.STATE_BUFFERING:
                case ExoPlayer.STATE_READY:
                    try {
                        updatePlaybackState(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ExoPlayer.STATE_ENDED:
                    playMusic(queueManager.getNextMediaMetadata(),context);
                   break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

    }
    public void updatePlaybackState(String error) throws Exception {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        position = simpleExoPlayer.getCurrentPosition();
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        int state = getState();
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,queueManager.getCurrentMediaMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) //id
                .putString(MediaMetadata.METADATA_KEY_TITLE,queueManager.getCurrentMediaMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE))//标题
                .putString(MediaMetadata.METADATA_KEY_ARTIST,queueManager.getCurrentMediaMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST))//作者
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,queueManager.getCurrentMediaMetadata().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))//背景图片
                .putLong(MediaMetadata.METADATA_KEY_DURATION,simpleExoPlayer.getDuration())//媒体时长
                .build();
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setMetadata(metadata);
        mediaNotificationManager.startNotification();
     //   mediaSession.setActive(true);
    }
    public int getState() {
        switch (simpleExoPlayer.getPlaybackState()) {
            case ExoPlayer.STATE_IDLE:
                return PlaybackStateCompat.STATE_PAUSED;
            case ExoPlayer.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;//缓冲
            case ExoPlayer.STATE_READY:
                return simpleExoPlayer.getPlayWhenReady()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED;
            case ExoPlayer.STATE_ENDED:
                return PlaybackStateCompat.STATE_PAUSED;
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }
    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (simpleExoPlayer.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }
    @Override
    public void onDestroy() {
        mediaNotificationManager.stopNotification();
        if(playerNotificationManager!=null){
            playerNotificationManager.setPlayer(null);
        }
        if(simpleExoPlayer!=null){
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
        super.onDestroy();
    }
}
