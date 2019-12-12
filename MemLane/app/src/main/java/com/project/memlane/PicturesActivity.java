package com.project.memlane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.GridView;

import java.util.ArrayList;

public class PicturesActivity extends AppCompatActivity {
    GridView gridView;
    private int columnWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures);

        // Displays Gidview of pictures
        gridView = (GridView) findViewById(R.id.gridview);
        InitilizeGridLayout();
        ArrayList<String> uris = getIntent().getStringArrayListExtra("urls");
        gridView.setAdapter(new ImageAdapter(PicturesActivity.this, uris, columnWidth));
    }

    // Initialize grid layout
    private void InitilizeGridLayout() {
        Resources r = getResources();

        // Padding for each image in the grid
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, r.getDisplayMetrics());

        // Grabs the width of the phone screen and evenyly divide it into 3 for 3 column grid
        columnWidth = (int) ((getScreenWidth() - ((3 + 1) * padding)) / 3);


        gridView.setNumColumns(3);
        gridView.setColumnWidth(columnWidth);
        // Do not stretch to preserved the quality of the image
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    // Grabs the size of the phone screen. Need this to calculate the size of the photo on the
    // 3 column grid
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
}
