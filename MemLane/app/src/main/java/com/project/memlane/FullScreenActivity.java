package com.project.memlane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class FullScreenActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private FullScreenImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        // Sets full size image of the picture. Also you can slide pictures
        viewPager = findViewById(R.id.pager);
        ArrayList<String> uris = getIntent().getStringArrayListExtra("links");
        adapter = new FullScreenImageAdapter(this, uris);
        // Set full size images of slide-able images
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int pos = getIntent().getIntExtra("position", 0);
        viewPager.setCurrentItem(pos);
    }
}
