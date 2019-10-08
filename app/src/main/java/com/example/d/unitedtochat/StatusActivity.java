package com.example.d.unitedtochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private TextInputLayout statusLayout;
    private Button statusButton;
    DatabaseReference mStatusDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mToolBar = (Toolbar) findViewById(R.id.statusAppBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        statusLayout = (TextInputLayout) findViewById(R.id.status_inputLayout);
        statusButton = findViewById(R.id.statusButton);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("status");

        //Setting already set status in edit text
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        statusLayout.getEditText().setText(status);


        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = statusLayout.getEditText().getText().toString();
                mStatusDatabase.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful() == true) {

                        } else {

                        }
                    }
                });
            }
        });
    }
}
