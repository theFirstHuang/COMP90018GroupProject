package com.thefirsthuang.authapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String currentImageUrl; //for display current user image

    private String userID;

    private Button logout;
    private Button refresh;

    private ImageView profileImage;
    private Button editProfile;

    private String defaultURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logout = (Button) findViewById(R.id.signOut);
        editProfile = (Button) findViewById(R.id.editProfile);

        final TextView fullNameProfile = (TextView) findViewById(R.id.fullNameProfile);
        final TextView emailProfile = (TextView) findViewById(R.id.emailProfile);
        final TextView ageProfile = (TextView) findViewById(R.id.ageProfile);

        profileImage = findViewById(R.id.profileImage);

        //fetch user details from DB to displayed on profile
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        //fetch user image from fireBase storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("Users");

        //catch the flag send by EditProfileActivity to refresh page
        String flag = getIntent().getStringExtra("flag");
        if(flag != null && flag.equals("true")) {
            //refresh page
            refresh();
        }


        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    String fullName = userProfile.fullName;
                    String email = userProfile.email;
                    String age = userProfile.age;
                    currentImageUrl = userProfile.photoUrl;


                    fullNameProfile.setText(fullName);
                    emailProfile.setText(email);
                    ageProfile.setText(age);
                    if (currentImageUrl != null) {
                        Picasso.get().load(currentImageUrl).into(profileImage);
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this,"Error", Toast.LENGTH_LONG).show();
            }

        });





        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                Toast.makeText(ProfileActivity.this,"Successfully logout!", Toast.LENGTH_LONG).show();
            }
        });



        //for user image update(upload)
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));

            }
        });




    }

    private void refresh() {
        Intent i = new Intent(ProfileActivity.this, ProfileActivity.class);
        finish();
        //delay 2.5 seconds to refresh
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 2500);
        System.out.println("REFRESH!!!");
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }


}

