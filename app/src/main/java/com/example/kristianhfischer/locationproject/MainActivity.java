package com.example.kristianhfischer.locationproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private final String TAG = MainActivity.class.getCanonicalName();

    private EditText mLocationSearchEditText;
    private Button mLocationSearchButton;
    private MyLocationService myLocationService;

    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationSearchEditText = (EditText) findViewById(R.id.locationSearchEditText);
        mLocationSearchButton = (Button) findViewById(R.id.locationSearchButton);
        mLocationSearchButton.setOnClickListener(this);
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




}
