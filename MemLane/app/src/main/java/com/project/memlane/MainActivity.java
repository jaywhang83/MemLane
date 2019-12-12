package com.project.memlane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private GridView gridView;
    private int columnWidth;
    private static String fileName = "savedRoutes.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize GridView
        gridView = (GridView) findViewById(R.id.gridviewMain);
        InitilizeGridLayout();
        final MainImageAdapter mainImageAdapter;

        // IF there is a savedRoutes, grab all of the saved routes so it can be displayed on
        // the GridView
        String jsonStr = read(this, fileName);
        HashMap<String, Route> savedLocations = new HashMap<>();
        if (jsonStr != null) {
            savedLocations = getSavedRoutes(jsonStr);
        }


        if (savedLocations != null) {
            ArrayList<String> pictureLinks = new ArrayList<>(savedLocations.keySet());
            //Grabs the first picture in each saved routes and display it on the GridView
            mainImageAdapter = new MainImageAdapter(MainActivity.this, pictureLinks, columnWidth);
            gridView.setAdapter(mainImageAdapter);
            // Grabs the file path of the pictures, locations of the pictures taken, and routes
            // of all of the saved routes
            final HashMap<String, Route> copySavedLocs = savedLocations;
            // When photo on the gridView is clicked, it will open a saved routes map with
            // all of the locations of the pictures taken
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String p = mainImageAdapter.getItem(position).toString();
                    Route clicked = copySavedLocs.get(p);

                    // Opens a saved routes route with routes traced and the locations of the pictures
                    // taken pinned
                    Intent savedRoutesIntent = new Intent(MainActivity.this, SavedRouteActivity.class);
                    savedRoutesIntent.putExtra("savedLocations", clicked);
                    startActivity(savedRoutesIntent);
                }
            });
        }
        bottomNavigationView = findViewById(R.id.bottomAppBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add:
                        // Starts new Routes
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.delete:
                        // TODO: need to implement ability delete saved route
                        break;
                }
                return true;
            }
        });
    }

    // Grabs all of the saved routes fromthe savedRoutes.json file
    private HashMap<String, Route> getSavedRoutes(String jsonStr) {
        try {
            JSONObject savedRoutes = new JSONObject(jsonStr);
            JSONArray savedList = savedRoutes.getJSONArray("savedRoutes");
            HashMap<String, Route> saved = new HashMap<>();
            Gson gson = new Gson();

            for (int i = 0; i < savedList.length(); i++) {

                JSONObject jsonObject = (JSONObject) savedList.get(i);

                // Grabs a PicturesLocations for pinning locations of the picture taken
                // and to display all of the pictures
                String temp = jsonObject.get("PictureLocations").toString();
                // Grabs a routes for the tracing
                String temp2 = jsonObject.get("routes").toString();

                Type mapType =  new TypeToken<HashMap<String, LatLng>>(){}.getType();
                Type arrType = new TypeToken<ArrayList<LatLng>>(){}.getType();
                // Array holding all of the routes/location for tracing the saved routes
                ArrayList<LatLng> loc = gson.fromJson(temp2, arrType);
                // HashMap that contains file path to pictures taken and the locations of the
                // pictures taken
                HashMap<String, LatLng> pics = gson.fromJson(temp, mapType);

                String firstPic = pics.keySet().toArray()[0].toString();

                // Save as route object for easier storing
                Route route = new Route(firstPic, pics, loc);
                saved.put(firstPic, route);

            }
            return saved;
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Opens the savedRoutes.json file to retrieve all of the saved routes data
    private String read(Context context, String fileName) {
        try {
//            String dir = getFilesDir().getAbsolutePath();
//            File file = new File(dir, fileName);
//            file.delete();
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }

    // Initalize the GridView with correct photo size for each grid
    private void InitilizeGridLayout() {
        Resources r = getResources();

        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, r.getDisplayMetrics());

        columnWidth = (int) ((getScreenWidth() - ((3 + 1) * padding)) / 3);

        gridView.setNumColumns(3);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    // Grabs the phone screen size to calculate correct size of the photo for the 3 column GridView
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
