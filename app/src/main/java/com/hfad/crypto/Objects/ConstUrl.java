package com.hfad.crypto.Objects;

public class ConstUrl {
    private static final String URL_LISTING = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?";
    private static final String URL_QUOTES = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?";
    private static final String IMAGE_URL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/info?";
    private static final String API_KEY = "&CMC_PRO_API_KEY=696d749e-d02a-454a-8fe7-2ec2029776fe";
    private static final String URL_CURRENCY = "https://pro-api.coinmarketcap.com/v1/fiat/map?";
    private static final String LIMIT = "limit=199";

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getLIMIT() {
        return LIMIT;
    }

    public static String getImageUrl() {
        return IMAGE_URL;
    }

    public static String getUrlListing() {
        return URL_LISTING;
    }

    public static String getUrlQuotes() {
        return URL_QUOTES;
    }

    public static String getURL_CURRENCY() {
        return URL_CURRENCY;
    }
}
