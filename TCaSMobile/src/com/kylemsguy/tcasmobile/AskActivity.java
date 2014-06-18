package com.kylemsguy.tcasmobile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.kylemsguy.tcasmobile.tasks.AskQuestionTask;
import com.kylemsguy.tcasparser.Question;
import com.kylemsguy.tcasparser.QuestionManager;
import com.kylemsguy.tcasparser.SessionManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.os.Build;

public class AskActivity extends ActionBarActivity {

	private SessionManager sm;
	private QuestionManager qm;
	private Map<Integer, Question> currQuestions;
	
	private ArrayAdapter<String> adapter;
	private List<String> listItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		sm = ((TCaSApp) getApplicationContext()).getSessionManager();
		qm = new QuestionManager(sm);
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

	public void askQuestion(View view) {
		// get text
		EditText askQuestionField = (EditText) findViewById(R.id.askQuestionField);
		String question = askQuestionField.getText().toString();
		
		if(question.equals("")){
			showNotifDialog("You cannot send a blank message.");
			return;
		}

		// Clear field
		askQuestionField.setText("");

		String response = null;
		// send question to be asked.
		try {
			response = new AskQuestionTask().execute(qm, question).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

			refreshQuestionList();
	}

	public void refreshQuestionList() {
		try {
			currQuestions = qm.getQuestions();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO implement dynamic list view
	}

	public void showNotifDialog(String contents) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(contents);
		builder.setPositiveButton("OK", null);
		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
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
			View rootView = inflater.inflate(R.layout.fragment_ask, container,
					false);
			return rootView;
		}
	}

}
