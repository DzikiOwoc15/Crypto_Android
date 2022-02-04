package com.hfad.crypto.Objects;

public class Coin {
    private final String name;
    private final double price;
    private final double change;
    private boolean isMenuActive;


    public Coin(String name, double price, double change, boolean menu) {
        this.name = name;
        this.price = price;
        this.change = change;
        this.isMenuActive = menu;
    }

    public boolean isMenuActive() {
        return isMenuActive;
    }

    public void setMenuActive(boolean menuActive) {
        isMenuActive = menuActive;
    }

    public String getName() {
        return name;
    }


    public double getPrice() {
        return price;
    }

    public double getChange() {
        return change;
    }

}
