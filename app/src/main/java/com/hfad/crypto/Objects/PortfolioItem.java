package com.hfad.crypto.Objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PortfolioItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private final int databaseId;
    private double amount;

    public PortfolioItem(double amount, int databaseId){
        this.amount = amount;
        this.databaseId = databaseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
