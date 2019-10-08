package com.example.d.unitedtochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {
    ImageView mImageView;
    TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    Button mProfileSendRequestButton;
    Button mDeclineButton;
    DatabaseReference mDatabaseReference;
    DatabaseReference frienReuqestDatabase;
    DatabaseReference friendDatabase;
    DatabaseReference mNotificationDatabase;
    FirebaseUser currentUser;
    private static final String sent = "sent";
    private static final String received = "received";
    private static final String notFriend = "not_friend";
    private static final String requestSent = "request_sent";
    private static final String requestReceived = "request_received";
    private static final String current_friend = "friends";
    private String current_state = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent i = getIntent();
        final String userId = i.getStringExtra("userId");
        mImageView = (ImageView) findViewById(R.id.userImage);
        mProfileName = (TextView) findViewById(R.id.userName);
        mProfileStatus = (TextView) findViewById(R.id.userStatus);
        mProfileFriendsCount = (TextView) findViewById(R.id.friendNo);
        mProfileSendRequestButton = (Button) findViewById(R.id.button1);
        mDeclineButton = (Button) findViewById(R.id.button2);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        frienReuqestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDeclineButton.setEnabled(false);
        mDeclineButton.setVisibility(View.INVISIBLE);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);
                Picasso.get().load(image).into(mImageView);
                mDeclineButton.setEnabled(false);


                frienReuqestDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        current_state = notFriend;
                        if (dataSnapshot.hasChild(userId)) {
                            String temp = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (temp.equals(received)) {
                                mProfileSendRequestButton.setEnabled(true);
                                mProfileSendRequestButton.setText("Accept Friend Request");
                                current_state = requestReceived;
                                mDeclineButton.setEnabled(true);
                                mDeclineButton.setVisibility(View.VISIBLE);

                            } else if (temp.equals(sent)) {

                                mProfileSendRequestButton.setEnabled(true);
                                mProfileSendRequestButton.setText("Cancel Friend Request");
                                current_state = requestSent;
                                mDeclineButton.setEnabled(false);
                                mDeclineButton.setVisibility(View.INVISIBLE);
                            }
                        }


                        if (friendDatabase.child(currentUser.getUid()) != null) {
                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userId)) {
                                        if (dataSnapshot.getValue().toString() != null) {
                                            mProfileSendRequestButton.setEnabled(true);
                                            mProfileSendRequestButton.setText("Unfriend " + mProfileName.getText().toString());
                                            current_state = current_friend;
                                            mDeclineButton.setEnabled(false);
                                            mDeclineButton.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ADDING YOURSELF
                if (currentUser.getUid().toString().equals(userId)) {
                    Toast.makeText(UserProfileActivity.this, "Cannot add yourself as a friend", Toast.LENGTH_LONG).show();
                    return;
                }

                // ADDING SOMEONE WHO IS NOT A FRIEND
                if (current_state.equals(notFriend)) {
                    mProfileSendRequestButton.setEnabled(false);
                    frienReuqestDatabase.child(currentUser.getUid()).child(userId).child("request_type").setValue(sent)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        frienReuqestDatabase.child(userId).child(currentUser.getUid()).child("request_type").setValue(received)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mProfileSendRequestButton.setEnabled(true);
                                                        mProfileSendRequestButton.setText("Cancel Friend Request");
                                                        current_state = requestSent;
                                                        mDeclineButton.setEnabled(false);
                                                        mDeclineButton.setVisibility(View.INVISIBLE);
                                                        HashMap<String, String> hashMap = new HashMap<>();
                                                        hashMap.put("from", currentUser.getUid());
                                                        hashMap.put("type", "request");
                                                        mNotificationDatabase.child(userId).push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                                    }
                                                });
                                    } else {
                                        // Toast.makeText(this,"Failed Sending Request", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
                // REQUEST ALREADY SENT
                if (current_state.equals(requestSent)) {
                    frienReuqestDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            frienReuqestDatabase.child(currentUser.getUid()).child(userId).removeValue();
                            mProfileSendRequestButton.setEnabled(true);
                            mProfileSendRequestButton.setText("Send Friend Request");
                            current_state = notFriend;
                            mDeclineButton.setEnabled(false);
                            mDeclineButton.setVisibility(View.INVISIBLE);
                        }
                    });


                }


                // REQUEST RECIEVED BY OTHER USER
                if (current_state.equals(requestReceived)) {
                    Toast.makeText(UserProfileActivity.this, "inside", Toast.LENGTH_LONG).show();
                    final String date = DateFormat.getDateTimeInstance().format(new Date());
                    friendDatabase.child(currentUser.getUid()).child(userId).child("date").setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(userId).child(currentUser.getUid()).child("date").setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequestButton.setEnabled(true);
                                    mProfileSendRequestButton.setText("UnFriend " + mProfileName.getText().toString());
                                    current_state = current_friend;
                                    mDeclineButton.setEnabled(false);
                                    mDeclineButton.setVisibility(View.INVISIBLE);

                                    frienReuqestDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            frienReuqestDatabase.child(currentUser.getUid()).child(userId).removeValue();

                                        }
                                    });

                                }
                            });
                        }
                    });

                }
                //unfriending
                if (current_state.equals(current_friend)) {
                    friendDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequestButton.setEnabled(true);
                                    mProfileSendRequestButton.setText("Send Friend Request");
                                    current_state = notFriend;
                                    mDeclineButton.setEnabled(false);
                                    mDeclineButton.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }
            }
        });


        mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (current_state == requestReceived) {

                    frienReuqestDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            frienReuqestDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDeclineButton.setEnabled(false);
                                    mProfileSendRequestButton.setText("Send Friend Request");
                                    current_state = notFriend;
                                    mDeclineButton.setEnabled(false);
                                    mDeclineButton.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }
            }
        });
        // Toast.makeText(this,userId,Toast.LENGTH_LONG).show();

    }
}
