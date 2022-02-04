package com.hfad.crypto.database;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hfad.crypto.Objects.Investment;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.Objects.SimpleCoin;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class CoinViewModel extends AndroidViewModel {
    private final LiveData<List<SimpleCoin>> allSimpleCoins;
    final CoinRepository repository;
    private final LiveData<List<Investment>> listInvestments;

    public CoinViewModel(@NonNull Application application) {
        super(application);
        repository = new CoinRepository(application);
        allSimpleCoins = repository.getAllCoins();
        listInvestments = repository.getAllInvestments();
    }

    public LiveData<Integer> getSimpleCoinCount(){return repository.getSimpleCoinCount();}

    public LiveData<List<Investment>> getAllInvestments(){return listInvestments;}

    public void deleteCoin(int id){repository.deleteCoin(id);}

    public void insertCoin(SimpleCoin coin){repository.insertCoin(coin);}

    public void setDatabaseList(List<SimpleCoin> databaseList){
        repository.setDatabaseList(databaseList);
    }

    public void moveCoin(int from, int to){repository.moveCoin(from, to);}

    public LiveData<List<SimpleCoin>> getAllSimpleCoins(){return allSimpleCoins;}

    public void  insertInvestment(Investment investment){repository.insertInvestment(investment);}

    public Observable<String> convertURLtoJSON(String url){
        return repository.convertURLtoJSON(url);
    }

    public Observable<Observable<Bitmap>> convertUrlToJsonThenToBitmap(String url, List<Integer> ids){
        return repository.convertUrlToJsonThenToBitmap(url,ids);
    }

    public LiveData<List<PortfolioItem>> getAllPortfolioItems(){
        return repository.getAllPortfolioItems();
    }

    public void insertPortfolioItem(PortfolioItem item){
        repository.insertPortfolioItem(item);
    }
}
