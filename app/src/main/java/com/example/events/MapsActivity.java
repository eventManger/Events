package com.example.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.events.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Skopiowane ze strony https://developers.google.com/maps/documentation/android-sdk/start
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Deklaracja root bazy danych
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");

        //Deklaracja drzewa events
        DatabaseReference reference = rootNode.getReference("events");

        //Definicja kontenera, gdzie będą elementy typu Event
        List<Event> eventList = new ArrayList<>();

        //Listener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventList.clear();
                //Pętla po elementach w bazie danych po events
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    try {
                        File localFile = File.createTempFile("image", "jpg");

                        //Id eventu jest ten sam co id zdjęcia
                        String id = postSnapshot.getKey();

                        String name = postSnapshot.child("name").getValue().toString();
                        String address = (String) postSnapshot.child("address").getValue().toString();

                        Double latitude = null;
                        Double longitude = null;
                        if(!postSnapshot.child("latitude").getValue().equals("")){
                            latitude = (Double) postSnapshot.child("latitude").getValue();
                            longitude = (Double) postSnapshot.child("longitude").getValue();

                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            LatLng ltlng = new LatLng(latitude, longitude);
                            mMap.addMarker(new MarkerOptions().position(ltlng).title(name + ", " + address));

                            Circle circle = mMap.addCircle(new CircleOptions()
                                    .center(ltlng)
                                    .radius(1000)
                                    .strokeColor(Color.RED)
                                    .fillColor(0x220000FF)
                                    .strokeWidth(5));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });

        //get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        Location targetLocation = null;
        if(locationGPS == null) targetLocation = locationNet;
        else targetLocation = locationGPS;


        LatLng currentLocation = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo( 13 ));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(locationNet.getLatitude(),
                        locationNet.getLongitude()), 13));
    }
}