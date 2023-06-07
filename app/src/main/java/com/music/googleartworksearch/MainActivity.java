package com.music.googleartworksearch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImagesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvResult = findViewById(R.id.rv_result);
        rvResult.setLayoutManager(new GridLayoutManager(this, 3));
         adapter = new ImagesAdapter();
        rvResult.setAdapter(adapter);
        rvResult.addItemDecoration(new EqualSpaceItemDecoration(10));
        
        EditText edKeyword = findViewById(R.id.ed_keyword);
        findViewById(R.id.bt_search).setOnClickListener(v -> {
            FindArtWorkByGoogle.find(this, edKeyword.getText().toString().trim(), images -> {
                runOnUiThread(() -> displayImages(images));
                return null;
            });
        });
    }

    private void displayImages(List<String> images) {
        if (images.isEmpty()) {
            Toast.makeText(this, "No images", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i("superx", "#######################################################");
        for (String image : images) {
            Log.i("superx", "displayImages at(MainActivity.java:44): " + image);
        }
        adapter.setData(images);
    }
}
