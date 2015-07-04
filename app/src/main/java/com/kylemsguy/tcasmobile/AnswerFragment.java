package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AnswerFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnswerFragment.
     */
    public static AnswerFragment newInstance() {
        return new AnswerFragment();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_answer, container, false);

        // TODO Make ActionBar only hide when keyboard activated
        final ActionBar actionBar = ((AppCompatActivity) view.getContext()).getSupportActionBar();

        final EditText answerField = (EditText) view.findViewById(R.id.answerField);

        answerField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // Let's hide that action bar
                    if (actionBar != null)
                        actionBar.hide();
                } else {
                    if (actionBar != null)
                        actionBar.show();
                }
            }
        });
        return view;
	}

}
