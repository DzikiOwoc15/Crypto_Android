package com.hfad.crypto.Wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.R;
import com.hfad.crypto.Round;
import com.hfad.crypto.database.CoinViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WalletFragment extends Fragment {
    private final String currency_save = "currency";
    private List<PortfolioItem> itemList = new ArrayList<>();
    List<Bitmap> bitmapList = new ArrayList<>();
    private Spinner currencySpinner;
    private TextView totalValueTextView;
    private CoinViewModel viewModel;
    private PortfolioAdapter adapter;
    private SharedPreferences sharedPreferences;
    private String currency;
    private Disposable priceDisposable;
    private Disposable imageDisposable;
    private Disposable currencyDisposable;
    private Disposable additionalDisposable;


    public WalletFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);
        //Test case v
        /*
        Portfolio_item item = new Portfolio_item(0.00829484, 1);
        itemList.add(item);
         */
        //Test case ^
        currencySpinner = root.findViewById(R.id.wallet_currency_spinner);
        totalValueTextView = root.findViewById(R.id.wallet_portfolio_value);
        RecyclerView recyclerView = root.findViewById(R.id.wallet_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PortfolioAdapter();
        recyclerView.setAdapter(adapter);
        SwipeRefreshLayout refreshLayout = root.findViewById(R.id.swipe_refresh);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        currency = sharedPreferences.getString(currency_save, "USD");
        Log.d("WalletFragment", currency);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CoinViewModel.class);
        viewModel.getAllPortfolioItems().observe(getViewLifecycleOwner(), items -> {
            itemList.addAll(items);
            getData(itemList);
            refreshLayout.setOnRefreshListener(() -> {
                getData(itemList);
                refreshLayout.setRefreshing(false);
            });
        });
        return root;

    }
    public void getData(List<PortfolioItem> items){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id=");
        List<Integer> ids = new ArrayList<>();
        for(int i = 0; i < items.size(); i++){
            ids.add(items.get(i).getDatabaseId());
            if(i != items.size() - 1){
                stringBuilder.append(items.get(i).getDatabaseId()).append(",");
            }
            else{
                stringBuilder.append(items.get(i).getDatabaseId());
            }
        }
        viewModel.convertURLtoJSON(ConstUrl.getUrlQuotes() +  stringBuilder.toString() + "&convert=" + currency + "&" + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                priceDisposable = d;
            }
            @SuppressLint("DefaultLocale")
            @Override
            public void onNext(@NonNull String s) {
                try {
                    JSONObject json = new JSONObject(s);
                    List<Double> amountList = new ArrayList<>();
                    List<Double> priceList = new ArrayList<>();
                    List<String> symbolList = new ArrayList<>();
                    for(int i = 0; i < items.size(); i++){
                        JSONObject obj = json.getJSONObject("data").getJSONObject(String.valueOf(items.get(i).getDatabaseId()));
                        double price = obj.getJSONObject("quote").getJSONObject(currency).getDouble("price");
                        String symbol = obj.getString("symbol");
                        symbolList.add(symbol);
                        priceList.add(price);
                        amountList.add(items.get(i).getAmount());
                        adapter.setSymbolList(symbolList);
                        adapter.setAmountList(amountList);
                        adapter.setPriceList(priceList);
                        adapter.notifyDataSetChanged();
                    }
                    double sum = 0;
                    for(int i = 0; i < priceList.size(); i++){
                        sum = sum + (priceList.get(i) * amountList.get(i));
                    }
                    sum = Round.round(sum, 2);
                    totalValueTextView.setText("");
                    totalValueTextView.setText(String.format("%s: \n %,.2f %s", getResources().getString(R.string.total_value), sum, currency));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {
            }
        });
        viewModel.convertUrlToJsonThenToBitmap(ConstUrl.getImageUrl() + stringBuilder.toString()  + ConstUrl.getApiKey(), ids).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Observable<Bitmap>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                imageDisposable = d;
            }
            @Override
            public void onNext(@NonNull Observable<Bitmap> bitmapObservable) {
                bitmapObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}
                    @Override
                    public void onNext(@NonNull Bitmap bitmap) {
                        bitmapList.add(bitmap);
                        adapter.setBitmapList(bitmapList);
                        adapter.notifyDataSetChanged();
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
        viewModel.convertURLtoJSON(ConstUrl.getURL_CURRENCY() + ConstUrl.getLIMIT() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                currencyDisposable = d;
            }
            @Override
            public void onNext(@NonNull String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    //For every currency entry add it to the Adapter
                    String[] currencies = new String[jsonObject.getJSONArray("data").length()];
                    int index = 0;
                    for(int i = 0; i < jsonObject.getJSONArray("data").length(); i++){
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        currencies[i] = jsonArray.getJSONObject(i).getString("symbol");
                        if(currencies[i].equals(currency)){
                            index = i;
                        }
                    }
                    String temp = currencies[index];
                    for(int i = index; i - 1 >= 0; i--){
                        currencies[i] = currencies[i - 1];
                    }
                    currencies[0] = temp;

                    ArrayAdapter<CharSequence> currencyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencies);
                    currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    currencySpinner.setAdapter(currencyAdapter);
                    currencySpinner.setSelection(0, true);

                    currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            currency = adapterView.getItemAtPosition(i).toString();
                            sharedPreferences.edit().putString(currency_save, currency).apply();
                            updateData(currency, items);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {}
                    });
                } catch (JSONException e) {
                    Log.d("JSON EXCEPTION",e.toString());
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {
                Log.d("JSON EXCEPTION",e.toString());
            }
            @Override
            public void onComplete() {}
        });
    }

    public void updateData(String currency, List<PortfolioItem> items){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id=");
        List<Integer> ids = new ArrayList<>();
        for(int i = 0; i < items.size(); i++){
            ids.add(items.get(i).getDatabaseId());
            if(i != items.size() - 1){
                stringBuilder.append(items.get(i).getDatabaseId()).append(",");
            }
            else{
                stringBuilder.append(items.get(i).getDatabaseId());
            }
        }
        viewModel.convertURLtoJSON(ConstUrl.getUrlQuotes() +  stringBuilder.toString() + "&convert=" + currency + "&" + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                additionalDisposable = d;
            }
            @SuppressLint("DefaultLocale")
            @Override
            public void onNext(@NonNull String s) {
                try {
                    JSONObject json = new JSONObject(s);
                    List<Double> amountList = new ArrayList<>();
                    List<Double> priceList = new ArrayList<>();
                    for(int i = 0; i < items.size(); i++){
                        double price = json.getJSONObject("data").getJSONObject(String.valueOf(items.get(i).getDatabaseId())).getJSONObject("quote").getJSONObject(currency).getDouble("price");
                        priceList.add(price);
                        amountList.add(items.get(i).getAmount());
                        adapter.setAmountList(amountList);
                        adapter.setPriceList(priceList);
                        adapter.notifyDataSetChanged();
                    }
                    double sum = 0;
                    for(int i = 0; i < priceList.size(); i++){
                        sum = sum + (priceList.get(i) * amountList.get(i));
                    }
                    sum = Round.round(sum, 2);
                    totalValueTextView.setText("");
                    totalValueTextView.setText(String.format("%s: \n %,.2f %s", getResources().getString(R.string.total_value), sum, currency));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(priceDisposable != null && !priceDisposable.isDisposed()){
            priceDisposable.dispose();
        }
        if(imageDisposable != null && !imageDisposable.isDisposed()){
            imageDisposable.dispose();
        }
        if(currencyDisposable != null && !currencyDisposable.isDisposed()){
            currencyDisposable.dispose();
        }
        if(additionalDisposable != null && !additionalDisposable.isDisposed()){
            additionalDisposable.dispose();
        }
    }
}