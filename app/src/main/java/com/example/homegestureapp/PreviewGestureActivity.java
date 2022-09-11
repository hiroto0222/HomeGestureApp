package com.example.homegestureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.VideoView;

public class PreviewGestureActivity extends AppCompatActivity {

    private String gestureSelected;
    private String gestureSelectedID;
    private String gestureSelectedName;
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
        gestureSelectedName = intent.getStringExtra("gestureSelectedName");

        gestureTextView.setText("Gesture: " + gestureSelected);

        findViewById(R.id.replayBtn).setOnClickListener(v -> handleReplay());
        findViewById(R.id.practiceBtn).setOnClickListener(v -> handlePractice());
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

    public void handleReplay() {
        initPlayer();
    }

    public void handlePractice() {
        Intent practiceGestureActivityIntent = new Intent(PreviewGestureActivity.this, PracticeGestureActivity.class);
        practiceGestureActivityIntent.putExtra("gestureSelected", gestureSelected);
        practiceGestureActivityIntent.putExtra("gestureSelectedID", gestureSelectedID);
        practiceGestureActivityIntent.putExtra("gestureSelectedName", gestureSelectedName);
        startActivity(practiceGestureActivityIntent);
    }
}