package com.kylemsguy.tcasmobile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

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

        mListAdapter = new ExpandableListAdapter(getActivity(),
                ((MainActivity) getActivity()).getmCurrQuestions());

        // set list adapter
        mExpListView.setAdapter(mListAdapter);

		return rootView;
	}

}
