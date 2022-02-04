package com.hfad.crypto.addInvestment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.hfad.crypto.MainActivity;
import com.hfad.crypto.R;
import com.hfad.crypto.ui.main.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddInvestmentActivity extends AppCompatActivity {
    private static final List<String> coinList = new ArrayList<>();
    private static final List<Integer> idList = new ArrayList<>();
    private static final List<Bitmap> bitmapList = new ArrayList<>();
    private ListAdapter listAdapter;
    private ViewPager viewPager;
    private boolean isCoinSelected = false;

    public void setViewPagerPage(int i){
        viewPager.setCurrentItem(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        InvestmentViewModel viewModel = new ViewModelProvider(this).get(InvestmentViewModel.class);

        InvestmentPagerAdapter pagerAdapter = new InvestmentPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("VIEW_PAGER_ID", 1);
            startActivity(intent);
        });

        viewModel.getBitmapLiveData().observe(this , bitmap -> {
            if(bitmap != null){
                isCoinSelected = true;
                Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_LONG).show();
                pagerAdapter.setBitmap(bitmap);
                pagerAdapter.notifyDataSetChanged();
            }
        });

        ImageButton fragmentBackButton = findViewById(R.id.imageButton2);
        ImageButton fragmentForwardButton = findViewById(R.id.imageButton3);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    fragmentBackButton.setVisibility(View.INVISIBLE);
                    fragmentForwardButton.setVisibility(View.VISIBLE);
                }
                if(position == 1){
                    fragmentBackButton.setVisibility(View.VISIBLE);
                    fragmentForwardButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        fragmentForwardButton.setOnClickListener(view -> {
            if(viewPager.getCurrentItem() == 0){
                viewPager.setCurrentItem(1,true);
            }
        });

        fragmentBackButton.setOnClickListener(view -> {
            if(viewPager.getCurrentItem() == 1) {
                viewPager.setCurrentItem(0,true);
            }
        });


        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        ListView listView = findViewById(R.id.investment_list_view);
        ImageView coinImage = findViewById(R.id.investment_image);
        CoinViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CoinViewModel.class);
        setSupportActionBar(toolbar);
        Log.d("Investment", ConstUrl.getUrlListing() + ConstUrl.getLIMIT() + ConstUrl.getApiKey());
        boolean isNightModeOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        listAdapter = new ListAdapter(getApplicationContext(), coinList);
        if(isNightModeOn){
            listAdapter.setNightModeOn();
        }
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            coinImage.setImageBitmap(bitmapList.get(i));
        });
        listAdapter.setImages(bitmapList);
        viewModel.convertURLtoJSON(ConstUrl.getUrlListing() + ConstUrl.getLIMIT() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(@NonNull String s) {
                if(s != null){
                    try{
                        JSONObject  jsonObject = new JSONObject(s);
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for(int i = 0; i < jsonObject.getJSONArray("data").length(); i++){
                            coinList.add(jsonArray.getJSONObject(i).getString("name"));
                            idList.add(jsonArray.getJSONObject(i).getInt("id"));
                        }
                        Log.d("Inv", coinList.get(0));
                        StringBuilder imageIds =  new StringBuilder("id=" + idList.get(0));
                        for(int i = 1; i < idList.size(); i++){
                            imageIds.append(",");
                            imageIds.append(idList.get(i));
                        }
                        listAdapter.notifyDataSetChanged();
                        Log.d("Inv", ConstUrl.getImageUrl() + imageIds + ConstUrl.getApiKey());
                        viewModel.convertUrlToJsonThenToBitmap(ConstUrl.getImageUrl() + imageIds + ConstUrl.getApiKey(), idList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Observable<Bitmap>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {}
                            @Override
                            public void onNext(@NonNull Observable<Bitmap> bitmapObservable) {
                                bitmapObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Bitmap>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {}
                                    @Override
                                    public void onNext(@NonNull Bitmap bitmap) {
                                        bitmapList.add(bitmap);
                                        listAdapter.notifyDataSetChanged();
                                    }
                                    @Override
                                    public void onError(@NonNull Throwable e) {}
                                    @Override
                                    public void onComplete() {}
                                });
                            }
                            @Override
                            public void onError(@NonNull Throwable e) {}
                            @Override
                            public void onComplete() {}
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
         */
    }
}