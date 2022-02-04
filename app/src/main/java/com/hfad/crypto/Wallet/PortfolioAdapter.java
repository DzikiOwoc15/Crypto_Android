package com.hfad.crypto.Wallet;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.crypto.R;
import com.hfad.crypto.Round;

import java.util.ArrayList;
import java.util.List;

public class PortfolioAdapter  extends RecyclerView.Adapter<PortfolioAdapter.PortfolioHolder> {
    /*          *AmountList*
      -how much of a specific coin a user owns, used later to calculate  a value of this amount
                *BitmapList*
      -a list of icons that represent different coins
                *PriceList*
      -current market prices received from an API
     */
    private List<Double> amountList = new ArrayList<>();
    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<Double> priceList = new ArrayList<>();
    private List<String> symbolList = new ArrayList<>();
    @NonNull
    @Override
    public PortfolioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_wallet, parent, false);
        return new PortfolioHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull PortfolioHolder holder, int position) {
        if(amountList.size() > position){
            holder.amountView.setText(String.valueOf(amountList.get(position)));
        }
        if(bitmapList.size() > position){
            holder.imageView.setImageBitmap(bitmapList.get(position));
        }
        if(priceList.size() > position){
            holder.valueView.setText(String.format("%,.2f", Round.round(amountList.get(position) * priceList.get(position), 2)));
        }
        if(symbolList.size() > position){
            holder.symbolView.setText(symbolList.get(position));
        }
    }

    public void setAmountList(List<Double> list){
        this.amountList = list;
    }

    public void setBitmapList(List<Bitmap> list){
        this.bitmapList = list;
    }

    public void setPriceList(List<Double> priceList) {
        this.priceList = priceList;
    }

    public void setSymbolList(List<String> symbolList){
        this.symbolList = symbolList;
    }


    @Override
    public int getItemCount() {
        return amountList.size();
    }

    public static class PortfolioHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView amountView;
        private final TextView valueView;
        private final TextView symbolView;
        public PortfolioHolder(@NonNull View itemView) {
            super(itemView);
            symbolView = itemView.findViewById(R.id.wallet_coin_symbol);
            imageView = itemView.findViewById(R.id.wallet_coin_image);
            amountView = itemView.findViewById(R.id.wallet_coin_amount);
            valueView = itemView.findViewById(R.id.wallet_coin_value);
        }
    }
}
