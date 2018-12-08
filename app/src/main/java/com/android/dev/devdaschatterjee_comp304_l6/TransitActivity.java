package com.android.dev.devdaschatterjee_comp304_l6;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class TransitActivity extends AppCompatActivity {

    private TextView txtDirections;
    //
    EditText startAddressEdt;
    EditText endAddressEdt;
    ArrayAdapter startAdapter, endAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        // set a default start and destinaton
        startAddressEdt = (EditText) findViewById(R.id.startAddressEdt);

        endAddressEdt = (EditText) findViewById(R.id.endAddressEdt);


    }

    public void onGetDirectionClick(View view) {
        //keep in mind your AVD should support Google API
        //
        String strUrl = "https://maps.googleapis.com/maps/api/directions/json?";
        String startAddress = startAddressEdt.getText().toString();
        String endAddress = endAddressEdt.getText().toString();

        if (startAddress.equals("")) {
            startAddressEdt.setError("Please enter start address");
        } else {
            if (endAddress.equals("")) {
                endAddressEdt.setError("Please enter end address");
            } else {
                try

                {
                    Locale locale = Locale.getDefault();
                    Geocoder geocoder = new Geocoder(this, Locale.CANADA);
                    String encodedStartAddress, encodedEndAddress;
                    encodedStartAddress = URLEncoder.encode(startAddress, "UTF-8");
                    encodedEndAddress = URLEncoder.encode(endAddress, "UTF-8");


                    String origine = "origin=" + encodedStartAddress;
                    String destination = "destination=" + encodedEndAddress;
                    String sensorValue = URLEncoder.encode("false", "UTF-8");
                    String sensor = "sensor=" + sensorValue;

                    Date date = new Date();
                    long l = date.getTime() / 1000;
                    System.out.println("Time: " + String.valueOf(l));
                    String departureTimeValue = URLEncoder.encode(String.valueOf(l), "UTF-8");
                    System.out.println(departureTimeValue);
                    String departureTime = "departure_time=" + departureTimeValue;
                    //
                    String modeValue = URLEncoder.encode("transit", "UTF-8");
                    String mode = "mode=" + modeValue;

                    //create the url
                    strUrl = strUrl + origine + "&" + destination + "&" + sensor + "&" + departureTime + "&" + mode + "&key=AIzaSyDPSfQ1Mvog9bt6hQI-Q5X27uf3Yw6h4N4";
                    System.out.println(strUrl);
                    Log.d("url", strUrl);


                    new ReadTransitJSONFeedTask().execute(strUrl);
                } catch (UnsupportedEncodingException e) {
                    Log.d("encodingerror", e.getMessage());
                } catch (IOException e) {
                    Log.d("geocode error", e.getMessage());

                }
            }
        }


    }

    //
    private class ReadTransitJSONFeedTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {

            Log.d("url", urls[0]);
            return getDirections(urls[0]);
        }

        protected void onPostExecute(String result) {

            txtDirections = (TextView) findViewById(R.id.txtDirections);


            if (!result.equals("")) {
                txtDirections.setText(result);
            } else {
                txtDirections.setText("Can't find directions.Please make sure start and destination address is correct");
            }

            // txtDirections.setMovementMethod(ScrollingMovementMethod.getInstance());


        }

        public String getDirections(String urlString) {

            String result = null;


            try {
                String response = sendRequest(urlString);
                result = parseResponse(response);


            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;

        }
    }

    private String parseResponse(String response) {

        String directions = "";

        try {
            String str = response; //readJSONFeed(result);
            Log.d("link", str);
            // build a JSON object
            JSONObject obj = new JSONObject(str);
            if (obj.getString("status").equals("OK")) {
                System.out.println("OK");
                JSONArray routes = obj.getJSONArray("routes");
                JSONObject data, data1;
                JSONObject bounds;
                //System.out.println(routes.length());
                //
                for (int i = 0; i < routes.length(); i++) {
                    data = routes.getJSONObject(i);
                    Iterator keys = data.keys();
                    while (keys.hasNext()) {
                        System.out.println(keys.next());
                    }
                    //System.out.println(data);
                    bounds = data.getJSONObject("bounds");
                    System.out.println(bounds);
                    Iterator keys1 = bounds.keys();
                    while (keys1.hasNext()) {
                        System.out.println(keys1.next());
                    }

                    JSONArray legs = data.getJSONArray("legs");
                    //System.out.println(legs.length());
                    for (i = 0; i < legs.length(); i++) {
                        data = legs.getJSONObject(i);
                        //System.out.println(legs);
                        Iterator keys2 = data.keys();
                        while (keys2.hasNext()) {
                            System.out.println(keys2.next());

                        }
                        JSONArray steps = data.getJSONArray("steps");
                        for (int j = 0; j < steps.length(); j++) {
                            data1 = steps.getJSONObject(j);
                            //System.out.println(steps);

                            Iterator keys3 = data1.keys();
                            while (keys3.hasNext()) {
                                String key = (String) keys3.next();
                                if (key.equals("html_instructions")) {
                                    directions += "\n" + data1.getString(key);
                                    System.out.println(data1.getString(key));
                                }

                            }
                        }

                    }

                }
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return directions;
    }

    public String sendRequest(String absoluteURL) throws IOException {

        URL url = null;

        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(absoluteURL);

            urlConnection = (HttpURLConnection) url.openConnection();


            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));

            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return stringBuilder.toString();

    }
}
