package com.hfad.crypto.addInvestment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.hfad.crypto.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoCoinSelectedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoCoinSelectedFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public NoCoinSelectedFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static NoCoinSelectedFragment newInstance(String param1, String param2) {
        NoCoinSelectedFragment fragment = new NoCoinSelectedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_no_coin_selected, container, false);

        return root;
    }
}