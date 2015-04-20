package com.example.kristianhfischer.locationproject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;

    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARILY_UNAVAILABLE = 1;
    public static final int AVAILABLE = 2;
    public static int status;

    private static final String DESTINATION_LATITUDE = "Dest_lat";
    private static final String DESTINATION_LONGITUDE = "Dest_lng";
    private static final String DESTINATION_SAVED = "Dest_saved";
    private static final String DESTINATION_NAME = "Dest_name";

    static LatLng LOCATION;

    private final String TAG = MainActivity.class.getCanonicalName();

    private EditText mLocationSearchEditText;
    private Button mLocationSearchButton;
    private Marker mCurrentLocationMarker;
    private Marker mDestinationLocationMarker;
    private MyLocationService myLocationService;
    private MyLocationService.IMyLocationListener mILocationListener;

    private String mDestinationName;

    private boolean mBound;
    private boolean mDestinationSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationSearchEditText = (EditText) findViewById(R.id.locationSearchEditText);
        mLocationSearchButton = (Button) findViewById(R.id.locationSearchButton);
        mLocationSearchButton.setOnClickListener(this);

        //Interface to location service to receive updates for the current location
        //and destination location
        mILocationListener = new MyLocationService.IMyLocationListener() {
            @Override
            public void onDestinationLocationChanged(Location location) {
                updateDestinationLocation(location);
            }

            @Override
            public void onCurrentLocationChanged(Location location) {
                makeUseOfNewLocation(location);
            }
        };

        /*
         * This code changes the "Enter" key on the soft key board to a "search" button so
         * that users can search for the location without having to click the "search" button
         */
        mLocationSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(mBound) {
                        performSearch();
                    } else {
                        bindMyLocationService();
                    }
                    hideSoftKeyboard(MainActivity.this,v);
                    return true;
                }
                return false;
            }
        });

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            status = OUT_OF_SERVICE;
        }
        else {
            status = AVAILABLE;
            System.out.println("Status is available");
        }

        setUpMapIfNeeded();

        /*
         * Load any marker info that was reset during phone rotation or reloading app
         */
        if( savedInstanceState != null) {

            if( savedInstanceState.getBoolean(DESTINATION_SAVED)) {
                mDestinationSaved = true;
                mDestinationLocationMarker.setPosition(new LatLng(
                        savedInstanceState.getDouble(DESTINATION_LATITUDE),
                        savedInstanceState.getDouble(DESTINATION_LONGITUDE)));
                mDestinationName = savedInstanceState.getString(DESTINATION_NAME);
                Log.d(TAG, "Setting title to: " + mDestinationName);
                mDestinationLocationMarker.setTitle(
                        mDestinationName);
                mDestinationLocationMarker.setVisible(true);

            }
        }
    }

    /*
     * Save marker info so that it can be loaded if phone is rotated
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //User's destination location, if any
        if( mDestinationLocationMarker != null ) {
            if( mDestinationLocationMarker.isVisible() ) {
                savedInstanceState.putBoolean(DESTINATION_SAVED, new Boolean(true));
                savedInstanceState.putDouble(DESTINATION_LATITUDE, mDestinationLocationMarker.getPosition().latitude);
                savedInstanceState.putDouble(DESTINATION_LONGITUDE, mDestinationLocationMarker.getPosition().longitude);
                savedInstanceState.putString(DESTINATION_NAME, mDestinationName);
            } else {
                savedInstanceState.putBoolean(DESTINATION_SAVED, new Boolean(false));
            }
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    //Update current marker if current location changes
    private void makeUseOfNewLocation(Location location) {
        LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
        mCurrentLocationMarker.setPosition(LOCATION);
        if( !mCurrentLocationMarker.isVisible() ) {
            mCurrentLocationMarker.setVisible(true);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCATION, 16));
    }

    /*
     *Update location marker based on search results
     */
    private void updateDestinationLocation(Location location) {
        LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "Setting title to: " + mDestinationName);
        mDestinationLocationMarker.setPosition(LOCATION);
        mDestinationLocationMarker.setTitle(mDestinationName);
        //Refresh marker title
        mDestinationLocationMarker.hideInfoWindow();
        mDestinationLocationMarker.showInfoWindow();
        if( !mDestinationLocationMarker.isVisible()) {
            mDestinationLocationMarker.setVisible(true);
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (status == AVAILABLE){
            Location local = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LOCATION = new LatLng(local.getLatitude(), local.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCATION, 16));
            MarkerOptions a = new MarkerOptions().title("Current Location").position(LOCATION);
            mCurrentLocationMarker = mMap.addMarker(a);
            MarkerOptions destination = new MarkerOptions().title("Destination").
                    position(new LatLng(0, 0)).visible(false);
            mDestinationLocationMarker = mMap.addMarker(destination);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mBound) {
            bindMyLocationService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBound) {
            unbindMyLocationService();
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( !mBound ) {
            bindMyLocationService();
        }
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Unbind from the service
        if (mBound) {
            unbindMyLocationService();
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.locationSearchButton:
                if(mBound) {
                    performSearch();
                } else {
                    bindMyLocationService();
                }
                break;

            default:
                break;
        }
    }

    /*
     * Tasks performed after user clicks the "Search" button
     */
    private void performSearch() {
        String searchLocation = mLocationSearchEditText.getText().toString();
        mDestinationName = new String(searchLocation);
        Log.d(TAG, "The new location is: " + mDestinationName);
        myLocationService.searchForLocation(searchLocation);
    }

    private void bindMyLocationService() {
        Intent boundIntent = new Intent(this, MyLocationService.class);
        bindService(boundIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMyLocationService() {
        myLocationService.removeMyLocationListener();
        unbindService(mServiceConnection);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder binder = (MyLocationService.MyBinder) service;
            myLocationService = binder.getService();
            myLocationService.setMyLocationListener(mILocationListener);
            if( mDestinationSaved) {
                myLocationService.setDestinationLocation(mDestinationLocationMarker.getPosition());
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}