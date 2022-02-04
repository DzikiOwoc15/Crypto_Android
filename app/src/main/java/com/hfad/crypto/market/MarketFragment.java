package com.hfad.crypto.market;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hfad.crypto.Objects.Coin;
import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.OnCustomBackPressed;
import com.hfad.crypto.R;
import com.hfad.crypto.Objects.SimpleCoin;
import com.hfad.crypto.database.CoinViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MarketFragment extends Fragment implements OnCustomBackPressed {


    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private static MarketAdapter marketAdapter;
    private static String settingsChange;
    CoinViewModel viewModel;
    private static String currency;
    private  static List<SimpleCoin> coinList;
    private static final List<Bitmap> bitmapList = new ArrayList<>();
    private boolean isConnectionWorking = false;
    private RecyclerView recyclerView;
    private ItemTouchHelper.SimpleCallback touchHelper;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_market, container, false);
        recyclerView = rootView.findViewById(R.id.market_recycler);
        TextView instructionPriceTextView = rootView.findViewById(R.id.instruction_price);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel =  new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CoinViewModel.class);
        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.market_refresher);

        //If close menu is active user can turn it off by pressing back Button
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(marketAdapter.isMenuActive()){
                    marketAdapter.closeMenu();
                }
                else{
                    onBackPressed();
                }
            }
        });

        //If user swipes coin he sees a menu that allows him to delete a coin
        touchHelper = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT){
            private final ColorDrawable background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                Log.d("ItemTouchHelper", "from" + fromPos + "to" + toPos);
                //viewModel.moveCoin(fromPos, toPos);
                return true;
            }

            @Override
            public void onMoved(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @androidx.annotation.NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                final int from = viewHolder.getAdapterPosition();
                final int to = target.getAdapterPosition();
                marketAdapter.moveCoin(fromPos, toPos);
                marketAdapter.notifyItemMoved(fromPos, toPos);
                //viewModel.moveCoin(from, to);
            }

            @Override
            public void clearView(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                marketAdapter.notifyDataSetChanged();
                List<SimpleCoin> newList = marketAdapter.getDatabaseList();
                viewModel.setDatabaseList(newList);
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                marketAdapter.showMenu(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@androidx.annotation.NonNull Canvas c, @androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(isConnectionWorking){
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    View itemView = viewHolder.itemView;
                    if (dX > 0) {
                        background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                    } else if (dX < 0) {
                        background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else {
                        background.setBounds(0, 0, 0, 0);
                    }
                    background.draw(c);
                }
            }
        };

        //On delete pressed listener
        marketAdapter = new MarketAdapter((view, position) -> {
            viewModel.deleteCoin(coinList.get(position).getId());
            marketAdapter.closeMenu();
        });

        recyclerView.setOnScrollChangeListener((view, i, i1, i2, i3) -> view.post(() -> {
            marketAdapter.closeMenu();
        }));

        //Get user's settings and based on that set String to get data from JSONObject
        SharedPreferences percentPreferences = getActivity().getSharedPreferences(String.valueOf(R.string.setting_percent), Context.MODE_PRIVATE);
        SharedPreferences currencyPreferences = getActivity().getSharedPreferences(String.valueOf(R.string.setting_currency), Context.MODE_PRIVATE);
        settingsChange = percentPreferences.getString(String.valueOf(R.string.setting_percent), "percent_change_7d");
        switch (settingsChange){
            case "1 h":
                settingsChange = "percent_change_1h";
                break;
            case "24 h":
                settingsChange = "percent_change_24h";
                break;
            case "7 dni":
                settingsChange = "percent_change_7d";
                break;
        }
        currency = currencyPreferences.getString(String.valueOf(R.string.setting_currency), "USD");
        //This line of code fixes weird bug, sometimes when the app was opened currency was set to "7 days"
        if(currency.equals("7 dni")){
            currency = "USD";
        }
        instructionPriceTextView.setText(getString(R.string.price, currency));

        //Sets data for the recyclerView
        final Observer<List<SimpleCoin>> coinObserver = coinList -> {
            MarketFragment.coinList = coinList;
            marketAdapter.notifyDataSetChanged();
            marketAdapter.setDatabaseList(coinList);
            isInternetWorking();
            refreshLayout.setOnRefreshListener(() -> {
                isInternetWorking();
                refreshLayout.setRefreshing(false);
            });
        };
        viewModel.getAllSimpleCoins().observe(getViewLifecycleOwner(), coinObserver);
        recyclerView.setAdapter(marketAdapter);
        return rootView;
    }

    public void isInternetWorking() {
        Observable.fromCallable(() -> {
            boolean success = false;
            try {
                URL url = new URL("https://google.com");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                success = connection.getResponseCode() == 200;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new io.reactivex.rxjava3.core.Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onNext(@NonNull Boolean connected) {
                if(connected){
                    marketAdapter.isConnected(1);
                }
                else {
                    marketAdapter.isConnected(0);
                }
                isConnectionWorking = connected;
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelper);
                itemTouchHelper.attachToRecyclerView(recyclerView);
                setData(coinList);
                }

            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    //set Data from CoinMarketCap API based on Id's in simpleCoinList
    public void setData(List<SimpleCoin> simpleCoinList){
        coinList = simpleCoinList;
        if(simpleCoinList != null)
        if(simpleCoinList.size() >= 1){
            StringBuilder id = new StringBuilder("id=" + simpleCoinList.get(0).getId());
            for(int i = 1; i < simpleCoinList.size(); i++){
                id.append(",");
                id.append(simpleCoinList.get(i).getId());
            }
            //This part creates separate url for JsonTaskImage because API call cant have a "convert" variable inside
            String imageString = id.toString();
            id.append("&convert=").append(currency);

            viewModel.convertURLtoJSON(ConstUrl.getUrlQuotes() + id.toString() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new io.reactivex.rxjava3.core.Observer<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {}
                @Override
                public void onNext(@NonNull String result) {
                    try{
                        if (result != null) {
                            List<Coin> coinList1 = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(result);
                            //For every coin received from database set adapter's item according to data
                            for (int i = 0; i < coinList.size(); i++) {
                                JSONObject btcObject = jsonObject.getJSONObject("data").getJSONObject(String.valueOf(coinList.get(i).getId()));
                                Coin coin = new Coin(btcObject.getString("name"), btcObject.getJSONObject("quote").getJSONObject(currency).getDouble("price"), btcObject.getJSONObject("quote").getJSONObject(currency).getDouble(settingsChange), false);
                                coinList1.add(coin);
                                marketAdapter.setCoins(coinList1);
                                marketAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("JSONTask failed", "task failed" + e.toString());
                    }
                }
                @Override
                public void onError(@NonNull Throwable e) {}
                @Override
                public void onComplete() {}
            });

            //It's for debugging - in the past without this some images replicated when adding new ones
            bitmapList.clear();

            viewModel.convertURLtoJSON(ConstUrl.getImageUrl() + imageString + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new io.reactivex.rxjava3.core.Observer<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {}
                @Override
                public void onNext(@NonNull String result) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        bitmapList.clear();
                        if(coinList.size() > 0){
                            String logoUrl =  jsonObject.getJSONObject("data").getJSONObject(String.valueOf(coinList.get(0).getId())).getString("logo");
                            Log.d("getData", "logo URL - " +  logoUrl);
                            getImageBitmapASYNC(logoUrl, coinList, 0, jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("JSONTask failed", "task failed" + e.toString());
                    }
                }
                @Override
                public void onError(@NonNull Throwable e) {}
                @Override
                public void onComplete() {}
            });
        }
    }
    //This one downloads image from the string url and returns bitmap
    private void getImageBitmapASYNC(String url, List<SimpleCoin> coinList1, int currentId, JSONObject jsonObject ){
        final String finalUrl = url;
        Observable.fromCallable(() -> {
            try {
                java.net.URL url1 = new URL(finalUrl);
                HttpURLConnection connection = (HttpURLConnection) url1
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("JSONTask failed", "task failed" + e.toString());
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new io.reactivex.rxjava3.core.Observer<Bitmap>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) { }
            @Override
            public void onNext(@NonNull Bitmap bitmap) {
                if(currentId + 1 < coinList1.size()){
                    try {
                        String logoUrl =  jsonObject.getJSONObject("data").getJSONObject(String.valueOf(coinList.get(currentId + 1).getId())).getString("logo");
                        getImageBitmapASYNC(logoUrl, coinList1, currentId + 1, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bitmapList.add(bitmap);
                Log.d("getData", "bitmap -  size -" + bitmapList.size() + "url -"  + url);
                marketAdapter.setBitmapList(bitmapList);
                marketAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Throwable e) {}

            @Override
            public void onComplete() {}
        });
    }

    @Override
    public boolean onBackPressed() {
        if(marketAdapter.isMenuActive()){
            marketAdapter.closeMenu();
            return true;
        }
        else return false;
    }
}