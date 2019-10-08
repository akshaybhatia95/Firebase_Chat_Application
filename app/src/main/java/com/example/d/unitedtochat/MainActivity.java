package com.example.d.unitedtochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("NATIVE");

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


        mViewPager = (ViewPager) findViewById(R.id.tabPager);
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.mainTab);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent startPageIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startPageIntent);
            finish();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.log_out) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if (item.getItemId() == R.id.account_Settings) {
            Intent settingsIntent = new Intent(MainActivity.this, AccountSettings.class);
            startActivity(settingsIntent);

        }
        if (item.getItemId() == R.id.allUsers) {
            Intent allUserIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(allUserIntent);
        }
        return true;
    }

    private void sendToStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent startPageIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startPageIntent);
            finish();
        }

    }
}
