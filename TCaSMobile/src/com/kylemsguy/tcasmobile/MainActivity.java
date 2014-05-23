package com.kylemsguy.tcasmobile;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.kylemsguy.tcasparser.AnswerManager;
import com.kylemsguy.tcasparser.SessionManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private SessionManager sm;
	private AnswerManager am;
	private Map<String, String> currQuestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		sm = ((TCaSApp) getApplicationContext()).getSessionManager();
		am = new AnswerManager(sm);

	}

	private Map<String, String> getNewQuestion() {
		try {
			return new GetQuestionTask().execute(sm).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void writeCurrQuestion() {
		TextView question = (TextView) findViewById(R.id.questionText);
		TextView id = (TextView) findViewById(R.id.questionId);

		question.setText(currQuestion.get("content"));
		id.setText(currQuestion.get("id"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void skipPerm(View view) {
		// TODO implement permanent skip of quesitons
	}

	public void skipTemp(View view) {
		currQuestion = getNewQuestion();
		writeCurrQuestion();
	}

	public void submitAnswer(View view) {
		// get ID
		String id = currQuestion.get("id");
		
		// get text
		EditText answerField = (EditText) findViewById(R.id.answerField);
		String answer = answerField.getText().toString();
		
		// Clear field
		answerField.setText("");

		Map<String, String> tempQuestion;
		
		try {
			tempQuestion = new SendAnswerTask().execute(
					id, answer, am).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if(tempQuestion != null){
			currQuestion = tempQuestion;
			writeCurrQuestion();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// TODO cookiemanager code here
		// CookieSyncManager.getInstance().startSync();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		// TODO cookiemanager code here
		// CookieSyncManager.getInstance().stopSync();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
