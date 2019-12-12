package com.project.memlane;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.*;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker>
{

    private final IconGenerator iconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private final ImageView imageView;
    private final ImageView clusterImageView;
    private final int markerWidth;
    private final int markerHeight;
    private GoogleMap mMap;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap,
                                    ClusterManager<ClusterMarker> clusterManager) {

        super(context, googleMap, clusterManager);

        View clusterView = LayoutInflater.from(context).inflate(R.layout.multi_profile, null);
        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        mClusterIconGenerator = new IconGenerator(context.getApplicationContext());

        // Creates the custom Cluster item marker and Cluster marker
        imageView = new ImageView(context.getApplicationContext());
        clusterImageView = (ImageView) clusterView.findViewById(R.id.image);
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        mClusterIconGenerator.setContentView(clusterView);
        iconGenerator.setContentView(imageView);
        mMap = googleMap;

    }


     // Rendering of the individual ClusterItems with custom marker
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        File f = new File(item.getIconPicture());
        // Uses photo from the gallery to create custom ClusterItem marker
        imageView.setImageURI(Uri.fromFile(f));
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
    }

    // Rendering of the cluster with custom marker
    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterMarker> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        Iterator<ClusterMarker> iterator = cluster.getItems().iterator();
        File f = new File(iterator.next().getIconPicture());
        // Uses photo from the gallery to create custom Cluster marker
        clusterImageView.setImageURI(Uri.fromFile(f));
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Renders cluster when there is more than on cluster items
        return cluster.getSize() > 1;
    }

}


