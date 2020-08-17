package com.example.hj.myexoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NavigationActivity extends AppCompatActivity implements View.OnClickListener {
    Button playVideoBtn;
    Button playMusicBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        initView();
    }

    private void  initView(){
        playVideoBtn=findViewById(R.id.playVideoBtn);
        playMusicBtn=findViewById(R.id.playMusicBtn);
        playMusicBtn.setOnClickListener(this);
        playVideoBtn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.playVideoBtn:
                Intent intent=new Intent(NavigationActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.playMusicBtn:
                Intent intent1=new Intent(NavigationActivity.this,MusicActivity.class);
                startActivity(intent1);
                break;
        }
    }
}