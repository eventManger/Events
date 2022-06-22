package com.example.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class FormularzRejestracyjny extends AppCompatActivity {

    //Pola formularza java
    TextView name, surname, mail, password1, password2;
    Button submit;



    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formularz_rejestracyjny);

        //spinanie elementów
        name = findViewById(R.id.input_first_name);
        surname = findViewById(R.id.input_last_name);
        mail = findViewById(R.id.input_email);
        password1 = findViewById(R.id.input_password1);
        password2 = findViewById(R.id.input_password_2);
        submit = findViewById(R.id.register);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().length() > 2 && surname.getText().length() > 2 && mail.getText().length() > 2 && password1.getText().toString().equals(password2.getText().toString())) {

                    rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
                    reference = rootNode.getReference("users");

                    Query query = reference.orderByChild("emailAddress").equalTo(mail.getText().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(getApplicationContext(), "Taki mail już istnieje w bazie", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                System.out.println("Not exists");

                                String id = generateRandomString();
                                String firstName = name.getText().toString();
                                String lastName = surname.getText().toString();
                                String email = mail.getText().toString();
                                String pass1 = password1.getText().toString();
                                String pass2 = password2.getText().toString();
                                LocalDateTime addDateTime = LocalDateTime.now();
                                Person person = new Person(id, firstName, lastName, email, pass1, LocalDateTime.now());

                                //Zapis do bazy danych nowego użytkownika zarejestrowanego
                                reference.child(id).setValue(person);

                                Toast.makeText(getApplicationContext(), "Konto zostało zarejestrowane", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("Wystąpił błąd: " + databaseError);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Niepełne dane lub niezgodne hasła", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        return generatedString;
    }
}