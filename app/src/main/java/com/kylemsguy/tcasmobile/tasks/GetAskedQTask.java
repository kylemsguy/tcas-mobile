package com.kylemsguy.tcasmobile.tasks;

import android.os.AsyncTask;

import com.kylemsguy.tcasmobile.backend.Question;
import com.kylemsguy.tcasmobile.backend.QuestionManager;

import java.util.List;

public class GetAskedQTask extends AsyncTask<QuestionManager, Void, List<Question>> {
    @Override
    protected List<Question> doInBackground(QuestionManager... params) {
        try {
            return params[0].getQuestions();
        } catch (Exception e) {
            // Something went terribly wrong here
            e.printStackTrace();
            return null;
        }
    }
}
