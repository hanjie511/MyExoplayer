package com.example.hj.myexoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MediaNotificationManager extends BroadcastReceiver {
    private final static String ACTION_PLAY="CMD_PLAY";
    private final static String ACTION_PAUSE="CMD_PAUSE";
    private final static String ACTION_NEXT="CMD_NEXT";
    private final static String ACTION_PRE="CMD_PRE";
    private final static String ACTION_LIKE="CMD_LIKE";
    private final static String ACTION_UNLIKE="CMD_UNLIKE";
    private static final String CHANNEL_ID = "MyExoplayerChannelID";
    private static final int REQUEST_CODE=100;
    private static int NOTIFICATION_ID=666;
    private  final MediaService mediaService;
    private MediaMetadataCompat mediaMetadataCompat;
    private final NotificationManager notificationManager;
    private MediaSessionCompat.Token meToken;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCompat.TransportControls transportControls;
    private PlaybackStateCompat mPlaybackState;
    private final PendingIntent playIntent;
    private final PendingIntent pauseIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent preIntent;
    private  final PendingIntent likeIntent;
    private final PendingIntent unlikeIntent;
    private boolean isStarted=false;
    public MediaNotificationManager(MediaService mediaService) throws Exception{
        this.mediaService = mediaService;
        String pkg=mediaService.getPackageName();
        notificationManager= (NotificationManager) mediaService.getSystemService(Context.NOTIFICATION_SERVICE);
        updateSessionToken();
        playIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        pauseIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        nextIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        preIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_PRE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        likeIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_LIKE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        unlikeIntent=PendingIntent.getBroadcast(mediaService,REQUEST_CODE,
                new Intent(ACTION_UNLIKE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        notificationManager.cancelAll();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action=intent.getAction();
        switch (action){
            case ACTION_NEXT:
                transportControls.skipToNext();
                break;
            case ACTION_PAUSE:
                transportControls.pause();
                break;
            case ACTION_PLAY:
                transportControls.play();
                break;
            case ACTION_PRE:
                transportControls.skipToPrevious();
                break;
        }
    }
    private final MediaControllerCompat.Callback callback=new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mPlaybackState=state;
            if(state.getState()==PlaybackStateCompat.STATE_STOPPED||
                    state.getState()==PlaybackStateCompat.STATE_NONE){
                stopNotification();
            }else{
                Notification notification=createNotification();
                if(notification!=null){
                    notificationManager.notify(NOTIFICATION_ID,notification);
                }
            }
        }


        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mediaMetadataCompat=metadata;
            Notification notification=createNotification();
            if(notification!=null){
                notificationManager.notify(NOTIFICATION_ID,notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private Notification createNotification(){
        if(mediaMetadataCompat==null||mPlaybackState==null){
            return null;
        }
        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        NotificationCompat.Builder notificationBuilder=new
                NotificationCompat.Builder(mediaService,CHANNEL_ID);
        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat
                .MediaStyle().setMediaSession(meToken)
                .setShowCancelButton(true).setShowActionsInCompactView(addActions(notificationBuilder)));

        notificationBuilder.setColor(Color.WHITE);
        notificationBuilder.setSmallIcon(R.drawable.ic_toolbar_24);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setContentText(mediaMetadataCompat.getString(MediaMetadata.METADATA_KEY_ARTIST));
        notificationBuilder.setContentTitle(mediaMetadataCompat.getString(MediaMetadata.METADATA_KEY_TITLE));
        notificationBuilder.setLargeIcon(Samples.netPicToBmp(mediaMetadataCompat.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)));
        setNotificationPlaybackState(notificationBuilder);
        return notificationBuilder.build();
    }
    public void startNotification(){
        if (!isStarted) {
            mediaMetadataCompat = mediaControllerCompat.getMetadata();
            mPlaybackState = mediaControllerCompat.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mediaControllerCompat.registerCallback(callback);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PRE);
                filter.addAction(ACTION_LIKE);
                filter.addAction(ACTION_UNLIKE);
                mediaService.registerReceiver(this, filter);
                mediaService.startForeground(NOTIFICATION_ID, notification);
                isStarted = true;
            }
        }
    }
    public void stopNotification(){
        if (isStarted) {
            isStarted = false;
            mediaControllerCompat.unregisterCallback(callback);
            try {
                notificationManager.cancel(NOTIFICATION_ID);
                mediaService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mediaService.stopForeground(true);
        }
    }
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mediaService.getSessionToken();
        if (meToken == null && freshToken != null ||
                meToken != null && !meToken.equals(freshToken)) {
            if (mediaControllerCompat != null) {
                mediaControllerCompat.unregisterCallback(callback);
            }
            meToken = freshToken;
            if (meToken != null) {
                mediaControllerCompat = new MediaControllerCompat(mediaService, meToken);
                transportControls = mediaControllerCompat.getTransportControls();
                if (!isStarted) {
                    mediaControllerCompat.registerCallback(callback);
                }
            }
        }
    }
    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mPlaybackState == null || !isStarted) {
            mediaService.stopForeground(true);
            return;
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * According PlaybackState to add media Button on Notification
     * @param builder
     * @return
     */
    private int[] addActions(NotificationCompat.Builder builder){
        builder.addAction(R.drawable.ic_outline_skip_previous_24,
                "上一首",preIntent);
        if(mPlaybackState.getState()==PlaybackStateCompat.STATE_PLAYING){
            builder.addAction(R.drawable.ic_outline_pause_circle_outline_24,
                    "暂停",pauseIntent);
        }else{
            builder.addAction(R.drawable.ic_outline_play_circle_outline_24,
                    "播放",playIntent);
        }
        builder.addAction(R.drawable.ic_outline_skip_next_24,
                "下一首",preIntent);
        builder.addAction(R.drawable.ic_baseline_unfavorite_border_24,
                "喜欢",unlikeIntent);
        return new int[]{0,1,2,3};
    }
    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            CHANNEL_ID,
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    CHANNEL_ID);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
