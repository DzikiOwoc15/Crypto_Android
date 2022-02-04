package com.hfad.crypto.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.hfad.crypto.Objects.Coin;
import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.R;
import com.hfad.crypto.Objects.SimpleCoin;
import com.hfad.crypto.database.CoinDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory{
    private final Context context;
    List<SimpleCoin> list;
    List<Coin> coinList;
    private String currency;

    public WidgetFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        CoinDatabase coinDatabase = CoinDatabase.buildDatabase(context);
        list = coinDatabase.coinDao().getAllSimpleCoinsNonLive();
        coinList = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences currencyPreferences = context.getApplicationContext().getSharedPreferences(String.valueOf(R.string.setting_currency), Context.MODE_PRIVATE);
        currency = currencyPreferences.getString(String.valueOf(R.string.setting_currency), "USD");
        if(currency.equals("7 dni")){
            currency = "USD";
        }
        list.clear();
        CoinDatabase coinDatabase = CoinDatabase.buildDatabase(context);
        list = coinDatabase.coinDao().getAllSimpleCoinsNonLive();
        String s = "";
        StringBuilder id = new StringBuilder("id=" + list.get(0).getId());
        for (int j = 0; j < list.size(); j++) {
            id.append(",");
            id.append(list.get(j).getId());
        }
        String finalUrl = ConstUrl.getUrlQuotes() + id.toString() + ConstUrl.getApiKey();
        try {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            java.net.URL url1 = new URL(finalUrl);
            connection = (HttpURLConnection) url1.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            connection.disconnect();
            reader.close();
            s = buffer.toString();

        }
        catch (java.io.IOException e){
            Log.d("Widget Factory", e.toString());
        }
        if (s != null) {
            try {
                JSONObject object = new JSONObject(s);
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObject = object.getJSONObject("data").getJSONObject(String.valueOf(list.get(i).getId()));
                    Log.d("Widget", "Coin -> " + jsonObject.getString("name") +  jsonObject.getJSONObject("quote").getJSONObject("USD").getDouble("price") + jsonObject.getJSONObject("quote").getJSONObject("USD").getDouble("percent_change_7d"));
                    Coin coin = new Coin(jsonObject.getString("name"), jsonObject.getJSONObject("quote").getJSONObject(currency).getDouble("price"), jsonObject.getJSONObject("quote").getJSONObject("USD").getDouble("percent_change_7d"), false);
                    coinList.add(coin);
                    getViewAt(i);
                }

            } catch (JSONException e) {
                Log.d("Widget", "Coin -> " + e.toString());
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Log.d("Widget", "Coin size getViewAt -> " + list.size() + "item num ->" + i);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.crypto_widget_list_item);
        if(coinList !=  null)
        if(coinList.size() > i) {
            Log.d("Widget", "Coin position added" + coinList.get(i).getName());
            remoteViews.setTextViewText(R.id.widgetItemTaskNameLabel, coinList.get(i).getName());
            remoteViews.setTextViewText(R.id.widgetItemPrice, String.format(Locale.getDefault(),"%,.2f", coinList.get(i).getPrice()) + " " + currency);
            if(coinList.get(i).getChange() > 0){
                int green =  context.getResources().getColor(R.color.green);
                remoteViews.setTextColor(R.id.widgetItemPrice, green);
                remoteViews.setTextColor(R.id.widgetItemTaskNameLabel, green);
            }
            else{
                int red =  context.getResources().getColor(R.color.red);
                remoteViews.setTextColor(R.id.widgetItemPrice, red);
                remoteViews.setTextColor(R.id.widgetItemTaskNameLabel, red);
            }
        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
