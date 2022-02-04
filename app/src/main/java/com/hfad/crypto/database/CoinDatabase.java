package com.hfad.crypto.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hfad.crypto.Objects.Investment;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.Objects.SimpleCoin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(version = 1, entities = {SimpleCoin.class, Investment.class, PortfolioItem.class}, exportSchema = false)
public abstract class CoinDatabase extends RoomDatabase {
    private static CoinDatabase INSTANCE;

    public abstract SimpleCoinDao coinDao();


    public static synchronized CoinDatabase buildDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CoinDatabase.class, "coin_database").addCallback(btcCallback).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return INSTANCE;
    }

    //The Database will always contain BTC's id, It's the only coin that user will see at first launch until he adds more
    private static final RoomDatabase.Callback btcCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            populateDatabaseASYNC(INSTANCE);
        }
    };

    private static void populateDatabaseASYNC(CoinDatabase database){
        Observable.fromCallable((Callable<Void>) () -> {
            database.coinDao().InsertAll(coinList());
            database.coinDao().insertAllPortfolioItems(items());
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Void>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Void aVoid) {

            }
            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    private static List<PortfolioItem> items(){
        List<PortfolioItem> items = new ArrayList<>();
        PortfolioItem item = new PortfolioItem(0.01, 1);
        items.add(item);
        return items;
    }

    public  static List<SimpleCoin> coinList(){
        List<SimpleCoin> coinList = new ArrayList<>();
        coinList.add(new SimpleCoin(1, "Bitcoin", 1));
        return coinList;
    }
}
