package com.hfad.crypto.addInvestment;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InvestmentViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> bitmapLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> stringLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer>  idLiveData = new MutableLiveData<>();

    public void setBitmap(Bitmap bitmap){
        bitmapLiveData.setValue(bitmap);
    }

    public void setName(String name){
        stringLiveData.setValue(name);
    }

    public void setId(Integer id){idLiveData.setValue(id);}

    public MutableLiveData<Integer> getIdLiveData() {
        return idLiveData;
    }
    public MutableLiveData<Bitmap> getBitmapLiveData(){
        return bitmapLiveData;
    }

    public MutableLiveData<String> getStringLiveData() {
        return stringLiveData;
    }
}
