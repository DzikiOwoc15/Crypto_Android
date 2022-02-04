package com.hfad.crypto.Objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SimpleCoin {
    @PrimaryKey
    private final int id;
    private int numberInOrder;
    private final String name;

    public SimpleCoin(int id, String name, int numberInOrder) {
        this.id = id;
        this.name = name;
        this.numberInOrder = numberInOrder;
    }

    public int getId() {
        return id;
    }

    public int getNumberInOrder() {
        return numberInOrder;
    }

    public void setNumberInOrder(int numberInOrder) {
        this.numberInOrder = numberInOrder;
    }

    public String getName() {
        return name;
    }

}
