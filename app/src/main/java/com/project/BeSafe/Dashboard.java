package com.project.BeSafe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.navigation.NavigationView;
import com.project.BeSafe.ui.home.HomeFragment;
import com.project.BeSafe.ui.report_incident.ReportIncidentFragment;
import com.project.BeSafe.ui.newsfeed.NewsFeedFragment;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private static final String TAG = "Dashboard";
    private static final int REQUEST_LOCATION_PERMISSION = 1, REQUEST_STORAGE_PERMISSION = 2;
    private AppDatabase db;
    private NotificationDao notificationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        checkInternetConnectivity();
        checkAndRequestLocationPermission();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "notification-db").build();
        notificationDao = db.notificationDao();

        //Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setup ActionBarDrawerToggle to manage the drawer opening and closing
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Load the default fragment (HomeFragment) on startup
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    //Checks and requests location permission from the user
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            Log.d(TAG, "Location permission already granted");
            // Proceed to check storage permission if location is already granted
            checkAndRequestStoragePermission();
        }
    }

    //Checks and requests storage permission from the user
    private void checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Request storage permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            Log.d(TAG, "Storage permission already granted");
        }
    }


    //Handles the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                checkAndRequestStoragePermission();

                //Initialize drawer layout and navigation view
                drawerLayout = findViewById(R.id.drawer_layout);
                NavigationView navigationView = findViewById(R.id.navigation_view);
                navigationView.setNavigationItemSelectedListener(this);

                //Load default fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
                Log.d(TAG, "Location permission denied");
                handlePermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted");
            } else {
                Log.d(TAG, "Storage permission denied");
                handlePermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }


    //Handles the situation when a permission is denied
    private void handlePermissionDenied(String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Location permission denied. Some features may not work properly.", Toast.LENGTH_LONG).show();
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage permission denied. Some features may not work properly.", Toast.LENGTH_LONG).show();
        }

        //If the user has permanently denied the permission prompt them to enable it from settings
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "Please enable permissions from settings.", Toast.LENGTH_LONG).show();
        }
    }

    //Handles the selection of navigation items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        Log.d(TAG, "Menu item selected: " + itemId);

        //Determine the selected fragment based on the menu item ID
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            Log.d(TAG, "HomeFragment selected");
        } else if (itemId == R.id.nav_reportincident) {
            selectedFragment = new ReportIncidentFragment();
            Log.d(TAG, "ReportIncidentFragment selected");
        } else if (itemId == R.id.nav_newsfeed) {
            selectedFragment = new NewsFeedFragment();
            Log.d(TAG, "NewsFeedFragment selected");
        } else if (itemId == R.id.nav_notifications) {
            selectedFragment = new NotificationFragment();
            Log.d(TAG, "NotificationFragment selected");
        }
        else if (itemId == R.id.navAboutUs) {
            selectedFragment = new AboutUsFragment();
            Log.d(TAG, "AboutUsFragment selected");
        }
        else if (itemId == R.id.navLogout) {
            logoutUser();
            return true;
        } else {
            Toast.makeText(this, "Unknown menu item selected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Unknown menu item selected");
        }

        //If a valid fragment is selected replace the current fragment with the new one
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            Log.d(TAG, "Selected fragment is ready to run");
        }

        //Close the drawer after a selection is made
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Handles the drawer open and close
    @Override
    public void onBackPressed() {
        //
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Logs out the user by clearing session data and redirecting to the login activity
    private void logoutUser() {
        // Clear user session data
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to Login Activity
        Intent intent = new Intent(Dashboard.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User logged out successfully");
    }

    //Checks the internet connectivity and logs the status.
    private void checkInternetConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
            Log.e(TAG, "No internet connection available");
        } else {
            Log.d(TAG, "Internet connection is available");
        }
    }
}
