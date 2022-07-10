package com.cr7.budgetapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class LoggedInActivity extends AppCompatActivity {
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        // Referencing to the toolbar which has the menu items and the hamburger icon for opening
        // the navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Setting the toolbar as an ActionBar as this activity has no action bar
        setSupportActionBar(toolbar);
        // Setting the title of the activity on the toolbar (ActionBar)
        getSupportActionBar().setTitle("Budget Manager");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Referencing the navigation drawer
        drawer = findViewById(R.id.drawer_layout);
        // This is the hamburger toggle button, which on clicking opens the navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_bar_open, R.string.navigation_bar_close);
        // Setting the toggle as the listener so that on clicking it the navigation drawer wil open
        drawer.addDrawerListener(toggle);
        // Syncing it's state i.e. if it it open or close
        toggle.syncState();
        // Referencing to the navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Setting a NavigationItemSelectedListener for all the menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    // If the menu item has the id "budget" then switching to the budget fragment
                    // (i.e. the "Root" fragment)
                    case R.id.budget:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                                new Root()).commit();
                        break;
                    // If menu item has id "laundry" then switching to the "Laundry" fragment
                    case R.id.laundry:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                                new Laundry()).commit();
                        break;
                }
                // Closing the navigation drawer after selecting an item
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        // Keeping the "Root" fragment open by default (i.e. when the app first starts)
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new Root()).commit();
        // Setting the default item (budget) checked by default (as it opens by default)
        navigationView.setCheckedItem(R.id.budget);
    }

    @Override
    public void onBackPressed() {
        // On pressing the back button when the navigation drawer is open then close it else do
        // the default back pressed
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
