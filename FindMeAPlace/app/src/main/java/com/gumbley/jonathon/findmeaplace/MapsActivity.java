package com.gumbley.jonathon.findmeaplace;

import android.*;
import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mItemLocation;
    private String mItemName;
    private LatLng mLastLocation;
    private boolean mShouldShowAllLocations = false;
    private List<PlaceItem> mAllPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButtonGetDirections);

        Bundle b = getIntent().getBundleExtra(getString(R.string.LOCATIONS_BUNDLE));

        if (b != null) {
            mItemLocation = (LatLng) b.get(getString(R.string.ITEM_LOCATION));
            mItemName = b.getString(getString(R.string.ITEM_NAME));
            mLastLocation = (LatLng) b.get(getString(R.string.USER_LOCATION));
            mShouldShowAllLocations = b.getBoolean(getString(R.string.SHOW_ALL_NEARBY_PLACES));
        }

        if (mLastLocation != null) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri intentUri = Uri.parse("google.navigation:q=" + mItemLocation.latitude + "," + mItemLocation.longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }
            });
        }

        if (mShouldShowAllLocations) {
            // Get all the places from the database to display them all on the map
            PlacesDbHelper dbHelper = new PlacesDbHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] projection = {
                    PlacesContract.PlaceEntry.COLUMN_NAME_TITLE,
                    PlacesContract.PlaceEntry.COLUMN_NAME_LOCATION,
                    PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL,
                    PlacesContract.PlaceEntry.COLUMN_NAME_ADDRESS };

            Cursor curser = db.query(
                    PlacesContract.PlaceEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null);

            mAllPlaces = new ArrayList<>();
            while (curser.moveToNext()){
                String[] location = curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_LOCATION)).split(" ");
                PlaceItem place = new PlaceItem(
                        curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_TITLE)),
                        new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1])),
                        curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL)),
                        curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_ADDRESS)));
                place.setDistanceFromUserLocation(mLastLocation);
                mAllPlaces.add(place);
            }
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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);

        if (mItemLocation != null) {
            mMap.addMarker(new MarkerOptions().position(mItemLocation).title(mItemName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mItemLocation, 12.0f));
        } else if(mLastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 12.0f));

            if (mShouldShowAllLocations) {
                for (PlaceItem place : mAllPlaces) {
                    mMap.addMarker(new MarkerOptions().position(place.getLocation()).title(place.getTitle()));
                }
            }

        }
    }
}
