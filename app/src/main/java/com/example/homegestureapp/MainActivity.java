package com.example.homegestureapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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