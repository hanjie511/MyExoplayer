package com.example.hj.myexoplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class Samples {
    public static Bitmap bitmap=null;
    public static List<MediaMetadataCompat> getPlayList() {
        ArrayList<MediaMetadataCompat> list = new ArrayList<>();
        MediaMetadataCompat.Builder mc1 = new MediaMetadataCompat.Builder();
        mc1.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "1");
        mc1.putString(MediaMetadata.METADATA_KEY_TITLE, "How You Like That");
        mc1.putString(MediaMetadata.METADATA_KEY_ARTIST, "BLACKPINK");
        mc1.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/BLACKPINK%20-%20How%20You%20Like%20That.flac");
        mc1.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_blackpink.jpg");

        MediaMetadataCompat.Builder mc2 = new MediaMetadataCompat.Builder();
        mc2.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "2");
        mc2.putString(MediaMetadata.METADATA_KEY_TITLE, "Secret Story of the Swan");
        mc2.putString(MediaMetadata.METADATA_KEY_ARTIST, "IZ#ONE");
        mc2.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/IZ%23ONE%20-%20%ED%99%98%EC%83%81%EB%8F%99%ED%99%94%20%28Secret%20Story%20of%20the%20Swan%29%20%28%E5%B9%BB%E6%83%B3%E7%AB%A5%E8%AF%9D%29.flac");
        mc2.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_izone.jpg");

        MediaMetadataCompat.Builder mc3 = new MediaMetadataCompat.Builder();
        mc3.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "3");
        mc3.putString(MediaMetadata.METADATA_KEY_TITLE, "莫问归期");
        mc3.putString(MediaMetadata.METADATA_KEY_ARTIST, "蒋雪儿");
        mc3.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/%E8%92%8B%E9%9B%AA%E5%84%BF%20-%20%E8%8E%AB%E9%97%AE%E5%BD%92%E6%9C%9F.flac");
        mc3.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, "https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_jxer.jpg");
        list.add(mc1.build());
        list.add(mc2.build());
        list.add(mc3.build());
        return list;
    }

    public static Bitmap netPicToBmp(final String src) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    //设置固定大小
                    //需要的大小
                    float newWidth = 200f;
                    float newHeigth = 200f;

                    //图片大小
                    int width = myBitmap.getWidth();
                    int height = myBitmap.getHeight();

                    //缩放比例
                    float scaleWidth = newWidth / width;
                    float scaleHeigth = newHeigth / height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeigth);
                    bitmap = Bitmap.createBitmap(myBitmap, 0, 0, width, height, matrix, true);
                    System.out.println("bitmap:---------"+bitmap);
                } catch (IOException e) {
                    // Log exception
                    bitmap =  null;
                }
            }
        }).start();
        return bitmap;
    }
}