package com.project.memlane;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<String> uris;
    private int imageWidth;

    public ImageAdapter(Activity activity, ArrayList<String> uris, int imageWidth) {
        this.activity = activity;
        this.uris = uris;
        this.imageWidth = imageWidth;
    }

    // Returns the total number of photo files
    @Override
    public int getCount() {
        return uris.size();
    }

    // Grabs the file path to the photo file at certain position in grid
    @Override
    public Object getItem(int position) {
        return this.uris.get(position);
    }

    // Grabs the position of the photo on the grid
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Builds grid view image from the photo saved in gallery
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        // If there no view exists create one
        if (convertView == null) {
            imageView = new ImageView(activity);
            imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,
                    imageWidth));
        }
        else
        {
            // If view already exists convert it to ImageView
            imageView = (ImageView) convertView;
        }

        // Get photo from the gallery and resize it to fit into grid
        Bitmap image = decodeFile(uris.get(position), imageWidth,
                imageWidth);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // Sets cropped image to the imageView to be displayed
        imageView.setImageBitmap(image);
        // Get the orientation rotation value of the photo
        int rotate = getCameraPhotoOrientation(uris.get(position));
        // Picture is coming in rotated so rotated back to original state
        imageView.setRotation(rotate);

        // Sets the onClickListener for the picture
        imageView.setOnClickListener(new OnImageClickListener(position));

        return imageView;
    }

    // GridView picture onClickListener
    class OnImageClickListener implements View.OnClickListener {

        int postion;

        // constructor
        public OnImageClickListener(int position) {
            this.postion = position;
        }

        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity
            Intent i = new Intent(activity, FullScreenActivity.class);
            i.putExtra("links", uris);
            i.putExtra("position", postion);
            activity.startActivity(i);
        }

    }

    // Returns the rotation value of the photo from the gallery
    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif  = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    // Given file path, gets photo from the gallery then crops the photo to the appropriate size
    // to fit into GridView
    public static Bitmap decodeFile(String filePath, int WIDTH, int HIGHT) {
        try {

            File f = new File(filePath);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;
            int scale = 1;
            // Scares the size of the photo
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                    && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
