package com.kalvin.theattic;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kalvin.theattic.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference likesRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Boolean likeChecher = false;
    private FirebaseRecyclerAdapter adapter;
    String currentUserID = null;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Initialize recyclerview
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Reverse the layout so as to display the most recent post at the top
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        //Initialize the databaseReference where you will store likes
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        //get currently logged in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            //if user is not logged in refer him/her to the register activity
            Intent loginIntent = new Intent(MainActivity.this,RegisterActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //check to see if the user is logged in
        if (currentUser !=null){
            //if the user is logged in populate the Ui with card views
            updateUI(currentUser);
            //Listen to the events on the adapter
            adapter.startListening();
        }
    }

    private void updateUI(final FirebaseUser currentUser) {
        //Create and initialize an instance of Query that retrieves all posts uploaded
        Query query = FirebaseDatabase.getInstance().getReference().child("Posts");
        //Create and initialize an instance of Recycler Options passing in your model class
        FirebaseRecyclerOptions<Attic> options = new FirebaseRecyclerOptions.Builder<Attic>().setQuery(query,
                new SnapshotParser<Attic>() {
                    @NonNull
                    @Override
                    //Create a snapshot of your model

                    public Attic parseSnapshot(@NonNull DataSnapshot snapshot) {

                            return new Attic(snapshot.child("title").getValue().toString(),
                                    snapshot.child("desc").getValue().toString(),
                                    snapshot.child("postImage").getValue().toString(),
                                    snapshot.child("displayName").getValue().toString(),
                                    snapshot.child("profilePhoto").getValue().toString(),
                                    snapshot.child("time").getValue().toString(),
                                    snapshot.child("date").getValue().toString());


                    }
                }).build();
        //create a firebase adapter passing in the model, and a view holder
        //create a new viewHolder as a public inner class that extends RecyclerView.Holder,
        //outside the create, start and update the Ui methods.
        //Then implement the methods onCreateViewHolder and onBondViewHolder
        //complete all teh steps in the AtticViewHolder before proceeding to the methods
        //onCreateViewHolder, and onBindViewHolder
        adapter = new FirebaseRecyclerAdapter<Attic,AtticViewHolder>(options) {

            @NonNull
            @Override
            public AtticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflate the layout where you have the card view items
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_items,parent,false);
                return new AtticViewHolder(MainActivity.this, view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AtticViewHolder holder, int position, @NonNull Attic model) {
                //very important for you to get the post key since we will use this to set likes and
                //delete a particular post
                final String post_key = getRef(position).getKey();
                //populate the card views with data
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDesc());
                holder.setPostImage(getApplicationContext(),model.getPostImage());
                holder.setUserName(model.getDisplayName());
                holder.setProfilePhoto(getApplicationContext(),model.getProfilePhoto());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                //set a like on a particular post
                holder.setLikeButtonStatus(post_key);
                //add onClickListener on a particuar post to allow opening this post on a different screen
                holder.post_layout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //Launch the screen single post activity on clicking a particular cardview item
                        //create this activity using the empty activity template
                        Intent singleActivity = new Intent(MainActivity.this,SinglePostActivity.class);
                        singleActivity.putExtra("postID",post_key);
                        startActivity(singleActivity);

                    }
                });
                //set the onClickListener on the button for liking a post
                holder.likePostButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //initialize the like checker to true, we are usint this boolean variable to determine
                        //if a post has been liked or disliked
                        //we declared this variable on to of our activity class
                        likeChecher = true;
                        //check the currently logged in user using his/her ID
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null){
                            currentUserID = user.getUid();
                        }else {
                            Toast.makeText(MainActivity.this, "please login", Toast.LENGTH_SHORT).show();

                        }
                        //Listen to changes in the likes database reference
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (likeChecher.equals(true)){
                                    //if the current post has a like, associated to the current logged and the user clicks on it again,
                                    //remove the like, basically this means the user is dislikind the post
                                    if (snapshot.child(post_key).hasChild(currentUserID)){
                                        likesRef.child(post_key).child(currentUserID).removeValue();
                                        likeChecher = false;
                                    }else{
                                        //here the user is liking, set value on the like
                                        likesRef.child(post_key).child(currentUserID).setValue(true);
                                        likeChecher = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser !=null){
            adapter.stopListening();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the menu; this adds item to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        //implement the functionality of the add icon, so that the user on clicking it launches the post activity
        else if (id == R.id.action_add) {
            Intent postIntent=new Intent(this,PostActivity.class);
            startActivity(postIntent);
            // on clicking log out, log the user out
        } else if (id == R.id.logout){
            mAuth.signOut();
            Intent logoutIntent = new Intent(MainActivity.this, RegisterActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logoutIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}