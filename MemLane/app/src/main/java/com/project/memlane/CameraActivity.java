package com.project.memlane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SymbolTable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_WRITE = 1;
    private static final int REQUEST_STORAGE_READ = 1;
    private boolean mCameraGranted;
    private boolean mStorageGranted;
    public static final int BITMAP_SAMPLE_SIZE = 8;

    ImageView imageView;
    private String currentPhotoPath;
    File photoFile;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        imageView = (ImageView) findViewById(R.id.imageview);

        // Gets permission to use default camera
        getPhotoPermission();
        // Gets permission to access file to read and write
        getStoragePermission();
        // Opens default camera
        dispatchTakePictureIntent();

        // Set onClickListener for the each item in the bottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomAppBarPicture);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // Adds marker of the location of the picture taken to the map
                    case R.id.add:
                        MapsActivity.placeMarker("test", currentPhotoPath, 100);
                        finish();
                        break;
                    case R.id.clear:
                        finish();
                        break;
                }
                return true;
            }
        });

    }

    // Opens default camera for taking picutre and save the picture taken
    private void dispatchTakePictureIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);

        if (takePicture.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Adds photo taken to the default photo gallery of the phone
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the caputure taken was successful, create photo file then save to gallery
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(currentPhotoPath);
            galleryAddPic();
            getStoragePermissionRead();
            // Displays full size image of the photo taken and saved
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
            }
        }

    }

    // Gets permission to use default camera
    private void getPhotoPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE);
        }
    }

    // Gets permission to write to file/gallery
    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mStorageGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_WRITE);
        }
    }

    // Gets permission to read from the file/gallery
    private void getStoragePermissionRead() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mStorageGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_READ);
        }
    }

    // Sets the mCameraGranted permission and mStorageGranted to true if the permission are granted
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mCameraGranted = false;
        mStorageGranted = false;
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraGranted = true;
                    mStorageGranted = true;
                }
            }

        }
    }

    // Greates the picture file so it can be saved
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "/JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // If the direcotry does not exits create one
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = new File(storageDir + imageFileName + ".jpg");
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Saves photo taken to the phones default photo gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}