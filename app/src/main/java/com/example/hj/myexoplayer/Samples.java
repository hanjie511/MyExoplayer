package com.example.hj.myexoplayer;

import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.List;

public class Samples {
    public static List<MediaMetadataCompat> getPlayList(){
        ArrayList<MediaMetadataCompat> list=new ArrayList<>();
        MediaMetadataCompat.Builder mc1=new MediaMetadataCompat.Builder();
        mc1.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "1");
        mc1.putString(MediaMetadata.METADATA_KEY_TITLE, "How You Like That");
        mc1.putString(MediaMetadata.METADATA_KEY_ARTIST,"BLACKPINK");
        mc1.putString(MediaMetadata.METADATA_KEY_MEDIA_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/BLACKPINK%20-%20How%20You%20Like%20That.flac");
        mc1.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_blackpink.jpg");

        MediaMetadataCompat.Builder mc2=new MediaMetadataCompat.Builder();
        mc2.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "2");
        mc2.putString(MediaMetadata.METADATA_KEY_TITLE, "Secret Story of the Swan");
        mc2.putString(MediaMetadata.METADATA_KEY_ARTIST,"IZ#ONE");
        mc2.putString(MediaMetadata.METADATA_KEY_MEDIA_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/IZ%23ONE%20-%20%ED%99%98%EC%83%81%EB%8F%99%ED%99%94%20%28Secret%20Story%20of%20the%20Swan%29%20%28%E5%B9%BB%E6%83%B3%E7%AB%A5%E8%AF%9D%29.flac");
        mc2.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_izone.jpg");

        MediaMetadataCompat.Builder mc3=new MediaMetadataCompat.Builder();
        mc3.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "3");
        mc3.putString(MediaMetadata.METADATA_KEY_TITLE, "莫问归期");
        mc3.putString(MediaMetadata.METADATA_KEY_ARTIST,"蒋雪儿");
        mc3.putString(MediaMetadata.METADATA_KEY_MEDIA_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/%E8%92%8B%E9%9B%AA%E5%84%BF%20-%20%E8%8E%AB%E9%97%AE%E5%BD%92%E6%9C%9F.flac");
        mc3.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,"https://hanjie-oos.oss-cn-shenzhen.aliyuncs.com/upload/bg_img_jxer.jpg");
        list.add(mc1.build());
        list.add(mc2.build());
        list.add(mc3.build());
        return list;
    }
}
