package com.project.memlane;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Polyline gpsTrack;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private static LatLng lastKnownLatLng;
    private GoogleApiClient googleApiClient;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static ClusterManager<ClusterMarker> mClusterManager;
    private BottomNavigationView bottomNavigationView;
    private static HashMap<String, LatLng> pictureLocations = new HashMap<>();
    private static String fileName = "savedRoutes.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        bottomNavigationView = findViewById(R.id.bottomAppBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add:
                        // Saves current state of the map including all the locations tracked and
                        // file paths of the photos taken and locations.
                        Intent mainIntent = new Intent(MapsActivity.this, MainActivity.class);
                        ArrayList<LatLng> points = new ArrayList<>(gpsTrack.getPoints());


                        boolean doesFileExist = doesFileExist(getApplicationContext(), fileName);
                        if (doesFileExist) {
                            // If file savedRoutes.json exists then add this data to it
                            String jsonStr = read(getApplicationContext(), fileName);
                            addToSavedRoutes(getApplicationContext(), jsonStr, pictureLocations, points);
                            pictureLocations.clear();
                            points.clear();

                        } else {
                            // Creates savedRoutes.json file if it does not exists to save the data
                           createFile(getApplicationContext(), fileName, pictureLocations, points);
                        }
                        finish();
                        startActivity(mainIntent);
                        break;
                    case R.id.take_picture:
                        // Opens the default camera to take a picture
                        Intent intent = new Intent(MapsActivity.this, CameraActivity.class);
                        startActivity(intent);
                        break;
                    }
                return true;
            }
        });

        // To kepp on track of locations
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    // If the savedRoutes.json exits add the new data to it
    private void addToSavedRoutes(Context context, String jsonStr,  HashMap<String, LatLng> pictureLocations, ArrayList<LatLng> points) {
        try {
            JSONObject savedRoutes = new JSONObject(jsonStr);
            // Grab savedRoutes array witch contains datas from the all of the saved routes
            JSONArray savedList =  savedRoutes.getJSONArray("savedRoutes");

            Gson gson = new Gson();
            String listString = gson.toJson(points, new TypeToken<ArrayList<LatLng>>(){}.getType());
            JSONArray pointsArr = new JSONArray(listString);

            String picLocs = gson.toJson(pictureLocations);

            // Create new JSONOBJect to store new saved route data
            JSONObject newRoute = new JSONObject();
            newRoute.put("PictureLocations", picLocs);
            newRoute.put("routes", pointsArr);

            // Add new saved route data to the list of saved routes
            savedList.put(newRoute);
            JSONObject newSavedRoutes = new JSONObject();
            newSavedRoutes.put("savedRoutes", savedList);

            // Write the updated data back to the file
            String newJsonStr = newSavedRoutes.toString();
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(newJsonStr.getBytes());
            fos.close();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Opens the savedRoute.json file and read the data
    private String read(Context context, String fileName) {
        try {
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

    // If the savedRoutes.json file doesn't exits create new one
    private void createFile(Context context, String fileName, HashMap<String, LatLng> pictureLocations, ArrayList<LatLng> points) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            String jsonStr = "";
            Gson gson = new Gson();
            String listString = gson.toJson(points, new TypeToken<ArrayList<LatLng>>(){}.getType());
            JSONArray pointsArr = new JSONArray(listString);

            String picLocs = gson.toJson(pictureLocations);

            // JSONOBject to hold the data
            JSONObject saves = new JSONObject();
            saves.put("PictureLocations", picLocs);
            saves.put("routes", pointsArr);

            // Array that will hold the all of the saved routes data
            JSONArray savedList = new JSONArray();
            savedList.put(saves);
            JSONObject savedRoutes = new JSONObject();
            savedRoutes.put("savedRoutes", savedList);

            // Create savedRoutes.json and saved the data
            jsonStr = savedRoutes.toString();
            fos.write(jsonStr.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check to see if the file exists
    public boolean doesFileExist(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }


    //Inflate menu to bottom bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Traces your routes by drawing red lines
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(10);
        gpsTrack = mMap.addPolyline(polylineOptions);

        // Get permission to use location services
        getLocationPermission();

        // Updates location UI
        updateLocationUI();

        // Grabs the location of the device
        getDeviceLocation();

        // Hands control over of placing marker to the cluster manager so it can place markers
        mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
        mClusterManager.setRenderer(new MyClusterManagerRenderer(this, mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(
                new ClusterManager.OnClusterClickListener<ClusterMarker>() {
                    @Override
                    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
                        LatLngBounds.Builder builder = LatLngBounds.builder();
                        for (ClusterItem item : cluster.getItems()) {
                            builder.include(item.getPosition());
                        }
                        // Get the LatLngBounds
                        final LatLngBounds bounds = builder.build();

                        // Animate camera to the bounds
                        try {
                            Toast.makeText(MapsActivity.this, "Cluster item click", Toast.LENGTH_SHORT).show();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ArrayList<String> urls = new ArrayList<>();
                        for (ClusterMarker c : cluster.getItems()) {
                           urls.add(c.getIconPicture());
                        }
                        Intent intent = new Intent(getApplicationContext(), PicturesActivity.class);
                        intent.putExtra("urls", urls);
                        startActivity(intent);
                        return true;
                    }
                });
        mClusterManager.setOnClusterItemClickListener(
                new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
                    @Override
                    public boolean onClusterItemClick(ClusterMarker clusterMarker) {
                        return false;
                    }
                });

        mClusterManager.setOnClusterInfoWindowClickListener(
                new ClusterManager.OnClusterInfoWindowClickListener<ClusterMarker>() {
                    @Override
                    public void onClusterInfoWindowClick(Cluster<ClusterMarker> cluster) {

                    }
                });

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    // Gets the location permission
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Checks for the location permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    // Update the default UI when location permission is granted
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            LatLng current = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
//                            mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
                        } else {
                            System.out.println("Something went wrong");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // When the location changes update the last know location and update the polyline
    @Override
    public void onLocationChanged(Location location) {
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateTrack();
    }

    // Starts the location update. IT will update the location at certain time interval
    protected void startLocationUpdates() {
        // Request the location
        LocationRequest locationRequest = new LocationRequest();
        // Sets time interval to request location
        locationRequest.setInterval(15 * 1000);
        // Sets the fastest/shortest time interval to request location
        locationRequest.setFastestInterval(5 * 1000);
        // Use mode that will balance the bettery usage and accuracy to have minimum impact on
        // the battery and highest accuracy at the same time as possible
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Check and see if the location tacking is permitted and if it is start the service
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,
                this);
    }

    // Stops the location update
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    // Updates the locations so we can continuously update the poly line and tace the route
    private void updateTrack() {
        List<LatLng> points = gpsTrack.getPoints();
        points.add(lastKnownLatLng);
        gpsTrack.setPoints(points);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Places the marker on location the picture was taken
    public static void placeMarker(String title, String currentPhotoPath, int size) {
        ClusterMarker mMarker = new ClusterMarker(lastKnownLatLng, "test", "this is test", currentPhotoPath);
        pictureLocations.put(currentPhotoPath, lastKnownLatLng);
        mClusterManager.addItem(mMarker);
        mClusterManager.cluster();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, DEFAULT_ZOOM));
    }
}
