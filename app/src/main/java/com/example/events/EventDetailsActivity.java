package com.example.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class EventDetailsActivity extends AppCompatActivity {

    TextView nameView, addingUserLoginView, localityView, addingPersonEmailView, notesView;
    ImageView imageView;
    Button editButton;
    Button deleteButton;

    Event event;


    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Intent intent = getIntent();

        String id = intent.getStringExtra("eventId");
        String name = intent.getStringExtra("eventName");
        String addingUserLogin = intent.getStringExtra("addingUserLogin");
        address = intent.getStringExtra("address");

        editButton = findViewById(R.id.editButton1);
        deleteButton = findViewById(R.id.deleteButton1);

        //Zdarzenie na kliknięcie w przycisk edycji
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Pobranie aktualnego użytkownika z SharedPreferences
                String eventAddingPersonMail = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userEmail", "");
                String personId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userId", "");

                if(eventAddingPersonMail.equals(addingUserLogin)){
                    Intent k = new Intent(EventDetailsActivity.this, EventEditActivity.class);
                    k.putExtra("eventId", id);
                    startActivityForResult(k, 1);
                    System.out.println("Użytkownik " + eventAddingPersonMail + " ma prawo do edycji");
                }
                else{
                    Toast.makeText(getApplicationContext(), "Możesz edytować tylko swoje wpisy!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pobranie aktualnego użytkownika z SharedPreferences
                String eventAddingPersonMail = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userEmail", "");
                String personId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userId", "");

                if(eventAddingPersonMail.equals(addingUserLogin)){
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
                                    DatabaseReference reference = rootNode.getReference("events");
                                    reference.child(id).child("address").setValue("");
                                    reference.child(id).child("latitude").setValue("");
                                    reference.child(id).child("longitude").setValue("");
                                    address = null;
                                    localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Lokalizacja:</b><br></font>" + address));
                                    System.out.println("Kliknieto YES");
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    System.out.println("Kliknieto NO");
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder .setMessage("Are you sure?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();

                    System.out.println("Użytkownik " + eventAddingPersonMail + " ma prawo do edycji");
                }
                else{
                    Toast.makeText(getApplicationContext(), "Możesz edytować tylko swoje wpisy!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Assign variables
        nameView = findViewById(R.id.eventName);
        addingUserLoginView = findViewById(R.id.AddingUserLogin);
        localityView = findViewById(R.id.eventLocality);
        imageView = findViewById(R.id.imageDetails);

        //Login dodającego
        nameView.setText(Html.fromHtml("<font color='#6200EE'><b>Nazwa zdarzenia:</b><br></font>" + name));

        //Lokalizacja
        localityView.setText(Html.fromHtml("<font color='#6200EE'><b>Lokalizacja:</b><br></font>" + address));

        //Login dodającego
        addingUserLoginView.setText(Html.fromHtml("<font color='#6200EE'><b>Dodane przez:</b><br></font>" + addingUserLogin));

        StorageReference gsReference = FirebaseStorage.getInstance().getReference().child(id);
        try {
            final File localFile = File.createTempFile("image", "jpg");
            gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    bitmap = AddEventActivity.rotateImage(bitmap, 90);
                    imageView.setImageBitmap(bitmap);
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
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                System.out.println("Wrocilo");
                Intent k = new Intent(EventDetailsActivity.this, EventsActivity.class);
                startActivity(k);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("Nie wrocilo");
            }
        }
    }


}