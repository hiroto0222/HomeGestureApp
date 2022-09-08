package com.example.homegestureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

public class PreviewGestureActivity extends AppCompatActivity {

    private String gestureSelected;
    private String gestureSelectedID;
    private VideoView gestureVideoView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_gesture);

        TextView gestureTextView = findViewById(R.id.gestureTitle);
        gestureVideoView = findViewById(R.id.gesturePreviewVideo);

        Intent intent = getIntent();
        gestureSelected = intent.getStringExtra("gestureSelected");
        gestureSelectedID = intent.getStringExtra("gestureSelectedID");

        gestureTextView.setText("Gesture: " + gestureSelected);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gestureVideoView.stopPlayback();
    }

    private void initPlayer() {
        gestureVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/raw/" + gestureSelectedID));
        gestureVideoView.start();
    }

    public void handleReplay(View view) {
        initPlayer();
    }

    public void handlePractice(View view) {

    }
}