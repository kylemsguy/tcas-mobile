package com.kylemsguy.tcasmobile;


import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.InfoManager;
import com.kylemsguy.tcasmobile.backend.RecentQuestion;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String ARG_USERNAME = "username";

    private InfoManager im;

    private List<RecentQuestion> recentQuestionList;

    private RecyclerView questionListView;
    private TextView userData;
    private TextView miscData;

    private String username;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String username) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle homeArgs = new Bundle();
        homeArgs.putString("username", username);
        homeFragment.setArguments(homeArgs);
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        im = ((TCaSApp) getActivity().getApplicationContext()).getInfoManager();

        // TODO get O'Harean Date
        //String date = OhareanCalendar.
        // TODO: get version history from GitHub
        String versionHistory = "\nVersion History:\nv1.0a:\nFirst release of app onto Google Play.\n";
        String greeting = "\nWelcome to TCaS Mobile. I am planning to put more information here in " +
                "the future.\n" +
                "";

        userData = (TextView) rootView.findViewById(R.id.user_data);
        miscData = (TextView) rootView.findViewById(R.id.misc_data);

        // TODO get more info for userdata
        String username = null;
        Bundle bundle = getArguments();
        if (bundle != null)
            username = getArguments().getString(ARG_USERNAME);

        if (username == null) {
            username = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREF_LOGGED_IN_KEY, null);
        }

        userData.setText("\nUsername: " + username);
        userData.setTypeface(null, Typeface.BOLD);
        //miscData.setText(versionHistory + greeting);
        miscData.setVisibility(View.GONE);

        // TODO set up the view
        recentQuestionList = new ArrayList<>();

        questionListView = (RecyclerView) rootView.findViewById(R.id.recent_question_list);


        try {
            updateRecentQuestionList();
        } catch (Exception e) {
            System.err.println("HomeFragment:");
            e.printStackTrace();
        }

        return rootView;
    }

    private void updateRecentQuestionList() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    im.updateInfo();
                } catch (Exception e) {
                    System.out.println("HomeFragment: Failed to update recent question list");
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateRecentQuestionListCallback();
            }

        }.execute();

        // notify data set changed & stuff
    }

    private void updateRecentQuestionListCallback() {
        List<RecentQuestion> questions;
        questions = im.getRecentQuestions();
        recentQuestionList.clear();
        recentQuestionList.addAll(questions);
    }


}
