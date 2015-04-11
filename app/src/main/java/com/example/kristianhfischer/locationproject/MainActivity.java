package com.example.kristianhfischer.locationproject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.InputStream;


public class MainActivity extends ActionBarActivity {

    final String latitude = "39.2833";
    final String longitude = "-76.6167";


    private final String GOOGLE_KEY_ANDROID = "AIzaSyB5FT_N6HleR3kMR3FY4xlTPim3iLbuXOI";
    private final String GOOGLE_KEY_SERVER = "AIzaSyCt-3wAPn021Bzbi_SWVidglR9DXNgLEY0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        Double[] params = new Double[3];
        params[0] = 79.5;
        params[1] = -49.7;
        params[2] = 145.0;
        GoogleDownload task = new GoogleDownload();
        task.execute("");
        //GoogleDownload task2 = new GoogleDownload();
        //task.execute(task2);

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

    private class GoogleDownload extends AsyncTask<String, Double, String> {

        @Override
        protected String doInBackground(String... params) {
            String Url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=" + latitude + "," + longitude + "&radius=500&key=" +
                    GOOGLE_KEY_SERVER;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Url);
            String resultString = "";
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

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultString;
        }
    }

    
}
