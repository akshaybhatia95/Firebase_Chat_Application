package com.example.d.unitedtochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    Toolbar mToolbar;
    RecyclerView recyclerView;
    private DatabaseReference mDatabaseRef;
    Query query;
    FirebaseRecyclerAdapter adapter;
    FirebaseRecyclerOptions<UserModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar = (Toolbar) findViewById(R.id.userAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        recyclerView = findViewById(R.id.userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // recyclerView.setHasFixedSize(true);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");
        options = new FirebaseRecyclerOptions.Builder<UserModel>().setQuery(mDatabaseRef, UserModel.class).build();
        adapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {

                holder.setNameView(model.getName());
                holder.setStatusview(model.getStatus());
                holder.setImage(model.getThumb_image());
                Log.d("Name", model.getName());
                Log.d("Status", model.getStatus());
                Log.d("Thumb", model.getThumb_image());

                final String clickedUserId = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(UsersActivity.this, UserProfileActivity.class);
                        i.putExtra("userId", clickedUserId);
                        startActivity(i);
                    }
                });


            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleuseritem, parent, false);

                return new UserViewHolder(v);
            }
        };
//        adapter.startListening();
        //adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        // ImageView imageView;
        String imageUrl;
        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            // Toast.makeText(UsersActivity.this,"ViewHolder",Toast.LENGTH_SHORT).show();
            //   imageView=(CircleImageView) itemView.findViewById(R.id.user_image);
            mView = itemView;
        }

        public void setStatusview(String statusview1) {
            TextView statusview = (TextView) itemView.findViewById(R.id.user_status);
            statusview.setText(statusview1);
        }

        public void setNameView(String nameView1) {
            TextView nameView = (TextView) itemView.findViewById(R.id.user_name);
            nameView.setText(nameView1);
        }


        public void setImage(String image) {
            CircleImageView circleImageView = (CircleImageView) itemView.findViewById(R.id.thumb);
            try {
                Picasso.get().load(image).into(circleImageView);
                //  Log.d("REACHED", image.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
