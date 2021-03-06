package com.example.fd.wifisignal;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import ratel.library.storylibrary.StoryLibrary;
import ratel.library.storylibrary.StoryProfile;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        setContentView(R.layout.activity_main);
        MainPage fragment = new MainPage();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        StoryLibrary s = new StoryLibrary(R.id.container, this, new ArrayList<StoryProfile>());
        s.start(new StoryLibrary.StoryLibraryReadyListener() {
            @Override
            public void onReady() {
                s.play();
            }
        });
    }

}

