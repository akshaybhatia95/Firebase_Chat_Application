package com.example.d.unitedtochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class StartActivity extends AppCompatActivity {
    private Button regButton;
    private Button loginButton;
    private EditText login_Email;
    private EditText login_Password;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();

        loginButton = (Button) findViewById(R.id.login_Button);
        regButton = (Button) findViewById(R.id.startRegButton);
        login_Email = (EditText) findViewById(R.id.login_Email);
        login_Password = (EditText) findViewById(R.id.login_Password);
        mProgressDialog = new ProgressDialog(this);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = login_Email.getText().toString();
                String password = login_Password.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(StartActivity.this, "Fields are Empty", Toast.LENGTH_LONG).show();
                } else {
                    mProgressDialog.setTitle("Logging in");
                    mProgressDialog.setMessage("Please wait..we are checking your credentials");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    loginUser(email, password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mProgressDialog.dismiss();
                    mUserDatabase.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(FirebaseInstanceId.getInstance().getToken());
                    Intent i = new Intent(StartActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();

                } else {
                    mProgressDialog.hide();
                    Toast.makeText(StartActivity.this, "Login Failed.Please check Email And Passowrd", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
