package com.project.mjazy.jaktamdojsc;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
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
import java.util.List;
import java.util.Observer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList markerPoints, observers, initialLocationArray, waypoint1Array, waypoint2Array,targetLocationArray;


    public void clearData(){
        mMap.clear();
        markerPoints.clear();
    }

    public void addObserver(ArrayList observer){
        observers.add(observer);
    }

    public void removeObserver(ArrayList observer){
        int index = observers.indexOf(observer);
        if (index >= 0){
            observers.remove(index);
        }
    }

    public void notifyObservers(){
        for (int index = 0; index < observers.size(); index++){
            ArrayList observer = (ArrayList)observers.get(index);
            observer.add(markerPoints.get(index));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        markerPoints = new ArrayList();
        observers = new ArrayList();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                clearData();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                // If there are already 4 markers clear map and ArrayList.
                if (markerPoints.size() == 4) {
                    clearData();
                }

                // Adding new item to the ArrayList
                markerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position of the marker
                markerOptions.position(point);

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(markerOptions);

                if (markerPoints.size() >= 1) {
                    if (markerPoints.size() == 4) {
                        notifyObservers();
                        LatLng firstPoint = (LatLng) markerPoints.get(0);
                        LatLng secondPoint = (LatLng) markerPoints.get(1);
                        LatLng thirdPoint = (LatLng) markerPoints.get(2);
                        LatLng fourthPoint = (LatLng) markerPoints.get(3);

                        // Add your own Api Key. Check https://github.com/akexorcist/Android-GoogleDirectionLibrary as for library used.
                        GoogleDirection.withServerKey("AIzaSyDukt_5HWdZiHf9s9AqiZaiZRK8wHQcTH4").from(firstPoint).and(secondPoint).and(thirdPoint).to(fourthPoint).transportMode(TransportMode.WALKING).execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.isOK()) {
                                    Route route = direction.getRouteList().get(0);
                                    int legCount = route.getLegList().size();
                                    for (int index = 0; index < legCount; index++) {
                                        Leg leg = route.getLegList().get(index);
                                        mMap.addMarker(new MarkerOptions().position(leg.getStartLocation().getCoordination()));
                                        if (index == legCount - 1) {
                                            mMap.addMarker(new MarkerOptions().position(leg.getEndLocation().getCoordination()));
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
                }

            }
        });
    }

}
