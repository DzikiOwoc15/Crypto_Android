package com.hfad.crypto;

public class Round {
    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }
}
