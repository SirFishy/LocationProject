package com.example.kristianhfischer.locationproject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class MyLocationService extends Service implements LocationListener {

    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private Location mStartingLocation;
    private Location mDestinationLocation;
    private IBinder myBinder = new MyBinder();

    private final String GOOGLE_KEY_ANDROID = "AIzaSyB5FT_N6HleR3kMR3FY4xlTPim3iLbuXOI";
    private final String GOOGLE_KEY_SERVER = "AIzaSyCt-3wAPn021Bzbi_SWVidglR9DXNgLEY0";
    private final String TAG = MyLocationService.class.getCanonicalName();
    private final double WITHIN_LOCATION_RADIUS_METERS = 200;

    private boolean mWithinLocationRadius;

    public class MyBinder extends Binder {
        MyLocationService getService() {
            return MyLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mStartingLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //GPS_PROVIDER); //GPS doesn't work inside
        mCurrentLocation = new Location(mStartingLocation);
        Log.d(TAG, "Starting Latitude: " + mStartingLocation.getLatitude());
        Log.d(TAG, "Starting Longitude: " + mStartingLocation.getLongitude());
        mWithinLocationRadius = false;
    }

    public MyLocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if( !mWithinLocationRadius ) {
            mWithinLocationRadius = checkDistanceToDestination();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void searchForLocation(String locationName) {
        mWithinLocationRadius = false;
        new GoogleSearchLocationTask().execute(locationName);
    }

    private boolean checkDistanceToDestination() {
        if( mStartingLocation == null || mDestinationLocation == null) {
            Log.d(TAG, "Starting Location Created: " + (mStartingLocation == null));
            Log.d(TAG, "Destination Location Created: " + (mDestinationLocation == null));
            return false;
        }

        float[] distance = new float[10];
        Location.distanceBetween(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), mDestinationLocation.getLatitude(),
                mDestinationLocation.getLongitude(), distance);
        if( distance[0] <= WITHIN_LOCATION_RADIUS_METERS ) {

            Toast.makeText(this, "You are within " + WITHIN_LOCATION_RADIUS_METERS + " meters of destination",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    private class GoogleSearchLocationTask extends AsyncTask<String, Double, Location> {

        @Override
        protected Location doInBackground(String... params) {
            String locationKeyword = params[0];
            String Url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=" + mStartingLocation.getLatitude() + "," +
                    mStartingLocation.getLongitude() +
                    "&rankby=distance" +
                    "&name=" + locationKeyword +
                    "&key=" + GOOGLE_KEY_SERVER;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Url);
            Location resultLocation = null;
            try {
                HttpResponse response = httpClient.execute(httpGet);
                InputStream istream = response.getEntity().getContent();
                BufferedInputStream bstream = new BufferedInputStream(istream);
                StringBuffer buffer = new StringBuffer();
                byte[] input = new byte[256];
                int len = 0;
                while( (len = istream.read(input)) != -1) {
                    buffer.append(new String(input, 0, len));
                }
                istream.close();
                Log.e("OUTPUT", "OUTPUT RESPONSE: " + buffer.toString());

                resultLocation = parseGetResponse(buffer.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if(resultLocation != null) {
                Log.d(TAG, "Latitude: " + resultLocation.getLatitude());
                Log.d(TAG, "Longitude: " + resultLocation.getLongitude());
            } else {
                Log.d(TAG, "Null Location");
            }
            return resultLocation;
        }


        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            mDestinationLocation = location;
            checkDistanceToDestination();
            //Pass this to next task
        }

        private Location parseGetResponse(String result) throws JSONException {
            JSONObject jObject = new JSONObject(result);
            JSONArray jArray = jObject.getJSONArray("results");
            JSONObject firstResult = jArray.getJSONObject(0);
            JSONObject resultGeometry = firstResult.getJSONObject("geometry");
            JSONObject resultLocation = resultGeometry.getJSONObject("location");
            String longitude = resultLocation.getString("lng");
            String latitude = resultLocation.getString("lat");
            Location resultLocationObj = new Location("");
            resultLocationObj.setLatitude(Double.parseDouble(latitude));
            resultLocationObj.setLongitude(Double.parseDouble(longitude));
            return resultLocationObj;
        }
    }
}
