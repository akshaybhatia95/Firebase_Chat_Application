package com.example.d.unitedtochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //UI vars
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button signUp;
    ProgressDialog mProgressDialog;

    //Firebase Vars
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDisplayName = (TextInputLayout) findViewById(R.id.reg_DisplayName);
        mEmail = (TextInputLayout) findViewById(R.id.reg_Email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_Password);
        signUp = (Button) findViewById(R.id.signUp);
        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String Email = mEmail.getEditText().getText().toString();
                String Password = mPassword.getEditText().getText().toString();
                if (Email.isEmpty() || Password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Fields are Empty", Toast.LENGTH_LONG).show();
                } else {
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait...creating account");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    registerUser(displayName, Email, Password);
                }

            }
        });

    }

    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            String uId = currentUser.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", displayName);
                            userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                            userMap.put("status", "Hey there! UnitedToChat peeps!!");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();
                                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        mProgressDialog.hide();
                                        Toast.makeText(RegisterActivity.this, "Error Registering...Try Again Later", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }


                    }
                });

    }
}
