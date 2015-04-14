package com.example.kristianhfischer.locationproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;

    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARILY_UNAVAILABLE = 1;
    public static final int AVAILABLE = 2;
    public static int status;

    static LatLng LOCATION;

    private final String TAG = MainActivity.class.getCanonicalName();

    private EditText mLocationSearchEditText;
    private Button mLocationSearchButton;
    private MyLocationService myLocationService;

    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationSearchEditText = (EditText) findViewById(R.id.locationSearchEditText);
        mLocationSearchButton = (Button) findViewById(R.id.locationSearchButton);
        mLocationSearchButton.setOnClickListener(this);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                makeUseOfNewLocation(location);
                System.out.println("CALLING MAKE USE OF NEW LOCATION!");

            }

            @Override
            public void onStatusChanged(String provider, int stat, Bundle extras) {
                /*
                if (provider == LocationManager.GPS_PROVIDER){
                    status = stat;
                    System.out.println("STATUS CHANGED!!!!!!!!!!!!!!!!!!!");
                }
                */
            }

            @Override
            public void onProviderEnabled(String provider) {
                /*
                if (provider == LocationManager.GPS_PROVIDER){
                    status = AVAILABLE;
                    //setUpMapIfNeeded();
                    System.out.println("**********PROVIDER ENABLED!!!!!!!!!!!!!!!!!!!");
                }
                */
            }

            @Override
            public void onProviderDisabled(String provider) {
                /*
                if (provider == LocationManager.GPS_PROVIDER) {
                    status = OUT_OF_SERVICE;
                    System.out.println("PROVIDER DISABLED!!!!**********!!!!!!!!!!!!!!!");

                }
                */
            }
        };



        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false){
            status = OUT_OF_SERVICE;
        }
        else {
            status = AVAILABLE;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    private void makeUseOfNewLocation(Location location) {

        LOCATION = new LatLng(location.getLatitude(), location.getLongitude());

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCATION, 16));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LOCATION));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(35, 25)).title("Marker"));

        mMap.addMarker(new MarkerOptions()
                .position(LOCATION)
                .title("LOCATION WORKS IN makeuseofnewlocation"));

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (status == AVAILABLE){
            Location local = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LOCATION = new LatLng(local.getLatitude(), local.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(0,0)));

            mMap.addMarker(new MarkerOptions().position(LOCATION).title("if LOCATION WORKS IN SETUPMAP"));
        }
        else{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(0,0)));

            mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("else LOCATION WORKS IN SETUPMAP"));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart'd");
        if(!mBound) {
            bindMyLocationService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause'd");
        /*if(mDetecting) {
            Toast.makeText(MainActivity.this, "Activity Detection Halted", Toast.LENGTH_SHORT).show();
            mDetecting = false;
        }*/
        if(mBound) {
            unbindMyLocationService();
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume'd");
        if( !mBound ) {
            bindMyLocationService();
        }
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop'd");
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
                String searchLocation = mLocationSearchEditText.getText().toString();
                if(mBound) {
                    myLocationService.searchForLocation(searchLocation);
                } else {
                    bindMyLocationService();
                }
                break;
            default:
                break;
        }
    }

    private void bindMyLocationService() {
        Intent boundIntent = new Intent(this, MyLocationService.class);
        bindService(boundIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMyLocationService() {
        unbindService(mServiceConnection);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder binder = (MyLocationService.MyBinder) service;
            myLocationService = binder.getService();
            if (myLocationService == null) {
                Log.d(TAG, "Service obj is indeed null");
            }
            mBound = true;
            //Toast.makeText(MainActivity.this, "bound service started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(MainActivity.this, "The service has been disconnected", Toast.LENGTH_SHORT).show();

        }
    };


    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(0, 0), 16));

        //mMap.addMarker(new MarkerOptions().position(new LatLng(35, 25)).title("Marker"));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0,0))
                .title("MarkerInOnMapReady"));

    }
}
