package com.hfad.crypto.market;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.crypto.Objects.Coin;
import com.hfad.crypto.Objects.SimpleCoin;
import com.hfad.crypto.R;

import java.util.ArrayList;
import java.util.List;

public class MarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static List<Coin> coins = new ArrayList<>();
    private static List<Bitmap> bitmapList = new ArrayList<>();
    private final ClickListener clickListener;
    private List<SimpleCoin> databaseList = new ArrayList<>();

    /*isConnected has 3 states, first one when the user launches the app and the internet connection has not been checked (loading circle is displayed),
     the second one when the connection has been checked and it failed to connect (no wifi signal is displayed),
     the third one when the user successfully connects with internet (proper recycler view is displayed);
     */
    private int isConnected = -1;
    private final int CONNECTION_NOT_CHECKED = -1;
    private final int CONNECTION_FAILED = 0;
    private final int CONNECTION_CONNECTED = 1;

    private final int MENU_SHOW = 1;
    private final int MENU_HIDE = 0;
    private final int MENU_LOADING = -1;

    public MarketAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(View view, int position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == MENU_HIDE){
            itemView =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_market, parent, false);
            return new MarketHolder(itemView);
        }
        else if(viewType == MENU_SHOW){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_market_menu_active, parent, false);
            return new MenuMarketHolder(itemView, clickListener);
        }
        else if(viewType == MENU_LOADING){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_market_loading, parent, false);
            return new ConnectionNotCheckedHolder(itemView);
        }
        else{
            itemView =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_no_connection, parent, false);
            return new NoConnectionHolder(itemView);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MarketHolder){
            Coin currentCoin;
            if(!(coins.size() - 1 < position)){
                currentCoin  = coins.get(position);
            }
            else{
                currentCoin = new Coin("loading",  0.0, 0.0, false);
            }
            Bitmap currentBitmap = null;
            if(!(bitmapList.size() - 1 < position)){
                currentBitmap = bitmapList.get(position);
            }
            if(currentBitmap != null){
                ((MarketHolder) holder).imageView.setImageBitmap(currentBitmap);
            }
            else{
                ((MarketHolder) holder).imageView.setImageResource(R.drawable.loading_coin_v2);
            }
            ((MarketHolder) holder).coinNameView.setText(currentCoin.getName());
            ((MarketHolder) holder).coinPriceView.setText(String.format("%,.2f", currentCoin.getPrice()));
            ((MarketHolder) holder).coinChange.setText(String.format("%.1f", currentCoin.getChange()) + "%");
            if(currentCoin.getChange() > 0){
                ((MarketHolder) holder).coinChange.setTextColor(((MarketHolder) holder).coinChange.getResources().getColor(R.color.green));
            }
            else if(currentCoin.getChange() < 0){
                ((MarketHolder) holder).coinChange.setTextColor(((MarketHolder) holder).coinChange.getResources().getColor(R.color.red));
            }
        }
        if(holder instanceof MenuMarketHolder){
            ((MenuMarketHolder) holder).deleteButton.setOnClickListener(view -> clickListener.onClick(view, position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int NO_CONNECTION = 2;
        if(isConnected == CONNECTION_CONNECTED){
            if (coins.get(position).isMenuActive()){
                return MENU_SHOW;
            }
            else{
                return MENU_HIDE;
            }
        }
        else if(isConnected == CONNECTION_NOT_CHECKED){
            return MENU_LOADING;
        }
        else
            return NO_CONNECTION;
    }

    @Override
    public int getItemCount() {
        if(isConnected == CONNECTION_CONNECTED){
            return coins.size();
        }
        else{
            return 1;
        }
    }

    public void moveCoin(int from, int to){
        Coin tempCoin = coins.get(from);
        Bitmap tempBitmap = bitmapList.get(from);
        SimpleCoin tempDatabase = databaseList.get(from);
        if(from < to){
            for(int i = from; i < to; i++){
                coins.set(i, coins.get(i + 1));
                databaseList.set(i, databaseList.get(i + 1));
                bitmapList.set(i, bitmapList.get(i + 1));
            }
        }
        if(from > to){
            for(int i = from; i > to; i--){
                coins.set(i, coins.get(i - 1));
                databaseList.set(i, databaseList.get(i - 1));
                bitmapList.set(i, bitmapList.get(i - 1));
            }
        }
        databaseList.set(to, tempDatabase);
        coins.set(to, tempCoin);
        bitmapList.set(to, tempBitmap);
    }

    public List<SimpleCoin> getDatabaseList(){
        for(int i = 0; i < databaseList.size(); i++){
            databaseList.get(i).setNumberInOrder(i + 1);
        }
        return databaseList;
    }

    public void isConnected(int connection){isConnected = connection;}

    public void setDatabaseList(List<SimpleCoin> databaseList){
        this.databaseList = databaseList;
    }

    public void setBitmapList(List<Bitmap> bitmapList){
        MarketAdapter.bitmapList = bitmapList;}

    public void setCoins(List<Coin> coinList){
        coins = coinList;
    }

    public void showMenu(int position){
        if(isConnected == CONNECTION_CONNECTED){
            for(int i = 0; i < coins.size(); i++){
                coins.get(i).setMenuActive(false);
            }
            coins.get(position).setMenuActive(true);
            notifyItemChanged(position);
        }
    }

    public List<Coin> getCoins(){
        return  coins;
    }

    public boolean isMenuActive(){
        for(int i = 0; i < coins.size(); i++){
            if(coins.get(i).isMenuActive()){
                return true;
            }
        }
        return false;
    }

    public void closeMenu(){
        for(int i = 0; i < coins.size(); i++){
            coins.get(i).setMenuActive(false);
        }
        notifyDataSetChanged();
    }

     static class MarketHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView coinNameView;
        private final TextView coinPriceView;
        private final TextView coinChange;

        public MarketHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.coin_image);
            coinNameView = itemView.findViewById(R.id.coin_name);
            coinPriceView = itemView.findViewById(R.id.coin_price);
            coinChange = itemView.findViewById(R.id.coin_change);
        }
     }

     public static class MenuMarketHolder extends RecyclerView.ViewHolder implements ClickListener{
        private final ImageButton deleteButton;
         public MenuMarketHolder(@NonNull View itemView, ClickListener listener) {
             super(itemView);
             deleteButton = itemView.findViewById(R.id.card_market_delete);
         }

         @Override
         public void onClick(View view, int position) {

         }
     }
     public static class ConnectionNotCheckedHolder extends RecyclerView.ViewHolder{
         public ConnectionNotCheckedHolder(@NonNull View itemView) {
             super(itemView);
         }
     }

     public static class NoConnectionHolder extends RecyclerView.ViewHolder{
         public NoConnectionHolder(@NonNull View itemView) {
             super(itemView);
         }
     }
}
