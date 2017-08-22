package com.gumbley.jonathon.findmeaplace;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   ListFragment.OnFragmentInteractionListener,
                   DetailsFragment.OnFragmentInteractionListener,
                   GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOCATION_BUNDLE = "location";
    private static final String LOCATION_BUNDLE_LATITUDE = "location_latitude";
    private static final String LOCATION_BUNDLE_LONGITUTDE = "location_longitude";
    private static final String LOCATION_BUNDLE_NAME = "location_name";

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 0;
    private static final int MY_PERMISSION_REQUEST_INTERNET = 10;
    private static final int DEFAULT_RADIUS = 5000;

    private static final String CURRENT_ITEM_TYPE = "current item type";
    private static final String LAST_LOCATION = "last location";
    private static final String FROM_ROTATE = "from rotate";
    boolean fromRotate = false;

    GoogleApiClient mGoogleApiClient;
    LatLng mLastLocation;
    String mCurrentItemType = "";
    List<PlaceItem> mCurrentItems;

    RequestQueue mRequestQueue;

    PlacesDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow another application to send a location (from a bundle) which this application will display onto the map activity
        if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
            if ("location".equals(getIntent().getType())) {
                Bundle bundleFromApp = getIntent().getBundleExtra(LOCATION_BUNDLE);
                LatLng location = new LatLng(
                        bundleFromApp.getDouble(LOCATION_BUNDLE_LATITUDE),
                        bundleFromApp.getDouble(LOCATION_BUNDLE_LONGITUTDE));
                String locationName = bundleFromApp.getString(LOCATION_BUNDLE_NAME);
                Intent i = new Intent(this, MapsActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(getString(R.string.ITEM_LOCATION), location);
                b.putString(getString(R.string.ITEM_NAME), locationName);
                i.putExtra(getString(R.string.LOCATIONS_BUNDLE), b);
                startActivity(i);
            }
        } else {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (savedInstanceState != null) {
                showProgressLoader(false);
                fromRotate = savedInstanceState.getBoolean(FROM_ROTATE);
                if (savedInstanceState.containsKey(LAST_LOCATION)) {
                    String[] location = savedInstanceState.getStringArray(LAST_LOCATION);
                    mLastLocation = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
                }

                mCurrentItemType = savedInstanceState.getString(CURRENT_ITEM_TYPE);
                if (mCurrentItemType.equals(getString(R.string.food_type))) {
                    getSupportActionBar().setTitle(getString(R.string.food));
                }
                else if (mCurrentItemType.equals(getString(R.string.lodging))) {
                    getSupportActionBar().setTitle(getString(R.string.lodging));
                }
                else if (mCurrentItemType.equals(getString(R.string.petrol_type))) {
                    getSupportActionBar().setTitle(getString(R.string.petrol));
                }
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            mRequestQueue = Volley.newRequestQueue(this);
            mDbHelper = new PlacesDbHelper(this);

            if(mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(FROM_ROTATE, true);
        if (mLastLocation != null) {
            bundle.putStringArray(LAST_LOCATION, new String[] { Double.toString(mLastLocation.latitude), Double.toString(mLastLocation.longitude) });
        }
        bundle.putString(CURRENT_ITEM_TYPE, mCurrentItemType);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reload) {
            showProgressLoader(true);
            getAllPlaces();
            return true;
        }
        else if (id == R.id.action_change_radius) {
            Intent i = new Intent(this, ChangeRadiusActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = getSupportActionBar().getTitle().toString();

        if (id == R.id.nav_food) {
            setUpList(getString(R.string.food_type));
            title = getResources().getString(R.string.food);
        } else if (id == R.id.nav_sleep) {
            setUpList(getString(R.string.lodging));
            title = getResources().getString(R.string.lodging);
        } else if (id == R.id.nav_petrol) {
            setUpList(getString(R.string.petrol_type));
            title = getResources().getString(R.string.petrol);
        } else if (id == R.id.nav_map) {
            Intent i = new Intent(this, MapsActivity.class);
            Bundle b = new Bundle();
            b.putParcelable(getResources().getString(R.string.USER_LOCATION), mLastLocation);
            b.putBoolean(getString(R.string.SHOW_ALL_NEARBY_PLACES), true);
            i.putExtra(getResources().getString(R.string.LOCATIONS_BUNDLE), b);
            startActivity(i);
            return true;
        }

        Fragment fragment = new ListFragment();
        ((ListFragment)fragment).setItems(mCurrentItems);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.addToBackStack(null);
        ft.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_FINE_LOCATION) {
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllPlaces();
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, getString(R.string.permissions_denied_error), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_PERMISSION_REQUEST_INTERNET) {
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.INTERNET) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllPlaces();
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, getString(R.string.permissions_denied_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!fromRotate) getAllPlaces();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e("LocationEabledError", ex.getMessage());
            Log.e("LocationEabledError", ex.getStackTrace().toString());
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e("LocationEabledError", ex.getMessage());
            Log.e("LocationEabledError", ex.getStackTrace().toString());
        }

        if (!gps_enabled || !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // Show toast telling user needs location and internet
                    Toast.makeText(getApplicationContext(), getString(R.string.permissions_denied_error), Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }
        return false;
    }

    private void getAllPlaces() {
        // Check that location services is enabled
        isLocationEnabled();
        // Check if the internet permission is allowed, if not request it
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, MY_PERMISSION_REQUEST_INTERNET);
        }
        else {
            // Check if fine location permission is allowed, if not request it
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                // location will be null if location services is turned off, only get data if their is a location
                if (location != null) {
                    mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    db.execSQL(PlacesContract.SQL_DELETE_ENTRIES);
                    mDbHelper.onCreate(db);
                    db = null;

                    getPlaces(getString(R.string.food_type));
                    getPlaces(getString(R.string.lodging));
                    getPlaces(getString(R.string.petrol_type));
                }
            }
        }
    }

    private void getPlaces(String type) {
        final String typeLower = type.toLowerCase();
        int radius = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).getInt(getString(R.string.preference_radius), DEFAULT_RADIUS);

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                mLastLocation.latitude + "," + mLastLocation.longitude +
                "&radius=" + radius +
                "&types=" + typeLower +
                "&key=" + getResources().getString(R.string.google_places_key);
        Log.d("url", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseJSONObject(response, typeLower);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.getMessage());
                Log.e("VolleyError", error.getStackTrace().toString());
            }
        });
        mRequestQueue.add(request);
    }

    private void parseJSONObject(JSONObject object, String type) {
        try {
            type = type.toLowerCase();
            // Create the database to read to
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            JSONArray results = object.getJSONArray("results");
            // Go through each place in the JSON array
            for (int i = 0; i < results.length(); i++) {
                // For each place add the place data to values which will be put into a database
                JSONObject jsonPlace = results.getJSONObject(i);
                values.put(PlacesContract.PlaceEntry.COLUMN_NAME_TITLE, jsonPlace.getString("name"));
                JSONObject location = jsonPlace.getJSONObject("geometry").getJSONObject("location");
                values.put(PlacesContract.PlaceEntry.COLUMN_NAME_LOCATION, location.getString("lat") + " " + location.getString("lng"));
                // Some places don't have a photo so just put an empty string for those
                if (jsonPlace.has("photos")) {
                    values.put(
                            PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL,
                            "https://maps.googleapis.com/maps/api/place/photo?maxheight=600&photoreference=" + jsonPlace.getJSONArray("photos").getJSONObject(0).getString("photo_reference") +
                                    "&key=" + getResources().getString(R.string.google_places_key));
                } else {
                    values.put(PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL, "");
                }
                values.put(PlacesContract.PlaceEntry.COLUMN_NAME_ADDRESS, jsonPlace.getString("vicinity"));
                values.put(PlacesContract.PlaceEntry.COLUMN_NAME_TYPE, type);
                // Insert into the database
                db.insert(PlacesContract.PlaceEntry.TABLE_NAME, null, values);
            }
            // When the food places are all gathered then show them, other items may still be downloading
            if (type.equals(getString(R.string.food_type).toLowerCase())) {
                // Hide progress loader
                showProgressLoader(false);
                // Get list of items of type food
                setUpList(getString(R.string.food_type));
                // Display the food list fragment
                Fragment fragment = new ListFragment();
                ((ListFragment)fragment).setItems(mCurrentItems);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, fragment).commit();
                getSupportActionBar().setTitle(getResources().getString(R.string.food));
            }

        } catch (JSONException e) {
            Log.e("parsing JSON", e.getMessage());
            Log.e("parsing JSON", e.getStackTrace().toString());
        }

    }

    private void setUpList(String type) {
        type = type.toLowerCase();
        mCurrentItemType = type;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                PlacesContract.PlaceEntry.COLUMN_NAME_TITLE,
                PlacesContract.PlaceEntry.COLUMN_NAME_LOCATION,
                PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL,
                PlacesContract.PlaceEntry.COLUMN_NAME_ADDRESS };
        String selection = PlacesContract.PlaceEntry.COLUMN_NAME_TYPE + " = ?";
        String[] selectionArgs = { type };
        String sortOrder = PlacesContract.PlaceEntry.COLUMN_NAME_TITLE + " ASC";

        Cursor curser = db.query(
                PlacesContract.PlaceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        mCurrentItems = new ArrayList<>();

        while (curser.moveToNext()) {
            String[] location = curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_LOCATION)).split(" ");
            PlaceItem place = new PlaceItem(
                    curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_TITLE)),
                    new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1])),
                    curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_IMAGE_URL)),
                    curser.getString(curser.getColumnIndex(PlacesContract.PlaceEntry.COLUMN_NAME_ADDRESS)));
            place.setDistanceFromUserLocation(mLastLocation);
            mCurrentItems.add(place);
        }
        // Order the list by the closest place first
        Collections.sort(mCurrentItems, new Comparator<PlaceItem>() {
            @Override
            public int compare(PlaceItem o1, PlaceItem o2) {
                return (int)(o1.getDistanceFrom() - o2.getDistanceFrom());
            }
        });

    }

    private void showProgressLoader(boolean show){
        if (show) {
            findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.progress_layout).setVisibility(View.GONE);
        }
    }
}
