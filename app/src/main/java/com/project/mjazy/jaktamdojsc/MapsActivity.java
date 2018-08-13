package com.project.mjazy.jaktamdojsc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList markerPoints, distanceValues;
    private TextView distanceTextView;

    public void addMarker(LatLng point){
        // Adding new item to the ArrayList
        markerPoints.add(point);

        // Creating MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position of the marker
        markerOptions.position(point);

        if (markerPoints.size() == 1){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        else
        {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Add new marker to the Google Map Android API V2
        mMap.addMarker(markerOptions);

        if (markerPoints.size() > 1){
            shouldRouteBeDrawn();
        }
    }

    public void shouldRouteBeDrawn(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you wish to add another marker?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        drawRoute();
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }
    public void drawRoute() {
        LatLng startPoint = (LatLng) markerPoints.get(0);
        LatLng endPoint = (LatLng) markerPoints.get(markerPoints.size() - 1);
        markerPoints.remove(0);
        markerPoints.remove(markerPoints.size() - 1);

        // Add your own Api Key. Check https://github.com/akexorcist/Android-GoogleDirectionLibrary as for library used.
        GoogleDirection.withServerKey("AIzaSyDukt_5HWdZiHf9s9AqiZaiZRK8wHQcTH4").from(startPoint).and(markerPoints).to(endPoint).transportMode(TransportMode.WALKING).execute(new DirectionCallback() {
            @Override
            public void onDirectionSuccess(Direction direction, String rawBody) {
                if (direction.isOK()) {
                    Route route = direction.getRouteList().get(0);
                    int legCount = route.getLegList().size();
                    Info distanceInfo;
                    float distanceFloat = 0;
                    for (int index = 0; index < legCount; index++) {
                        Leg leg = route.getLegList().get(index);
                        distanceInfo = leg.getDistance();
                        String distanceValue = distanceInfo.getValue();
                        distanceFloat += Float.parseFloat(distanceValue);
                        distanceValues.add(distanceFloat);
                        if (index == legCount - 1) {
                            distanceFloat = distanceFloat / 1000;
                            distanceTextView.setText(distanceFloat + " km");
                        }
                        List<Step> stepList = leg.getStepList();
                        ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.RED, 3, Color.BLUE);
                        for (PolylineOptions polylineOption : polylineOptionList) {
                            mMap.addPolyline(polylineOption);
                        }
                    }
                } else {
                    // Do something
                }
            }

            @Override
            public void onDirectionFailure(Throwable t) {
                // Do something
            }
        });
    }

    public void clearData(){
        mMap.clear();
        markerPoints.clear();
        distanceTextView.setText("Select your your destination");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        markerPoints = new ArrayList();
        distanceValues = new ArrayList();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button button = (Button) findViewById(R.id.button);
        distanceTextView = (TextView) findViewById(R.id.distanceView);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                clearData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                addMarker(point);

            }
        });
    }

}
