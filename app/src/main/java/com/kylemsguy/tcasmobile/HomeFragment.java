package com.kylemsguy.tcasmobile;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Type;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String ARG_USERNAME = "username";

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // TODO get O'Harean Date
        //String date = OhareanCalendar.
        // TODO: get version history from GitHub
        String versionHistory = "\nVersion History:\nv1.0a:\nFirst release of app onto Google Play.\n";
        String greeting = "\nWelcome to TCaS Mobile. I am planning to put more information here in " +
                "the future.\n" +
                "";

        TextView userData = (TextView) rootView.findViewById(R.id.user_data);
        TextView miscData = (TextView) rootView.findViewById(R.id.misc_data);

        // TODO get more info for userdata
        userData.setText("\nUsername: " + getArguments().getString(ARG_USERNAME));
        userData.setTypeface(null, Typeface.BOLD);
        miscData.setText(versionHistory + greeting);

        return rootView;
    }


}
