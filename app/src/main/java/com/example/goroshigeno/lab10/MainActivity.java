package com.example.goroshigeno.lab10;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button search = (Button)findViewById(R.id.search);
        final Button change = (Button)findViewById(R.id.change);
        final EditText local = (EditText) findViewById(R.id.local) ;
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                search.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        search(googleMap,local.getText().toString());
                    }
                });
                change.setOnClickListener(new View.OnClickListener(){
                    boolean a=true;
                    @Override
                    public void onClick(View view){

                        googleMap.clear();
                        if(a){
                            display_filter(googleMap);
                        }
                        else display(googleMap);
                        a=!a;


                    }
                });
                display(googleMap);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.033739,121.527886), 11));



            }
        });

    }
    private void search(GoogleMap googleMap,String local){

        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> addressList = null;
        int maxResults = 1;
        try {
            addressList = geocoder
                    .getFromLocationName(local, maxResults);
        } catch (IOException e) {
            Log.e("GeocoderActivity", e.toString());
        }

        if(addressList == null || addressList.isEmpty()){
            Toast.makeText(getBaseContext(), "沒有找到這個地點" ,Toast.LENGTH_SHORT).show();
        }
        else{
            Address address = addressList.get(0);
            LatLng position = new LatLng(address.getLatitude(),address.getLongitude());
            String snippet = address.getAddressLine(0);
            googleMap.addMarker(new MarkerOptions().position(position).title(local).snippet(snippet));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(15).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void display(final GoogleMap googleMap){
        String urlParkingArea = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=a880adf3-d574-430a-8e29-3192a41897a5";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response = " + response.toString());

                        try {

                            JSONArray data = response.getJSONObject("result").getJSONArray("results");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject o = data.getJSONObject(i);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(o.getDouble("緯度(WGS84)"), o.getDouble("經度(WGS84)")))
                                        .title(o.getString("停車場名稱"))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.images))
                                );
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "error : " + error.toString());
                    }
                }
        );
        Volley.newRequestQueue(MainActivity.this).add(jsonObjectRequest);
    }
    private void display_filter(final GoogleMap googleMap){
        String urlParkingArea = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=a880adf3-d574-430a-8e29-3192a41897a5";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response = " + response.toString());

                        try {

                            JSONArray data = response.getJSONObject("result").getJSONArray("results");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject o = data.getJSONObject(i);
                                MarkerOptions m1 = new MarkerOptions();
                                m1.position(new LatLng(o.getDouble("緯度(WGS84)"), o.getDouble("經度(WGS84)")));
                                m1.title(o.getString("停車場名稱"));
                                m1.draggable(true);
                                if(o.getString("停車場名稱").contains("機車")){
                                    m1.icon(BitmapDescriptorFactory.fromResource(R.drawable.scooter));
                                }
                                else if (o.getString("停車場名稱").contains("自行車")){
                                    m1.icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle));
                                }
                                else  m1.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));

                                googleMap.addMarker(m1);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "error : " + error.toString());
                    }
                }
        );
        Volley.newRequestQueue(MainActivity.this).add(jsonObjectRequest);


    }
}
