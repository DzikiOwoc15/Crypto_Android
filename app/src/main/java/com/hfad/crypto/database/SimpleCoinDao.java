package com.hfad.crypto.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hfad.crypto.Objects.Investment;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.Objects.SimpleCoin;

import java.util.List;

@Dao
public interface SimpleCoinDao {
    @Query("SELECT *  FROM simplecoin ORDER BY numberInOrder ASC")
    LiveData<List<SimpleCoin>> getAllSimpleCoins();

    @Query("SELECT * FROM simplecoin ORDER BY numberInOrder ASC")
    List<SimpleCoin> getAllSimpleCoinsNonLive();

    @Query("SELECT COUNT(id) FROM simplecoin")
    LiveData<Integer> getSimpleCoinCount();

    @Insert
    void insertAllSimpleCoins(List<SimpleCoin> items);

    @Query("DELETE FROM simplecoin")
    void deleteAllCoins();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPortfolioItems(List<PortfolioItem> items);

    @Delete
    void deletePortfolioItem(PortfolioItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPortfolioItem(PortfolioItem item);

    @Query("SELECT * FROM PortfolioItem")
    LiveData<List<PortfolioItem>> getAllPortfolioItems();

    @Query("SELECT * FROM PortfolioItem WHERE databaseId= :databaseId")
    List<PortfolioItem> getItemListByID(int databaseId);

    @Insert
    void InsertAll(List<SimpleCoin> coinList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertCoin(SimpleCoin coin);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertInvestment(Investment investment);

    @Query("SELECT * FROM investment")
    LiveData<List<Investment>> getAllInvestments();

    @Query("DELETE FROM simplecoin WHERE id = :id")
    void deleteCoin(int id);

    default void moveCoin(int fromPos, int toPos){
        List<SimpleCoin> coinList = getAllSimpleCoinsNonLive();
        coinList.get(fromPos).setNumberInOrder(fromPos);
        if(fromPos < toPos){
            for(int i = fromPos + 1; i <= toPos && i < coinList.size(); i++){
                coinList.get(i).setNumberInOrder(coinList.get(i).getNumberInOrder() - 1);
            }
        }
        if(fromPos > toPos){
            for(int i = fromPos - 1; i >= toPos && i >= 0; i--){
                coinList.get(i).setNumberInOrder(coinList.get(i).getNumberInOrder() + 1);
            }
        }
        deleteAllCoins();
        insertAllSimpleCoins(coinList);
    }

    default void setNewSimpleCoinList(List<SimpleCoin> list){
        insertAllSimpleCoins(list);
    }

    default void insertOrUpdate(PortfolioItem portfolioItem){
        List<PortfolioItem> itemsFromDB = getItemListByID(portfolioItem.getDatabaseId());
        if(itemsFromDB.size() == 0){
            insertPortfolioItem(portfolioItem);
        }
        else{
            double sum = itemsFromDB.get(0).getAmount() + portfolioItem.getAmount();
            PortfolioItem newItem = new PortfolioItem(sum, portfolioItem.getDatabaseId());
            deletePortfolioItem(itemsFromDB.get(0));
            insertPortfolioItem(newItem);
        }
    }
}
