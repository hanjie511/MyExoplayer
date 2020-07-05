package com.example.hj.myexoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {
    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;
    private String videoPath="https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/testVideo.mp4?Expires=1593932418&OSSAccessKeyId=TMP.3KdFEzdJc4mVhaprkzZdDgDN9kiG2QMvPsSw5ZXxpZJuHju3LCpE9T8dG3H5F4ETFyNENuQc9pwnteFR2iyEGNFHU9gwVg&Signature=Yiz9zWF0p%2FSUWdZlhH%2B5YsCBnRw%3D";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView=findViewById(R.id.playerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        simpleExoPlayer=new SimpleExoPlayer.Builder(MainActivity.this).build();
        // Bind the player to the view.
        playerView.setPlayer(simpleExoPlayer);
        // Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(MainActivity.this,
                Util.getUserAgent(MainActivity.this, "MyExoplayer"));
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(videoPath));
        // Prepare the player with the source.
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerView.setPlayer(null);
        simpleExoPlayer.release();
        simpleExoPlayer=null;
    }
}



