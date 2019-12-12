package com.project.memlane;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

class FullScreenImageAdapter extends PagerAdapter {
    private Activity activity;
    private ArrayList<String> urls;
    private LayoutInflater inflater;


    public FullScreenImageAdapter(Activity activity, ArrayList<String> urls) {
        this.activity = activity;
        this.urls = urls;
    }

    // Returns the number of picture files
    @Override
    public int getCount() {
        return urls.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((RelativeLayout) object);
    }

    // Creates slide-able full size image of the photo
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
        FloatingActionButton btnClose;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container, false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (FloatingActionButton) viewLayout.findViewById(R.id.floating_action_button);

        // Reads photo from the given list of image file path
        // then using bit map the set the iamge to imageView to display
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(urls.get(position), options);
        int rotate = 0;
        // Images are coming in rotated to right so grab how much the orientation of the image
        // has been rotated so we can re-rotate to the original state
        rotate = getCameraPhotoOrientation(urls.get(position));
        imgDisplay.setImageBitmap(bitmap);
        // Rotate the image back to orginal state
        imgDisplay.setRotation(rotate);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        // Adds this full picture view to the ViewPager container so it can be part of slide-able
        // pictures
        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    // Removes the picture when no longer needed
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    // Grabs the rotation value of photo orientation from the photo grab from the gallery file
    // Pictures were coming in rotated so need this funtion to find out the rotation value
    // so I can rotate back to original orientation
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
}
