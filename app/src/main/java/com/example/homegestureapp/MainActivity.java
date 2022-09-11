package com.example.homegestureapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] gestureIDList;
    private String[] gestureNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init spinner dropdown list
        gestureIDList = getResources().getStringArray(R.array.gestureIDList);
        gestureNameList = getResources().getStringArray(R.array.gestureNameList);
        Spinner gestureList = findViewById(R.id.gestureList);
        gestureList.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> gestureListAdapter = ArrayAdapter.createFromResource(this, R.array.gestureValueList, android.R.layout.simple_spinner_item);
        gestureListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gestureList.setAdapter(gestureListAdapter);

        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.0.208:5000/";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "unable to connect", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = Objects.requireNonNull(response.body()).string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "connected!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String gestureSelected = parent.getItemAtPosition(position).toString();
        if (!gestureSelected.equals("Select a Gesture…")) {
            Intent previewGestureActivityIntent = new Intent(MainActivity.this, PreviewGestureActivity.class);
            previewGestureActivityIntent.putExtra("gestureSelected", gestureSelected);
            previewGestureActivityIntent.putExtra("gestureSelectedID", gestureIDList[position]);
            previewGestureActivityIntent.putExtra("gestureSelectedName", gestureNameList[position]);
            startActivity(previewGestureActivityIntent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}