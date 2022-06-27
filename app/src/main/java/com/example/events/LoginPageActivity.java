package com.example.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;

public class LoginPageActivity extends AppCompatActivity {

    private static final String PREF_USER_NAME = "unlogged";
    private static final String PREF_ID = "unlogged";

    ImageView google_img;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    TextView email, password;
    Button loginButton;
    Button registration;

    static FirebaseDatabase rootNode;
    static DatabaseReference reference;

    Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        google_img = findViewById(R.id.google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        registration = findViewById(R.id.button_register_main);

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(LoginPageActivity.this, FormularzRejestracyjny.class);
                startActivity(k);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
                reference = rootNode.getReference("users");
                Query query = reference.orderByChild("emailAddress").equalTo(email.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String email = dataSnapshot.getChildren().iterator().next().child("emailAddress").getValue().toString();
                            String firstName = dataSnapshot.getChildren().iterator().next().child("firstName").getValue().toString();
                            String lastName = dataSnapshot.getChildren().iterator().next().child("lastName").getValue().toString();
                            String password = dataSnapshot.getChildren().iterator().next().child("password").getValue().toString();
                            String id = dataSnapshot.getChildren().iterator().next().child("id").getValue().toString();
                            LocalDateTime addedDate = LocalDateTime.now();
                            System.out.println("Są dzieciaczki");
                            person = new Person(id, firstName, lastName, email, password, addedDate);




                        } else {
                            System.out.println("Brak dzieci");
                            //Do nothing, p remains null
                        }

                        if(person == null || !person.password.equals(password.getText().toString())){
                            Toast.makeText(getApplicationContext(), "Błędny login lub hasło", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Zalogowałeś się, zioooom!", Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                            editor.putString("userEmail", person.getEmailAddress());
                            editor.putString("userId", person.getId());
                            editor.commit();

                            Intent k = new Intent(LoginPageActivity.this, EventsActivity.class);
                            startActivity(k);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        gsc = GoogleSignIn.getClient(this, gso);

        google_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });
    }

    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                HomeActivity();
            } catch (ApiException e) {
                Toast.makeText(this, "Nie masz dostępu do internetu lub Konto nie jest zarejestrowane. Użyj formularza rejestracyjnego.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void HomeActivity() {
        //finish();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            rootNode = FirebaseDatabase.getInstance("https://authproject-b0bfc-default-rtdb.europe-west1.firebasedatabase.app");
            reference = rootNode.getReference("users");
            Query query = reference.orderByChild("emailAddress").equalTo(account.getEmail());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.getChildren().iterator().next().child("emailAddress").getValue().toString();
                        String id = dataSnapshot.getChildren().iterator().next().child("id").getValue().toString();
                        String firstName = dataSnapshot.getChildren().iterator().next().child("firstName").getValue().toString();
                        String lastName = dataSnapshot.getChildren().iterator().next().child("lastName").getValue().toString();
                        String password = dataSnapshot.getChildren().iterator().next().child("password").getValue().toString();
                        LocalDateTime addedDate = LocalDateTime.now();
                        System.out.println("Są dzieciaczki");
                        person = new Person(FormularzRejestracyjny.generateRandomString(), firstName, lastName, email, password, addedDate);

                        System.out.println("Mail to: " + email + ", id to: " + id);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString("userEmail", email);
                        editor.putString("userId", id);
                        editor.commit();

                    } else {
                        System.out.println("Brak dzieci");
                        //Do nothing, p remains null
                    }

                    if(person == null){
                        Toast.makeText(getApplicationContext(), "Brak zarejestrowanego konta " +account.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Zalogowałeś się, zioooom!", Toast.LENGTH_SHORT).show();
                        Intent k = new Intent(LoginPageActivity.this, EventsActivity.class);
                        startActivity(k);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
        }
    }
}