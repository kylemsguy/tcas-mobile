package com.kylemsguy.tcasmobile;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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

    List<RecentQuestion> recentQuestionList;

    ExpandableListView questionListView;

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

        TextView userData = (TextView) rootView.findViewById(R.id.user_data);
        TextView miscData = (TextView) rootView.findViewById(R.id.misc_data);

        // TODO get more info for userdata
        String username = getArguments().getString(ARG_USERNAME);
        if (username == null) {
            username = PrefUtils.getFromPrefs(getActivity(), PrefUtils.PREF_LOGGED_IN_KEY, null);
        }
        userData.setText("\nUsername: " + username);
        userData.setTypeface(null, Typeface.BOLD);
        //miscData.setText(versionHistory + greeting);
        miscData.setVisibility(View.GONE);

        // TODO set up the view
        recentQuestionList = new ArrayList<>();

        questionListView = (ExpandableListView) rootView.findViewById(R.id.recent_question_list);

        try {
            updateRecentQuestionList();
        } catch (Exception e) {
            System.err.println("HomeFragment:");
            e.printStackTrace();
        }

        return rootView;
    }

    private void updateRecentQuestionList() throws Exception {
        List<RecentQuestion> questions = null;

        im.updateInfo();
        questions = im.getRecentQuestions();

        recentQuestionList.clear();
        recentQuestionList.addAll(questions);

        // notify data set changed & stuff
    }


}
