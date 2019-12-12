package com.project.memlane;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Route savedRoute;
    private static GoogleMap mMap;
    private static ClusterManager<ClusterMarker> mClusterManager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_route);
        // Grabs the data of the specific saved routes data
        // Data is coming from the on picture click in the mainActivity
        savedRoute = getIntent().getParcelableExtra("savedLocations");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bottomNavigationView = findViewById(R.id.bottomAppBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Takes it back to mainActivity
                        Intent mainIntent = new Intent(SavedRouteActivity.this, MainActivity.class);
                        finish();
                        startActivity(mainIntent);
                        break;
                    case R.id.view_pictures:
                        // Can view all of the pictures taken at specific saved routes
                        ArrayList<String> urls = new ArrayList<String>(savedRoute.getPictureLocations().keySet());
                        Intent intent = new Intent(getApplicationContext(), PicturesActivity.class);
                        intent.putExtra("urls", urls);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Draws line that traces saved routes
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(10);
        polylineOptions.addAll(savedRoute.getRoutes());
        mMap.addPolyline(polylineOptions);

        // Hands over control to the ClusterManager so it can place markers
        mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
        mClusterManager.setRenderer(new MyClusterManagerRenderer(this, mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        // When cluster marker is clicked opens the dialog box that fives option of getting direction
        // or view all the pictures
        mClusterManager.setOnClusterClickListener(
                new ClusterManager.OnClusterClickListener<ClusterMarker>() {
                    // When the cluster is clicked AlertDialog is displayed
                    // In AlertDialog, there are two options
                    // Direction will give direction to the cluster marker taken using default
                    // Google map
                    // View photo will let you view all photos taken at that cluster marker location
                    @Override
                    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
                        LatLng lastpoint = null;
                        mMap.getUiSettings().setMapToolbarEnabled(true);
                        LatLngBounds.Builder builder = LatLngBounds.builder();
                        for (ClusterItem item : cluster.getItems()) {
                            builder.include(item.getPosition());
                            lastpoint = item.getPosition();
                        }

                        ArrayList<LatLng> locations = new ArrayList<LatLng>(savedRoute.getPictureLocations().values());
                        LatLng first = locations.get(0);
                        LatLng last = locations.get(locations.size() - 1);
                        LatLng locRange = new LatLng(last.longitude - first.longitude, last.latitude - first.latitude);

                        // Get the LatLngBounds
                        final LatLngBounds bounds = builder.build();

                        ArrayList<String> urls = new ArrayList<>();
                        for (ClusterMarker c : cluster.getItems()) {
                            urls.add(c.getIconPicture());
                        }

                        // Animate camera to the bounds
                        try {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Open AlertDialog
                        buildOptions(SavedRouteActivity.this, lastpoint, urls);
                        return true;
                    }
                });

        // When cluster item marker is clicked opens the dialog box that fives option of getting direction
        // or view all the pictures
        mClusterManager.setOnClusterItemClickListener(
                new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
                    // When the cluster item is clicked AlertDialog is displayed
                    // In AlertDialog, there are two options
                    // Direction will give direction to the cluster item marker taken using default
                    // Google map
                    // View photo will let you view all photos taken at that cluster marker items location
                    @Override
                    public boolean onClusterItemClick(ClusterMarker clusterMarker) {
                        ArrayList<String> singlePic = new ArrayList<>();
                        singlePic.add(clusterMarker.getIconPicture());
                        buildOptions(SavedRouteActivity.this, clusterMarker.getPosition(), singlePic);
                        return true;
                    }
                });

        // TODO: Implement infoWindow
        mClusterManager.setOnClusterInfoWindowClickListener(
                new ClusterManager.OnClusterInfoWindowClickListener<ClusterMarker>() {
                    @Override
                    public void onClusterInfoWindowClick(Cluster<ClusterMarker> cluster) {

                    }
                });
        placeMarker(savedRoute.getPictureLocations());
    }

    // Places the custom cluster markers of the locations of the photo taken on the map
    public static void placeMarker( HashMap<String, LatLng> pictureLocations) {
        LatLng lastPoint = null;
        for (Map.Entry<String, LatLng> entry : pictureLocations.entrySet()) {
            ClusterMarker mMarker = new ClusterMarker(entry.getValue(), "test", "this is test", entry.getKey());
            mClusterManager.addItem(mMarker);
            mClusterManager.cluster();
            lastPoint = entry.getValue();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 15));
    }

    // Creates AlertDialog that will open when cluster marker is clicked
    private void buildOptions(Context context, final LatLng lastLocation, final ArrayList<String> urls) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Dialog);
        builder.setTitle("Menu");
        builder.setNegativeButton("Direction", new DialogInterface.OnClickListener() {
            // Directions button will give direction to the location of the piture takne
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:"+lastLocation.latitude +", " + lastLocation.longitude));
                startActivity(mapIntent);
            }
        });
        builder.setPositiveButton("View Pictures", new DialogInterface.OnClickListener() {
            // Will show all of the photos taken at the location
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), PicturesActivity.class);
                intent.putExtra("urls", urls);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }
}
