package com.kalvin.theattic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail, loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private Button loginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //initialize the views
        loginBtn = findViewById(R.id.loginBtn);
        loginEmail = findViewById(R.id.login_email);
        loginPass = findViewById(R.id.login_password);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,"PROCESSING...",
                        Toast.LENGTH_LONG).show();
                String email = loginEmail.getText().toString().trim();
                String password = loginPass.getText().toString().trim();

                if (!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)){
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                checkUserExistence();
                            }else{
                                Toast.makeText(LoginActivity.this,"Couldn't login,user not found",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this,"Complete all fields",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //check if user exists
    private void checkUserExistence() {
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.hasChild(user_id)){
                Intent mainPage = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(mainPage);
            }else {
                Toast.makeText(LoginActivity.this,"User not registered!",
                        Toast.LENGTH_SHORT).show();
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
