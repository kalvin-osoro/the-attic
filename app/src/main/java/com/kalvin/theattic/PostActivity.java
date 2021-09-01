package com.kalvin.theattic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private EditText textTitle;
    private EditText textDesc;
    private Button postBtn;

    private StorageReference mStorageRef;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;

    private static final int GALLERY_REQUST_CODE = 2;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        postBtn = findViewById(R.id.postBtn);
        textDesc = findViewById(R.id.textDesc);
        textTitle = findViewById(R.id.textTitle);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        imageButton = findViewById(R.id.imgBtn);
        //picking image from gallery
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUST_CODE);
            }
        });
        //posting to Firebase
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostActivity.this,"POSTING...",Toast.LENGTH_LONG).show();
                final String postTitle = textTitle.getText().toString().trim();
                final String postDesc = textDesc.getText().toString().trim();
                //get the date and time of the post
                java.util.Calendar calendar = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                final String saveCurrentDate = currentDate.format(calendar.getTime());

                java.util.Calendar calendar1 = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                final String saveCurrentTime = currentTime.format(calendar1.getTime());
                //do a check for empty fields
                if (!TextUtils.isEmpty(postDesc)&& !TextUtils.isEmpty(postTitle)){
                    StorageReference filepath = mStorageRef.child("post_images").child(uri.getLastPathSegment());

                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload of the post image was successful get the download url
                            if (taskSnapshot.getMetadata()!=null){
                                if (taskSnapshot.getMetadata().getReference()!=null){
                                    //get the download Url for your storage use the methods getStorage() and getDownloadUrl()
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    //call the merhod addOnSuccessListener to determine if we got the download url
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //convert the uri to a string on success
                                            final String imageUrl = uri.toString();

                                            Toast.makeText(getApplicationContext(),"Successfully Uploaded",
                                                    Toast.LENGTH_SHORT).show();
                                            final DatabaseReference newPost = databaseRef.push();
                                            //adding post contents to database reference,
                                            //call addValueListener so as to set the values
                                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    newPost.child("title").setValue(postTitle);
                                                    newPost.child("desc").setValue(postDesc);
                                                    newPost.child("postImage").setValue(imageUrl);
                                                    newPost.child("uID").setValue(mCurrentUser.getUid());
                                                    newPost.child("time").setValue(saveCurrentTime);
                                                    newPost.child("date").setValue(saveCurrentDate);
                                                    newPost.child("profilePhoto").setValue(snapshot.child("profilePhoto").getValue());
                                                    newPost.child("displayName").setValue(snapshot.child("displayName").getValue())
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        //Launch the mainActivity after posting
                                                                        Intent intent = new Intent(PostActivity.this,MainActivity.class);
                                                                        startActivity(intent);
                                                                    }

                                                                }
                                                            });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    //image from gallery result
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUST_CODE && resultCode == RESULT_OK){
            //get the image selected b the user
            uri = data.getData();
            //set the image
            imageButton.setImageURI(uri);
        }
    }
}
