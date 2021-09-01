package com.kalvin.theattic;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

class AtticViewHolder extends RecyclerView.ViewHolder {
    private final MainActivity mainActivity;
    //declare the view objects in the card view
    public TextView post_desc;
    public ImageView post_image;
    public TextView post_title;
    public TextView postUserName;
    public ImageView user_image;

    public TextView postTime;
    public TextView postDate;
    public LinearLayout post_layout;
    public ImageButton likePostButton, commentPostButton;
    public TextView displayLikes;

    //Declare an int variable to hold the count of likes
    int countLikes;
    //Declare a string variable to hold the user Id of currently logged in user
    String currentUserID;
    //Declare an instanc of firebase authentication
    FirebaseAuth mAUth;
    //Declare a databaseReference where you are saving the likes
    DatabaseReference likesRef;
    //create constructor matching super

    public AtticViewHolder(MainActivity mainActivity, View itemView) {
        super(itemView);
        this.mainActivity = mainActivity;
        //Initialize the cardview item objects
        post_title = itemView.findViewById(R.id.post_title_txtview);
        post_desc = itemView.findViewById(R.id.post_desc_txtview);
        post_image = itemView.findViewById(R.id.post_image);
        postUserName = itemView.findViewById(R.id.post_user);
        user_image = itemView.findViewById(R.id.userImage);
        postTime = itemView.findViewById(R.id.time);
        postDate = itemView.findViewById(R.id.date);
        post_layout = itemView.findViewById(R.id.linear_layout_post);
        likePostButton = itemView.findViewById(R.id.like_button);
        commentPostButton = itemView.findViewById(R.id.comment);
        displayLikes = itemView.findViewById(R.id.likes_display);

        //Initialize a database reference where you will store the likes
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
    }
    //create your setters, you will use this setter in your onBindViewHolder method

    public void setTitle(String title) {
        post_title.setText(title);
    }

    public void setDesc(String desc) {
        post_desc.setText(desc);
    }

    public void setPostImage(Context ctx, String postImage) {
        Picasso.with(ctx).load(postImage).into(post_image);
    }

    public void setUserName(String userName) {
        postUserName.setText(userName);
    }

    public void setProfilePhoto(Context context, String profilePhoto) {
        Picasso.with(context).load(profilePhoto).into(user_image);
    }

    public void setTime(String time) {
        postTime.setText(time);
    }

    public void setDate(String date) {
        postDate.setText(date);
    }

    public void setLikeButtonStatus(final String post_key) {
        //we want to know who has liked a particular post, so let's get the user using
        //thier user_ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserID = user.getUid();
        } else {
            Toast.makeText(mainActivity, "Please login", Toast.LENGTH_SHORT).show();
        }
        //Listen to changes in the database reference on likes
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //define post_key in the onBindViewHolder method
                //check if  post has been liked
                if (snapshot.child(post_key).hasChild(currentUserID)) {
                    //if liked get the number of likes
                    countLikes = (int) snapshot.child(post_key).getChildrenCount();
                    //check the image from initial dislike to like
                    likePostButton.setImageResource(R.drawable.like);
                    //count the like and display them in the textView for likes
                    displayLikes.setText(Integer.toString(countLikes));

                } else {
                    //If disliked, get the current number of likes
                    countLikes = (int) snapshot.child(post_key).getChildrenCount();
                    //set the image resource as disliked
                    likePostButton.setImageResource(R.drawable.dislike);
                    //display the current number of likes
                    displayLikes.setText(Integer.toString(countLikes));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
