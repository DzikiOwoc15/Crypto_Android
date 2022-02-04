package com.hfad.crypto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.hfad.crypto.addInvestment.AddInvestmentActivity;
import com.hfad.crypto.ui.main.AddCoinActivity;
import com.hfad.crypto.ui.main.SectionsPagerAdapter;
import com.hfad.crypto.ui.main.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int VIEW_PAGER_ID = getIntent().getIntExtra("VIEW_PAGER_ID", 0);
        boolean SHOW_SNACKBAR = getIntent().getBooleanExtra("SHOW_SNACKBAR", false);

        if(SHOW_SNACKBAR){
            Toast.makeText(getApplicationContext(), "Dodano kryptowalutę", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences nightMode = getSharedPreferences("nightMode", MODE_PRIVATE);
        boolean mode = nightMode.getBoolean("mode", false);
        if(mode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        viewPager.setCurrentItem(VIEW_PAGER_ID);

        fab.setOnClickListener(view -> {
            int index = tabs.getSelectedTabPosition();
            Intent intent;
            if(index == 0){
                intent = new Intent(getApplicationContext(), AddCoinActivity.class);
            }
            else{
                intent = new Intent(getApplicationContext(), AddInvestmentActivity.class);
            }
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
        });
        }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.view_pager);
        if(!(fragment instanceof OnCustomBackPressed) || !((OnCustomBackPressed) fragment).onBackPressed()){
            super.onBackPressed();
        }
    }

}