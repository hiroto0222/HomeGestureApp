package com.example.homegestureapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PracticeGestureActivity extends AppCompatActivity implements View.OnClickListener {

    PreviewView previewView;
    private VideoCapture videoCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button recordBtn, uploadBtn;

    private String gestureSelectedName;
    private static String mediaFileName = null;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_gesture);

        // check camera permissions
        checkPermissions(this);

        recordBtn = findViewById(R.id.recordBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        previewView = findViewById(R.id.previewView);

        recordBtn.setOnClickListener(this);
        uploadBtn.setOnClickListener(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        Intent intent = getIntent();
        gestureSelectedName = intent.getStringExtra("gestureSelectedName");
    }

    public static void checkPermissions(Context context) {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            System.out.println("give permission for camera");
            // Permission is not granted
            Log.d("checkCameraPermissions", "No Camera Permissions");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    100);
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100);
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recordBtn:
                if (!isRecording) {
                    recordBtn.setText(R.string.recording);
                    recordBtn.setEnabled(false);
                    recordVideo();
                    isRecording = true;
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            videoCapture.stopRecording();
                        }
                    }, 5000);
                }
                break;

            case R.id.uploadBtn:
                if (!isRecording && !(mediaFileName == null)) {
                    postRequest();
                }
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {
            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            mediaFileName = gestureSelectedName + "_PRACTICE_" + timeStamp + "_AOYAMA";
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, mediaFileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                checkPermissions(this);
                recordBtn.setText(R.string.recordBtn);
                recordBtn.setEnabled(true);
                mediaFileName = null;
                isRecording = false;
                return;
            }
            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                    ).build(),
                    getExecutor(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            isRecording = false;
                            recordBtn.setText(R.string.recordBtn);
                            recordBtn.setEnabled(true);
                            Toast.makeText(PracticeGestureActivity.this, "Video has been saved!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Toast.makeText(PracticeGestureActivity.this, "Error saving video: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    public void postRequest() {
        RequestBody reqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", mediaFileName + ".mp4", RequestBody.Companion.create(new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_MOVIES + "/" + mediaFileName + ".mp4"), MediaType.parse("video/mp4")))
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        String url = "http://192.168.0.208:5000/upload";

        Request req = new Request.Builder()
                .url(url)
                .post(reqBody)
                .build();

        okHttpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                call.cancel();
                runOnUiThread(() -> {
                    Toast.makeText(PracticeGestureActivity.this, "Something went wrong:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        String response_body = Objects.requireNonNull(response.body()).string();
                        System.out.println(response_body);
                        Toast.makeText(PracticeGestureActivity.this, response_body, Toast.LENGTH_LONG).show();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mediaFileName = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}