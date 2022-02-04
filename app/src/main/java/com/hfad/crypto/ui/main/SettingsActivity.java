package com.hfad.crypto.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.MainActivity;
import com.hfad.crypto.R;
import com.hfad.crypto.database.CoinViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {
    private Spinner currenciesSpinner;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final CoinViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CoinViewModel.class);
        viewModel.convertURLtoJSON(ConstUrl.getURL_CURRENCY() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }
            @Override
            public void onNext(@NonNull String result) {
                try {
                    if (result != null) {
                        JSONObject jsonObject = new JSONObject(result);
                        //For every currency entry add it to the Adapter
                        String[] currencies = new String[jsonObject.getJSONArray("data").length()];
                        for(int i = 0; i < jsonObject.getJSONArray("data").length(); i++){
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            currencies[i] = jsonArray.getJSONObject(i).getString("symbol");
                        }
                        //When the user opens the settings, his previous settings will be displayed first
                        SharedPreferences currencyPreferences = getApplication().getSharedPreferences(String.valueOf(R.string.setting_currency), Context.MODE_PRIVATE);
                        String currency = currencyPreferences.getString(String.valueOf(R.string.setting_currency), "USD");
                        for(int i =0; i < currencies.length; i++){
                            if(currencies[i].equals(currency)){
                                String temp = currencies[0];
                                currencies[0] = currency;
                                currencies[i] = temp;
                                break;
                            }
                        }

                        ArrayAdapter<CharSequence> currencyAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, currencies);
                        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        currenciesSpinner.setAdapter(currencyAdapter);
                        currenciesSpinner.setSelection(0, true);
                        View view = currenciesSpinner.getSelectedView();
                        //Is nightMode on?
                        boolean isOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
                        if(isOn)
                            ((TextView)view).setTextColor(getResources().getColor(R.color.colorAccent));
                        else{
                            ((TextView)view).setTextColor(getResources().getColor(R.color.darkBackground));
                        }
                        //User selects data from dropdown list, based on that data MarketFragment's API call is made
                        currenciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if(isOn)
                                    ((TextView)view).setTextColor(getResources().getColor(R.color.colorAccent));
                                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(String.valueOf(R.string.setting_currency), MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(String.valueOf(R.string.setting_currency), adapterView.getItemAtPosition(i).toString());
                                editor.apply();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONTask failed", "task failed" + e.toString());
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() { }
        });

        //Save new dark mode settings
        SharedPreferences nightModePreferences = getApplication().getSharedPreferences("nightMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = nightModePreferences.edit();
        SwitchMaterial themeSwitch = findViewById(R.id.theme_switch);
        themeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if(!b){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                boolean isOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
                Log.d("SettingsActivity", "IsNightMode on? =>" + isOn);
                editor.putBoolean("mode", false);
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                boolean isOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
                Log.d("SettingsActivity", "IsNightMode on? =>" + isOn);
                editor.putBoolean("mode", true);
            }
            editor.apply();
        });

        boolean defaultTheme = nightModePreferences.getBoolean("mode", false);
        themeSwitch.setChecked(defaultTheme);

        currenciesSpinner = findViewById(R.id.currency_spinner);
        Spinner percentSpinner = findViewById(R.id.percent_spinner);



        String[] strings = getResources().getStringArray(R.array.currencies);
        //When the user opens settingsActivity, his previous settings will be displayed first
        SharedPreferences percentPreferences = getApplication().getSharedPreferences(String.valueOf(R.string.setting_percent), Context.MODE_PRIVATE);
        String percent =  percentPreferences.getString(String.valueOf(R.string.setting_percent), "percent_change_7d");
        String[] percentArray = getResources().getStringArray(R.array.percent_change);
        for(int i = 0; i < strings.length; i++){
            if(percentArray[i].equals(percent)){
                String temp = percentArray[0];
                percentArray[0] = percent;
                percentArray[i] = temp;
                break;
            }
        }

        ArrayAdapter<CharSequence> percentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, percentArray);
        percentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        percentSpinner.setAdapter(percentAdapter);

        //User selects data from dropdown list, based on that data MarketFragment's API call is made
        percentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(String.valueOf(R.string.setting_percent), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(String.valueOf(R.string.setting_percent), adapterView.getItemAtPosition(i).toString());
                Log.d("SharedPref", "settings of percent change R.string - " + R.string.setting_percent);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }
}