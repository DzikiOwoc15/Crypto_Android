package com.hfad.crypto.Objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Investment {
    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int coinId;
    private String coinName;
    private float amount;
    private float price;

    public Investment() {
    }

    public Investment(int id, String name, float amount, float price) {
        this.coinId = id;
        this.coinName = name;
        this.amount = amount;
        this.price = price;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public int getId() {
        return id;
    }

    public int getCoinId() {
        return coinId;
    }

    public void setCoinId(int coinId) {
        this.coinId = coinId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
