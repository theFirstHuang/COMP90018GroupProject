package com.thefirsthuang.authapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference dbReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri updateImageUri; //for selected image Uri in gallery

    private String userID;
    private String currentName;
    private String newName;
    private String currentImageUrl;//for display current user image
    private String newImageUrl;

    private EditText fullNameProfileEdit;
    private Button updateProfileEdit;
    private ImageView photoProfileEdit;
    private ProgressBar progressBarProfileEdit;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fullNameProfileEdit = findViewById(R.id.fullNameProfileEdit);
        updateProfileEdit = findViewById(R.id.updateProfileEdit);
        photoProfileEdit = findViewById(R.id.photoProfileEdit);
        progressBarProfileEdit = findViewById(R.id.progressBarProfileEdit);

        //fetch/upload user image from fireBase storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("Users");

        //fetch user details from DB to displayed on profile
        user = FirebaseAuth.getInstance().getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        //fetch user profile
        dbReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    currentName = userProfile.fullName;
                    fullNameProfileEdit.setText(currentName);
                    currentImageUrl = userProfile.photoUrl;

                    if (currentImageUrl != null) {
                        Picasso.get().load(currentImageUrl).into(photoProfileEdit);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this,"Error", Toast.LENGTH_LONG).show();
            }
        });


        photoProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        updateProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
             
            }
        });
    }

    //helper function for uploadFile
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //update user info and update into user's
    private void uploadFile() {
        progressBarProfileEdit.setVisibility(View.VISIBLE);
        newName = fullNameProfileEdit.getText().toString().trim();

        if (updateImageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(updateImageUri));

            //upload into firebase storage
            fileReference.putFile(updateImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(EditProfileActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

                            //get the download url of the photo
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for fileReference
                                    newImageUrl = uri.toString();
                                    //find user info in DB and update Db
                                    updateDB(0);
                                    progressBarProfileEdit.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    progressBarProfileEdit.setVisibility(View.GONE);
                                    Toast.makeText(EditProfileActivity.this, "User profile not update cause by URL issue", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //back to profile page, send a flag to profileActivity
                            Intent i = new Intent(EditProfileActivity.this, ProfileActivity.class);
                            i.putExtra("flag", "true");
                            startActivity(i);
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarProfileEdit.setVisibility(View.GONE);
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else if (!newName.equals(currentName)) {
            updateDB(1);
            progressBarProfileEdit.setVisibility(View.GONE);
            Toast.makeText(this, "Username changed", Toast.LENGTH_SHORT).show();
            //back to profile page, send a flag to profileActivity
            Intent i = new Intent(EditProfileActivity.this, ProfileActivity.class);
            i.putExtra("flag", "true");
            startActivity(i);
        }else {
            progressBarProfileEdit.setVisibility(View.GONE);
            Toast.makeText(this, "Nothing changed", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==1 && resultCode == RESULT_OK && data!= null && data.getData()!=null){
            updateImageUri = data.getData();
            photoProfileEdit.setImageURI(updateImageUri);

            Picasso.get().load(updateImageUri).into(photoProfileEdit);
        }
    }

    private void updateDB(int flag) {
        HashMap User = new HashMap();
        //don't update URL if no update of URL
        //using hashMap
        if (flag == 1) {    //fullName
            User.put("fullName", newName);
        } else {    //url
            User.put("fullName", newName);
            User.put("photoUrl",newImageUrl);
        }


        dbReference.child(userID).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "User profile not update cause by DB issue", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}