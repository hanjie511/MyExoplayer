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

public class MediaService extends MediaBrowserServiceCompat {

    private List<MediaMetadataCompat> misic_list = Samples.getPlayList();
    SimpleExoPlayer simpleExoPlayer;
    PlayerNotificationManager playerNotificationManager;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat playbackState;
    private int index=0;
    private Context context;
    private MediaNotificationManager mediaNotificationManager;
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("123", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaSession=new MediaSessionCompat(this,"MediaService");
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
                .build();
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackState);
        try {
            mediaNotificationManager=new MediaNotificationManager(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mediaSession.getSessionToken());

    }
    private void playMusic(final int index, final Context context){
        if(simpleExoPlayer!=null){
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
        simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "MyExoplayer"));
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (MediaMetadataCompat mc : misic_list) {
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
//        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context,
//                "100", R.string.app_name, R.string.app_name, 100,
//                new PlayerNotificationManager.MediaDescriptionAdapter() {
//
//                    @Override
//                    public CharSequence getCurrentContentTitle(Player player) {
//                        return misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_TITLE);
//                    }
//
//                    @Nullable
//                    @Override
//                    public PendingIntent createCurrentContentIntent(Player player) {
//                        Intent intent = new Intent(context, MainActivity.class);
//
//                        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    }
//
//                    @Nullable
//                    @Override
//                    public CharSequence getCurrentContentText(Player player) {
//                        return misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
//                    }
//
//                    @Nullable
//                    @Override
//                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
//                        return Samples.netPicToBmp(misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI));
//                    }
//
//                }, new PlayerNotificationManager.NotificationListener() {
//                    @Override
//                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
//                        stopSelf();
//                    }
//
//                    @Override
//                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
//                        startForeground(notificationId, notification);
//                    }
//
//                });
//        playerNotificationManager.setPlayer(simpleExoPlayer);

        simpleExoPlayer.addListener(new ExoplayerEvnetListener());
     //   simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        simpleExoPlayer.prepare(concatenatingMediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
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
                playMusic(index,context);
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
//            if (simpleExoPlayer != null) {
//                simpleExoPlayer.next();
//            }
            index++;
            if(index>=3){
                index=0;
            }
            playMusic(index,context);
        }

        @Override
        public void onSkipToPrevious() {
//            if (simpleExoPlayer != null) {
//                simpleExoPlayer.previous();
//            }
            index--;
            if(index<=0){
                index=0;
            }
            playMusic(index,context);
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
                    updatePlaybackState(null);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    updatePlaybackState(null);
                    break;
                case ExoPlayer.STATE_READY:
                    updatePlaybackState(null);
                    break;
                case ExoPlayer.STATE_ENDED:
                    index++;
                    if(index>=3){
                        index=0;
                    }
                    playMusic(index,context);
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
    public void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        position = simpleExoPlayer.getCurrentPosition();
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        int state = getState();
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) //id
                .putString(MediaMetadata.METADATA_KEY_TITLE, misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_TITLE))//标题
                .putString(MediaMetadata.METADATA_KEY_ARTIST,misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_ARTIST))//作者
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,misic_list.get(index).getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))//背景图片
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
