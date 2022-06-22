package com.example.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = "dfw4";
    private static final String PREF_USER_NAME = "unlogged";
    private static final String PREF_ID = "unlogged";
    Button btLocation;
    TextView addressView, countryView, localityView ;
    TextInputEditText nameView, notesView;
    FusedLocationProviderClient fusedLocationProviderClient;

    Button submit;
    Bitmap bitmap;
    Address location;

    Button takePicture;
    ImageView imageView;

    Uri image_uri;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    FirebaseStorage storage;


    FirebaseFirestore db;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //Assign variables
       btLocation = findViewById(R.id.bt_location);
        nameView = findViewById(R.id.addName);
        addressView = findViewById(R.id.addAddress);
        countryView = findViewById(R.id.addCountry);
        localityView = findViewById(R.id.addLocality);
        notesView = findViewById(R.id.addNotes);
        submit = findViewById(R.id.addButton);

        rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
        reference = rootNode.getReference("events");

        //init fusedLocation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(localityView.getText().toString().length() < 2){
                    Toast.makeText(getApplicationContext(), "Nie można dodać zdarzenia bez pobrania lokalizacji", Toast.LENGTH_SHORT).show();
                }
                else {
                    String id = generateRandomString();
                    String eventName = nameView.getText().toString();
                    String eventAddress = addressView.getText().toString();
                    String eventNotes = notesView.getText().toString();
                    String eventAddingPersonMail = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userEmail", "");
                    String personId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userId", "");

                    DatabaseReference usersReference = rootNode.getReference("users");
                    usersReference.child(personId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String fn = "";
                            String ln = "";
                            fn = task.getResult().child("firstName").getValue().toString();
                            ln = task.getResult().child("lastName").getValue().toString();
                            Person p = new Person(personId, fn, ln, eventAddingPersonMail, "fff", LocalDateTime.now());
                            FirebaseAuth.getInstance().signInAnonymously();
                            storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            StorageReference mountainsRef = storageRef.child(id);
                            // Create a reference to 'images/mountains.jpg'
                            StorageReference mountainImagesRef = storageRef.child(image_uri.getPath());

                            // While the file names are the same, the references point to different files
                            mountainsRef.getName().equals(mountainImagesRef.getName());    // true
                            mountainsRef.getPath().equals(mountainImagesRef.getPath());

                            mountainsRef.putFile(image_uri);


                            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            String compressedImageString = new ByteArrayOutputStream(8192).toByteArray().toString();

                            Event event = new Event(id, eventName, location.getLatitude(), location.getLongitude(), p, null, eventNotes, LocalDateTime.now(), eventAddress, compressedImageString);

                            reference.child(id).setValue(event);
                            reference.orderByChild(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        System.out.println(ds);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            Toast.makeText(getApplicationContext(), "Zdarzenie zostało dodane", Toast.LENGTH_SHORT).show();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result",1);
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        }
                    });




                }

            }

            private ContentResolver getContentResolverr() {
                return getContentResolver();
            }
        });

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check permission
                if(ActivityCompat.checkSelfPermission(AddEventActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //If granted
                    System.out.println("1");
                    getLocation();
                }
                else{
                    System.out.println("2");
                    ActivityCompat.requestPermissions(AddEventActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }

            private void getLocation() {
                System.out.println("3");

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                getLastBestLocation(locationManager);

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        //Init location
                        Location currentLocation = task.getResult();
                        if(currentLocation != null){
                            System.out.println("6");
                            try {
                                System.out.println("4");
                                //Init geoCoder
                                Geocoder geocoder = new Geocoder(AddEventActivity.this, Locale.getDefault());

                                //init address list
                                List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

                                location = addresses.get(0);

                                //Set address
                                addressView.setText(Html.fromHtml("<font color='#6200EE'><b>Address :</b><br></font>"+ addresses.get(0).getAddressLine(0)));
                                countryView.setText(Html.fromHtml("<font color='#6200EE'><b>Country :</b><br></font>" + addresses.get(0).getCountryName()));
                                localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Locality :</b><br></font>" + addresses.get(0).getLocality()));
                            } catch (IOException e){
                                e.printStackTrace();
                            }

                        }
                        else{
                            System.out.println("Location is null");
                        }
                    }
                });
            }
        });
        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.imageButton);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        //brak dostepu, zglos prosbe
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //pokaż pop-up
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        //są już zgody
                        openCamera();
                    }
                }
                else{
                    openCamera();

                }
            }
        });

    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nowe zdjęcie");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Z kamery");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);

    }

    //obsługa zgód
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    //zgoda z pop-up została odrzucona
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            imageView.setImageURI(image_uri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        System.out.println(generatedString);
        return generatedString;
    }
    private Location getLastBestLocation(LocationManager mLocationManager) {
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }
}

