package com.example.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventsActivity extends AppCompatActivity {

    ListView listView;
    FloatingActionButton addButton;
    FloatingActionButton mapButton;

    Bitmap bitmap;
    StorageReference gsReference;

    static Event event;
    ArrayList<Event> dataModalArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context c = this.getApplicationContext();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_events);

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(EventsActivity.this, AddEventActivity.class);
                startActivityForResult(k, 1);
            }
        });

        mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(EventsActivity.this, MapsActivity.class);
                startActivity(k);
            }
        });

        setOnCreateData();
    }

    private void setOnCreateData(){
        //Przypisanie do elementu w XML
        listView = (ListView) findViewById(R.id.listView);

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


                        Address targetLocation = null;
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
                            targetLocation = addresses.get(0);
                        }

                        Object addDateTime = postSnapshot.child("addDateTime").getValue();
                        LocalDateTime ldt = getLocalDateTimeFromDatabaseObject(addDateTime);

                        Object person = postSnapshot.child("addedBy").getValue();
                        Person addedBy = getPersonFromDatabaseObject(person);

                        String note = postSnapshot.child("note").getValue().toString();
                        String compressedImageString = "";
                        try{
                            compressedImageString = postSnapshot.child("imageString").getValue().toString();
                        }catch(Exception e){

                        }
                        Bitmap image = StringToBitMap(compressedImageString);

                        Event event = new Event(id, name, targetLocation==null?null:targetLocation.getLatitude(), targetLocation==null?null:targetLocation.getLongitude(), addedBy, image, note, ldt, address, compressedImageString);

                        //DOdaje każdy obiekt event z bazy danych do listy eventList
                        eventList.add(event);


                        List<Event> orderedByDateList;

                        orderedByDateList = eventList.stream()
                                .sorted(Event::compareTo)
                                .collect(Collectors.toList());


                        ListAdapter adapter  = new ListAdapter(getApplicationContext(), R.layout.custom_adapter, orderedByDateList);
                        listView.setAdapter(adapter);

//                        //Referencja do bazy plików (zdjęć) FirebaseStorage
//                        gsReference = FirebaseStorage.getInstance().getReference().child(id);
//                        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                                bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//
//
//
//
//
//
//                            }
//
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                System.out.println("Nie znaleziono zdjęcia lub wystąpił błąd");
//                            }
//                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Event item = (Event) listView.getItemAtPosition(i);
                        Intent intent = new Intent(EventsActivity.this, EventDetailsActivity.class);

                        intent.putExtra("eventId", item.getId());
                        intent.putExtra("eventName", item.getName());
                        intent.putExtra("addingUserLogin", ""+item.getAddedBy().getEmailAddress());
                        intent.putExtra("latitude", item.getLatitude());
                        intent.putExtra("longitude", item.getLongitude());
                        intent.putExtra("address", ""+item.getAddress());
                        intent.putExtra("notes", ""+item.getNote());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    public static LocalDateTime getLocalDateTimeFromDatabaseObject(Object o){
        HashMap ds = (HashMap) o;
        int year = ((Long) ds.get("year")).intValue();
        int month = ((Long) ds.get("monthValue")).intValue();
        int day = ((Long) ds.get("dayOfMonth")).intValue();
        int hour = ((Long) ds.get("hour")).intValue();
        int minute = ((Long) ds.get("minute")).intValue();
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute);
        return ldt;
    }

    public static Person getPersonFromDatabaseObject(Object o){
        HashMap ds = (HashMap) o;
        System.out.println("aaaa" + ds);
        LocalDateTime addedDateTime = getLocalDateTimeFromDatabaseObject(ds.get("addDateTime"));
        String id = (String) ds.get("id");
        String mail = (String) ds.get("emailAddress");
        String fName = (String) ds.get("firstName");
        String lName = (String) ds.get("lastName");
        String pass = (String) ds.get("password");
        Person p = new Person(id, fName, lName, mail, pass, addedDateTime);
        return p;
    }

    public static Event getEventFromDatabaseObject(Object o, Context c){
        event = getEventData(o, c);
        return event;
    }

    public static Event getEventData(Object o, Context c){
        HashMap ds = (HashMap) o;
        String id = (String) ds.get("id");
        String name = (String) ds.get("name");

        Double latitude;
        Double longitude;
        String address = "";
        List<Address> addresses = null;
        if(!ds.get("latitude").equals("")){
            //Location
            latitude = (Double) ds.get("latitude");
            longitude = (Double) ds.get("longitude");

            Geocoder geocoder;

            geocoder = new Geocoder(c, Locale.getDefault());
            address = (String) ds.get("address");
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap persMap = (HashMap) ds.get("addedBy");
        Person addedBy = getPersonFromDatabaseObject(persMap);
        String note = (String) ds.get("note");
        String compressedImageString = (String) ds.get("imageString");
        LocalDateTime addDate = getLocalDateTimeFromDatabaseObject(ds.get("addDateTime"));

        Event event = new Event(id, name, addresses==null?null:addresses.get(0).getLatitude(), addresses==null?null:addresses.get(0).getLongitude(), addedBy, null, note, addDate, addresses==null?null:address, compressedImageString);

        return event;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK){
                setOnCreateData();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Do nothing
            }
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}