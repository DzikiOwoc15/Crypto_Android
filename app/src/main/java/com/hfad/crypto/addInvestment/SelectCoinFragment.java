package com.hfad.crypto.addInvestment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.crypto.Objects.ConstUrl;
import com.hfad.crypto.R;
import com.hfad.crypto.database.CoinViewModel;
import com.hfad.crypto.ui.main.ListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SelectCoinFragment extends Fragment {

    private static final List<String> coinList = new ArrayList<>();
    private static final List<Integer> idList = new ArrayList<>();
    private static final List<Bitmap> bitmapList = new ArrayList<>();
    private Disposable disposable;
    private Disposable disposableBitmap;
    private Disposable disposableBitmap2;

    public SelectCoinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView  = inflater.inflate(R.layout.fragment_select_coin, container, false);
        ListView listView = rootView.findViewById(R.id.select_coin_list_view);
        InvestmentViewModel investmentViewModel = new ViewModelProvider(requireActivity()).get(InvestmentViewModel.class);
        CoinViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CoinViewModel.class);
        ListAdapter listAdapter = new ListAdapter(getContext(), coinList);
        boolean isNightModeOn =  AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        if(isNightModeOn){
            listAdapter.setNightModeOn();
        }
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            AddInvestmentActivity activity = (AddInvestmentActivity) getActivity();
            investmentViewModel.setBitmap(bitmapList.get(i));
            investmentViewModel.setName(coinList.get(i));
            investmentViewModel.setId(idList.get(i));
            activity.setViewPagerPage(1);
        });
        listView.setAdapter(listAdapter);
        listAdapter.setImages(bitmapList);
        viewModel.convertURLtoJSON(ConstUrl.getUrlListing() + ConstUrl.getLIMIT() + ConstUrl.getApiKey()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }
            @Override
            public void onNext(@NonNull String s) {
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for(int i = 0; i < jsonObject.getJSONArray("data").length(); i++){
                        coinList.add(jsonArray.getJSONObject(i).getString("name"));
                        idList.add(jsonArray.getJSONObject(i).getInt("id"));
                    }
                    Log.d("Inv", coinList.get(0));
                    StringBuilder imageIds =  new StringBuilder("id=" + idList.get(0));
                    for(int i = 1; i < idList.size(); i++){
                        imageIds.append(",");
                        imageIds.append(idList.get(i));
                    }
                    listAdapter.notifyDataSetChanged();
                    Log.d("Inv", ConstUrl.getImageUrl() + imageIds + ConstUrl.getApiKey());
                    viewModel.convertUrlToJsonThenToBitmap(ConstUrl.getImageUrl() + imageIds + ConstUrl.getApiKey(), idList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Observable<Bitmap>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            disposableBitmap = d;
                        }
                        @Override
                        public void onNext(@NonNull Observable<Bitmap> bitmapObservable) {
                            bitmapObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Bitmap>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                    disposableBitmap2 = d;
                                }
                                @Override
                                public void onNext(@NonNull Bitmap bitmap) {
                                    bitmapList.add(bitmap);
                                    listAdapter.notifyDataSetChanged();
                                }
                                @Override
                                public void onError(@NonNull Throwable e) {}
                                @Override
                                public void onComplete() {}
                            });
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {}
                        @Override
                        public void onComplete() {}
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(@NonNull Throwable e) {}
            @Override
            public void onComplete() {}
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
        if(disposableBitmap != null && !disposableBitmap.isDisposed()){
            disposableBitmap.dispose();
        }
        if(disposableBitmap2 != null && !disposableBitmap2.isDisposed()){
            disposableBitmap2.dispose();
        }
    }
}