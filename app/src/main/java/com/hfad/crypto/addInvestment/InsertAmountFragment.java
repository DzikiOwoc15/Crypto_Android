package com.hfad.crypto.addInvestment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hfad.crypto.MainActivity;
import com.hfad.crypto.Objects.PortfolioItem;
import com.hfad.crypto.R;
import com.hfad.crypto.database.CoinViewModel;

public class InsertAmountFragment extends Fragment {


    public InsertAmountFragment() {
        // Required empty public constructor
    }

    public static InsertAmountFragment newInstance(String param1, String param2) {
        InsertAmountFragment fragment = new InsertAmountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_insert_amount, container, false);
        ImageView coinImageView = root.findViewById(R.id.fragment_insert_amount_image);
        TextView coinTextView = root.findViewById(R.id.fragment_insert_amount_coin_name);
        EditText amountEditText = root.findViewById(R.id.fragment_insert_amount_edit_text);
        Button button = root.findViewById(R.id.fragment_insert_amount_button);

        CoinViewModel coinViewModel = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CoinViewModel.class);

        InvestmentViewModel investmentViewModel = new ViewModelProvider(requireActivity()).get(InvestmentViewModel.class);
        investmentViewModel.getBitmapLiveData().observe(getViewLifecycleOwner(), bitmap -> {
            if(bitmap != null){
                coinImageView.setImageBitmap(bitmap);
                investmentViewModel.getIdLiveData().observe(getViewLifecycleOwner(), integer -> {
                    button.setOnClickListener(view -> {
                        if(!amountEditText.getText().toString().equals("")){
                            PortfolioItem itemToInsert = new PortfolioItem(Double.parseDouble(amountEditText.getText().toString()), integer);
                            coinViewModel.insertPortfolioItem(itemToInsert);
                            Intent intentStartActivity = new Intent(getContext(), MainActivity.class);
                            intentStartActivity.putExtra("VIEW_PAGER_ID", 1);
                            intentStartActivity.putExtra("SHOW_SNACKBAR", true);
                            startActivity(intentStartActivity);
                        }
                        else{
                            Snackbar.make(getView(), "Wprowadź ilość", 5000).show();
                            //Show error toast
                        }
                        //coinViewModel.insertPortfolioItem();
                        //Database update
                    });
                });
            }
        });
        investmentViewModel.getStringLiveData().observe(getViewLifecycleOwner(), s -> {
         coinTextView.setText(s);
        });


        return root;
    }
}