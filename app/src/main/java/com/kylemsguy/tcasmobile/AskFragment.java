package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.kylemsguy.tcasmobile.backend.Question;

import java.util.List;

public class AskFragment extends Fragment {

    // stuff for Asked questions
    private ExpandableListAdapter mListAdapter;
    private ExpandableListView mExpListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ask, container,
                false);

        // Set up the ListAdapter for AskActivity
        mExpListView = (ExpandableListView) rootView.findViewById(R.id.questionList);
        ((MainActivity) getActivity()).refreshQuestionList();
        mListAdapter = new ExpandableListAdapter(getActivity(),
                ((MainActivity) getActivity()).getmCurrQuestions());
        mListAdapter.notifyDataSetChanged();
        // set list adapter
        mExpListView.setAdapter(mListAdapter);


        return rootView;
    }

}
