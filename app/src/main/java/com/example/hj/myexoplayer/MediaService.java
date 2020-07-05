package com.example.hj.myexoplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import androidx.annotation.Nullable;

public class MediaService extends Service {
    private String [] misic_list={
            "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/BLACKPINK%20-%20DDU-DU%20DDU-DU%20%28Remix%29.flac?Expires=1593962828&OSSAccessKeyId=TMP.3KdFEzdJc4mVhaprkzZdDgDN9kiG2QMvPsSw5ZXxpZJuHju3LCpE9T8dG3H5F4ETFyNENuQc9pwnteFR2iyEGNFHU9gwVg&Signature=ndUPZpXmMHJG5%2Bg%2B0gT7TqdCFbA%3D","https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/test.mp3?Expires=1593962862&OSSAccessKeyId=TMP.3KdFEzdJc4mVhaprkzZdDgDN9kiG2QMvPsSw5ZXxpZJuHju3LCpE9T8dG3H5F4ETFyNENuQc9pwnteFR2iyEGNFHU9gwVg&Signature=hZBu35OpYzWuQ%2B%2FuEhFuYWubucE%3D"};
    SimpleExoPlayer simpleExoPlayer;
    PlayerNotificationManager playerNotificationManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context=this;
        simpleExoPlayer=new SimpleExoPlayer.Builder(context).build();
        DefaultDataSourceFactory dataSourceFactory=new DefaultDataSourceFactory(context,
                Util.getUserAgent(context,"MyExoplayer"));
        ConcatenatingMediaSource concatenatingMediaSource=new ConcatenatingMediaSource();
        for(String str:misic_list){
            MediaSource mediaSource=new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(str));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        simpleExoPlayer.prepare(concatenatingMediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
        playerNotificationManager=PlayerNotificationManager.createWithNotificationChannel(context,
                "100", R.string.app_name, R.string.app_name, 100,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public CharSequence getCurrentContentTitle(Player player) {
                        return "测试音乐";
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent = new Intent(context, MainActivity.class);

                        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    @Nullable
                    @Override
                    public CharSequence getCurrentContentText(Player player) {
                        return "这是测试音乐";
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return BitmapFactory.decodeFile("");
                    }
                }, new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        stopSelf();
                    }
                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                        startForeground(notificationId,notification);
                    }
                });
        playerNotificationManager.setPlayer(simpleExoPlayer);
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState==Player.STATE_ENDED){
                    int position=simpleExoPlayer.getCurrentWindowIndex();
                    if(position==1){
                        simpleExoPlayer.previous();

                    }else{
                        simpleExoPlayer.next();
                    }
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        playerNotificationManager.setPlayer(null);
        simpleExoPlayer.release();
        simpleExoPlayer=null;
        super.onDestroy();
    }
}
