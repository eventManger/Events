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
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
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

public class EventEditActivity extends AppCompatActivity {

//    private static final String TAG = "dfw4";
//    private static final String PREF_USER_NAME = "unlogged";
//    private static final String PREF_ID = "unlogged";
    Button btLocation;
    TextView addressView, countryView, localityView ;
    TextInputEditText nameView, notesView;
    FusedLocationProviderClient fusedLocationProviderClient;

    Button submit;

    Address location;

    Button takePicture;
    ImageView imageView;

    Uri image_uri;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    FirebaseStorage storage;
    Event event;

    //FirebaseFirestore db;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        Intent intent = getIntent();
        String eventId =  intent.getStringExtra("eventId");

        event = null;

        //Assign variables
        btLocation = findViewById(R.id.editLocationButton);
        nameView = findViewById(R.id.editName);
        addressView = findViewById(R.id.editAddress);
        countryView = findViewById(R.id.editCountry);
        localityView = findViewById(R.id.editLocality);
        notesView = findViewById(R.id.editNotes);
        submit = findViewById(R.id.editButton);
        imageView = findViewById(R.id.EditImageView);
        takePicture = findViewById(R.id.editImageButton);

        //Baza plików dla events
        rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
        reference = rootNode.getReference("events");
        Query query = reference.orderByChild("id").equalTo(eventId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Jeśli istnieje event o danym ID
                if (dataSnapshot.exists()) {
                    try {
                        final File localFile = File.createTempFile("image", "jpg");
                        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child(eventId);
                        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                event = EventsActivity.getEventFromDatabaseObject(dataSnapshot.child(eventId).getValue(), getApplicationContext());
                                event.setImage(BitmapFactory.decodeFile(localFile.getAbsolutePath()));

                                //Uzupełniam pola danymi z obiektu Event
                                nameView.setText(event.getName());
                                notesView.setText(event.getNote());

                                //Set address
                                if(event.getLatitude() == null || event.getLatitude().equals("")){

                                }
                                else{
                                    Geocoder geocoder = new Geocoder(EventEditActivity.this, Locale.getDefault());
                                    List<Address> addresses = null;
                                    try {
                                        addresses = geocoder.getFromLocation(event.getLatitude(), event.getLongitude(), 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    location = addresses.get(0);

                                    addressView.setText(Html.fromHtml("<font color='#6200EE'><b>Address :</b><br></font>"+ location.getAddressLine(0)));
                                    countryView.setText(Html.fromHtml("<font color='#6200EE'><b>Country :</b><br></font>"+ location.getCountryName()));
                                    localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Locality :</b><br></font>"+ location.getLocality()));
                                    localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Added by :</b><br></font>" + event.getAddedBy().getEmailAddress()));

                                }

                                imageView.setImageBitmap(event.getImage());

                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                event.getImage().compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), event.getImage(), "Title", null);
                                image_uri = Uri.parse(path);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "No picture found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    //Do nothing, p remains null
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Do nothing
            }
        });

        //init fusedLocation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(localityView.getText().toString().length() < 2){
                    Toast.makeText(getApplicationContext(), "Nie można dodać zdarzenia bez pobrania lokalizacji", Toast.LENGTH_SHORT).show();
                }
                else {
                    String id = eventId;
                    String eventName = nameView.getText().toString();
                    String eventAddress = addressView.getText().toString();
                    String eventNotes = notesView.getText().toString();
                    String eventAddingPersonMail = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userEmail", "");
                    String personId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userId", "");

                    //Firebase database - users
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
                            StorageReference imageDatabase = storage.getReference();

                            //Aktualizacja wpisu o nazwie takiej jak ID zdarzenia
                            StorageReference objectReference = imageDatabase.child(id);
                           // StorageReference mountainImagesRef = imageDatabase.child(image_uri.getPath());

                            //Umieszcza plik w bazie plików
                            objectReference.putFile(image_uri);

                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String imageString = BitMapToString(bitmap);


                            //Stwórz obiekt event aby zaktualizować go w bazie po ID
                            Event event = new Event(id,
                                    eventName,
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    p,
                                    null,
                                    eventNotes,
                                    LocalDateTime.now(),
                                    eventAddress,
                                    imageString
                            );

                            reference.child(id).setValue(event);
                            Toast.makeText(getApplicationContext(), "Zdarzenie zostało zaktualizowane", Toast.LENGTH_SHORT).show();
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
                if(ActivityCompat.checkSelfPermission(EventEditActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //If granted
                    getLocation();
                }
                else{
                    ActivityCompat.requestPermissions(EventEditActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }

            private void getLocation() {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(getLastBestLocation(locationManager) != null){
                            try {
                                //Init geoCoder
                                Geocoder geocoder = new Geocoder(EventEditActivity.this, Locale.getDefault());

                                //init address list
                                if(location == null){
                                    Location l = getLastBestLocation(locationManager);
                                    List<Address> addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
                                    location = addresses.get(0);
                                    //Set address
                                    addressView.setText(Html.fromHtml("<font color='#6200EE'><b>Address :</b><br></font>"+ addresses.get(0).getAddressLine(0)));
                                    countryView.setText(Html.fromHtml("<font color='#6200EE'><b>Country :</b><br></font>" + addresses.get(0).getCountryName()));
                                    localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Locality :</b><br></font>" + addresses.get(0).getLocality()));

                                }

                                } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Nie znaleziono ostatniej lokalizacji.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

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

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,2, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}