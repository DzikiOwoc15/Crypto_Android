package com.hfad.crypto.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.hfad.crypto.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter implements Filterable {
    final Context context;
    private final List<String> coins;
    private List<String> tempCoins;
    private List<Bitmap> tempImages;
    private List<Bitmap> images;
    private static LayoutInflater inflater = null;
    private final ItemFilter filter = new ItemFilter();
    private boolean isNightModeOn = false;

    public ListAdapter(Context context, List<String> coins){
        this.context = context;
        this.coins = coins;
        tempCoins = coins;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setImages(List<Bitmap> images){
        this.images = images;
        tempImages = images;
    }

    public void setNightModeOn(){
        isNightModeOn = true;
    }

    public String getCoinName(int position){
        return tempCoins.get(position);
    }

    @Override
    public int getCount() {
        if(tempCoins == null){
            return 0;
        }
        return tempCoins.size();
    }

    @Override
    public Object getItem(int i) {
        return tempCoins.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view1 = view;
        if(view1 == null){
            view1 = inflater.inflate(R.layout.custom_list_view_item, viewGroup, false);
        }
        if(coins != null)
        if(tempCoins.size() > i){
            TextView text = view1.findViewById(R.id.text_item);
            text.setText(tempCoins.get(i));
            if(isNightModeOn){
                text.setTextColor(context.getResources().getColor(android.R.color.primary_text_dark_nodisable));
            }
        }
        if(images != null)
        if(tempImages.size() > i){
            ImageView imageView = view1.findViewById(R.id.image_item);
            imageView.setImageBitmap(tempImages.get(i));
        }
        return view1;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    @SuppressWarnings("unchecked")
    private class ItemFilter extends Filter{
        final List<Integer> imageIds = new ArrayList<>();
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            imageIds.clear();
            FilterResults filterResults = new FilterResults();
            List<String> filteredNames = new ArrayList<>();
            String filterString = constraint.toString().toLowerCase();
            String filterable;
            for(int i = 0; i < coins.size(); i++){
                filterable = coins.get(i);
                if(filterable.toLowerCase().contains(filterString)){
                    filteredNames.add(filterable);
                    imageIds.add(i);
                }
            }
            filterResults.values = filteredNames;
            filterResults.count = filteredNames.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            List<Bitmap> img = new ArrayList<>();
            for(int i = 0; i < imageIds.size(); i++){
                if(images.size() > imageIds.get(i))
                img.add(images.get(imageIds.get(i)));
            }
            tempImages = img;
            tempCoins = (List<String>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
