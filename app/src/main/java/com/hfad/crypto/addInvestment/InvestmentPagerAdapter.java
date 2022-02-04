package com.hfad.crypto.addInvestment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.io.ByteArrayOutputStream;

public class InvestmentPagerAdapter  extends FragmentStatePagerAdapter {
    private Bitmap bitmap;
    private String name;

    public InvestmentPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        if(bitmap != null && name != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap.recycle();
            bundle.putByteArray("IMG", byteArray);
            bundle.putString("NAME", name);
        }
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new SelectCoinFragment();
                break;
            case 1:
                if(bitmap != null){
                    Log.d("PAGER", "Bitmap != null");
                    fragment = new InsertAmountFragment();
                    fragment.setArguments(bundle);
                }
                else{
                    Log.d("PAGER", "Bitmap == null");
                    fragment = new NoCoinSelectedFragment();
                }
                break;
        }
        return fragment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
