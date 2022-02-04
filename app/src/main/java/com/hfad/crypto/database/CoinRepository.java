package com.hfad.crypto.database;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.hfad.crypto.Objects.Investment;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.Objects.SimpleCoin;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CoinRepository {
    private final LiveData<List<SimpleCoin>> allCoins;
    final SimpleCoinDao coinDao;
    private final LiveData<List<Investment>> allInvestments;
    private Object Void;

    public CoinRepository(Application application){
        CoinDatabase coinDatabase = CoinDatabase.buildDatabase(application);
        coinDao = coinDatabase.coinDao();
        allCoins = coinDao.getAllSimpleCoins();
        allInvestments = coinDao.getAllInvestments();
    }

    public LiveData<Integer> getSimpleCoinCount(){
        return coinDao.getSimpleCoinCount();
    }

    public LiveData<List<SimpleCoin>> getAllCoins(){return allCoins;}

    public void insertInvestment(Investment investment){
        insertInvestmentASYNC(investment);
    }

    private void insertInvestmentASYNC(Investment investment){
        Observable.fromCallable((Callable<Void>) () -> {
            coinDao.InsertInvestment(investment);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
    public LiveData<List<Investment>> getAllInvestments(){return allInvestments;}

   public void insertCoin(SimpleCoin coin){
        insertCoinASYNC(coin);
    }

    private void insertCoinASYNC(SimpleCoin simpleCoin){
        Observable.fromCallable((Callable<Void>) () -> {
            coinDao.InsertCoin(simpleCoin);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }
            @Override
            public void onNext(@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}

            @Override
            public void onComplete() {}
        });
    }

    public void deleteCoin(int id){
        deleteCoinASYNC(id);
    }

    private void deleteCoinASYNC(int id){
        Observable.fromCallable((Callable<Void>) () -> {
            coinDao.deleteCoin(id);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() { }
        });
    }

    public void moveCoin(int from, int to){moveCoinASYNC(from, to);}

    //Get all coins -> delete all -> shift an array -> insert
    private void moveCoinASYNC(int from, int to){
        Observable.fromCallable((Callable<Void>) () -> {
            coinDao.moveCoin(from, to);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public Observable<String> convertURLtoJSON(String url){
        return convertURLtoJSON_async(url);
    }
    /*
       This AsyncTask is converting URl to JsonObject
     */
    private Observable<String> convertURLtoJSON_async(String finalUrl){
       return Observable.fromCallable(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            URL url1 = new URL(finalUrl);
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
            return buffer.toString();
        }).doOnError(e -> {
           if (e instanceof UndeliverableException) {
               e = e.getCause();
           }
           if ((e instanceof IOException) || (e instanceof SocketException)) {
               // fine, irrelevant network problem or API that throws on cancellation
               return;
           }
           if (e instanceof InterruptedException) {
               // fine, some blocking code was interrupted by a dispose call
               return;
           }
           if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
               // that's likely a bug in the application
               Log.d("bug in the application", e.toString());
               Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
               return;
           }
           if (e instanceof IllegalStateException) {
               // that's a bug in RxJava or in a custom operator
               Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
               return;
           }
           Log.d("Undeliverable exception", e.toString());
       });
    }

    public Observable<Observable<Bitmap>> convertUrlToJsonThenToBitmap(String url, List<Integer> ids){
        return convertUrlToJsonThenToBitmapASYNC(url,ids);
    }

    private @NonNull Observable<Observable<Bitmap>> convertUrlToJsonThenToBitmapASYNC(String url, List<Integer> ids){
        return Observable.fromCallable(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            URL url1 = new URL(url);
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
            JSONObject jsonObject = new JSONObject(buffer.toString());
            List<String> logoURLs = new ArrayList<>();
            for(int i = 0; i < ids.size(); i++){
                logoURLs.add(jsonObject.getJSONObject("data").getJSONObject(String.valueOf(ids.get(i))).getString("logo"));
            }
            return Observable.fromIterable(logoURLs).flatMap(this::bitmapFromURL);
        }).onErrorComplete();
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
            connection.disconnect();
            input.close();
            return bitmap;
        }).onErrorComplete();
    }
    public LiveData<List<PortfolioItem>> getAllPortfolioItems(){
        return coinDao.getAllPortfolioItems();
    };

    public void insertPortfolioItem(PortfolioItem item){
        insertPortfolioItemsASYNC(item);
    }
    
    private void insertPortfolioItemsASYNC(PortfolioItem item){
        Observable.fromCallable((Callable<java.lang.Void>) () -> {
            coinDao.insertOrUpdate(item);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() { }
        });
    }
    
    public void setDatabaseList(List<SimpleCoin> databaseList){
        setDatabaseListASYNC(databaseList);
    }
    
    private void setDatabaseListASYNC(List<SimpleCoin> databaseListASYNC){
        Observable.fromCallable((Callable<java.lang.Void>) () -> {
            coinDao.setNewSimpleCoinList(databaseListASYNC);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<java.lang.Void>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(java.lang.@NonNull Void aVoid) {}
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
}
