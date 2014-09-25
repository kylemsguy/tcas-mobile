package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AnswerFragment extends Fragment {
	
	private static final String ARG_QUESTION_ID = "question_id";
	private static final String ARG_QUESTION_CONTENT = "question_content";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_answer, container, false);
		TextView questionText = (TextView) view.findViewById(R.id.questionText);
		TextView questionId = (TextView) view.findViewById(R.id.questionId);
		
		questionText.setText(getArguments().getString(ARG_QUESTION_CONTENT));
		questionId.setText(getArguments().getString(ARG_QUESTION_ID));
		return view;
	}

}
