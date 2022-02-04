package com.hfad.crypto.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.MainActivity;
import com.hfad.crypto.R;
import com.hfad.crypto.Objects.SimpleCoin;
import com.hfad.crypto.database.CoinViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddCoinActivity extends AppCompatActivity {
    private ListAdapter listAdapter;
    private static final List<String> coinList = new ArrayList<>();
    private static final List<Integer> idList = new ArrayList<>();
    private static final List<Bitmap> bitmapList = new ArrayList<>();
    private Disposable disposableMain;
    private Disposable disposableBitmap;
    private Disposable disposableConvertToJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coin);
        ListView listView = findViewById(R.id.search_list);
        final CoinViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CoinViewModel.class);
        viewModel.convertURLtoJSON(ConstUrl.getUrlListing() + ConstUrl.getLIMIT() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposableMain = d;
            }
            @Override
            public void onNext(@NonNull String result) {
                if(result != null){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        for(int i = 0; i < jsonObject.getJSONArray("data").length(); i++){
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            coinList.add(jsonArray.getJSONObject(i).getString("name"));
                            idList.add(jsonArray.getJSONObject(i).getInt("id"));
                            listAdapter.notifyDataSetChanged();
                        }
                        if(idList.size() >= 1){
                            StringBuilder imageIds =  new StringBuilder("id=" + idList.get(0));
                            for(int i = 1; i < idList.size(); i++){
                                imageIds.append(",");
                                imageIds.append(idList.get(i));
                            }
                            viewModel.convertURLtoJSON(ConstUrl.getImageUrl() + imageIds + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {disposableConvertToJSON = d;}
                                @Override
                                public void onNext(@NonNull String result) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        bitmapList.clear();
                                        if(coinList.size() > 0){
                                            List<String> logoList = new ArrayList<>();
                                            for(int i = 0; i<idList.size(); i++){
                                                String logoUrl =  jsonObject.getJSONObject("data").getJSONObject(String.valueOf(idList.get(i))).getString("logo");
                                                logoList.add(logoUrl);
                                            }
                                            Observable.fromIterable(logoList).flatMap(AddCoinActivity.this::bitmapFromURL).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Bitmap>() {
                                                @Override
                                                public void onSubscribe(@NonNull Disposable d) {
                                                    disposableBitmap = d;
                                                }
                                                @Override
                                                public void onNext(@NonNull Bitmap bitmap) {
                                                    bitmapList.add(bitmap);
                                                    listAdapter.setImages(bitmapList);
                                                    listAdapter.notifyDataSetChanged();
                                                }
                                                @Override
                                                public void onError(@NonNull Throwable e) {}
                                                @Override
                                                public void onComplete() {}
                                            });
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("JSONTask failed", "task failed" + e.toString());
                                    }
                                }
                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                }
                                @Override
                                public void onComplete() {}
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() { }
        });

        //If night mode is on make listItems white (textColor)
        boolean isNightModeOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        listAdapter = new ListAdapter(this, coinList){
            @androidx.annotation.NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                if(isNightModeOn){
                    ((TextView)view.findViewById(R.id.text_item)).setTextColor(getResources().getColor(R.color.white));
                }
                return view;
            }
        };

        //If user clicks on item add it to the database
        listView.setAdapter(listAdapter);
        viewModel.getSimpleCoinCount().observe(this, integer -> {
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                SimpleCoin coin = new SimpleCoin(getIdBasedOnName(listAdapter.getCoinName(i)), listAdapter.getCoinName(i), integer + 1);
                viewModel.insertCoin(coin);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Bitcoin");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listAdapter.getFilter().filter(s);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private Observable<Bitmap> bitmapFromURL(String url){
        return Observable.fromCallable(() -> {
            URL url1 = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if(bitmap == null){
                return Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
            }
            return bitmap;
        }).onErrorComplete();
    }


    public int getIdBasedOnName(String name){
        for (int i = 0; i < coinList.size(); i++){
            if(coinList.get(i).equals(name)){
                return idList.get(i);
            }
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposableMain != null && !disposableMain.isDisposed()){
            disposableMain.dispose();
        }
        if(disposableBitmap != null && !disposableBitmap.isDisposed()){
            disposableBitmap.dispose();
        }
        if(disposableConvertToJSON != null && !disposableConvertToJSON.isDisposed()){
            disposableConvertToJSON.dispose();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}