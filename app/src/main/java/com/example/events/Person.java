package com.example.events;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Person {
    private static Person p;
    String id;
    String firstName;
    String lastName;
    String emailAddress;
    String password;
    LocalDateTime addDateTime;


    static FirebaseDatabase rootNode;
    static DatabaseReference reference;

    public Person(String id, String firstName, String lastName, String emailAddress, String password, LocalDateTime addDateTime) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.addDateTime = addDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getAddDateTime() {
        return addDateTime;
    }

    public void setAddDateTime(LocalDateTime addDateTime) {
        this.addDateTime = addDateTime;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static Person getPersonByEmail(String email){
        rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
        reference = rootNode.getReference("users");
        p = null;

        Query query = reference.orderByChild("emailAddress").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.getChildren().iterator().next().child("emailAddress").getValue().toString();
                    String firstName = dataSnapshot.getChildren().iterator().next().child("firstName").getValue().toString();
                    String lastName = dataSnapshot.getChildren().iterator().next().child("lastName").getValue().toString();
                    String password = dataSnapshot.getChildren().iterator().next().child("password").getValue().toString();
                    LocalDateTime addedDate = LocalDateTime.now();
                    System.out.println("Są dzieciaczki");
                    p = new Person(FormularzRejestracyjny.generateRandomString(), firstName, lastName, email, password, addedDate);

                } else {
                    System.out.println("Brak dzieci");
                    //Do nothing, p remains null
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return p;
    }
}
