package com.kylemsguy.tcasmobile;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.kylemsguy.tcasmobile.tasks.AskQuestionTask;
import com.kylemsguy.tcasmobile.tasks.GetQuestionTask;
import com.kylemsguy.tcasmobile.tasks.SendAnswerTask;
import com.kylemsguy.tcasmobile.tasks.SkipQuestionTask;
import com.kylemsguy.tcasparser.AnswerManager;
import com.kylemsguy.tcasparser.Question;
import com.kylemsguy.tcasparser.QuestionManager;
import com.kylemsguy.tcasparser.SessionManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private SessionManager sm;
	private QuestionManager qm;
	private AnswerManager am;

	private Map<String, String> currQuestion;
	private Map<Integer, Question> currQuestions;

	// stuff for Asked questions
	private ArrayAdapter<String> adapter;
	private List<String> listItems;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		sm = ((TCaSApp) getApplicationContext()).getSessionManager();
		qm = new QuestionManager(sm);
		am = new AnswerManager(sm);
		
		currQuestion = getNewQuestion();
	}
	
	// BEGIN AskActivity
	public void askQuestion(View view) {
		// get text
		EditText askQuestionField = (EditText) findViewById(R.id.askQuestionField);
		String question = askQuestionField.getText().toString();

		if (question.equals("")) {
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

	// END AskActivity

	// BEGIN AnswerActivity
	private void writeCurrQuestion() {
		TextView question = (TextView) findViewById(R.id.questionText);
		LinearLayout idWrapper = (LinearLayout) findViewById(R.id.idLinearLayout);
		TextView id = (TextView) idWrapper.findViewById(R.id.questionId);

		question.setText(currQuestion.get("content"));
		id.setText(currQuestion.get("id"));
	}

	private void skipQuestion(boolean forever) {
		if (currQuestion != null) {
			Map<String, String> tempQuestion = null;
			try {
				tempQuestion = new SkipQuestionTask().execute(sm,
						currQuestion.get("id"), forever).get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return; // ABORT ABORT
			}
			if (tempQuestion == null) {
				return; // ABORT ABORT
			} else {
				currQuestion = tempQuestion;
				writeCurrQuestion();
			}
		} else {
			getFirstQuestion();
		}
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

	public void getFirstQuestion() {
		((Button) findViewById(R.id.btnSubmit)).setText("Submit");
		currQuestion = getNewQuestion();
		writeCurrQuestion();
	}

	public void skipPerm(View view) {
		skipQuestion(true);
	}

	public void skipTemp(View view) {
		skipQuestion(false);
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
			tempQuestion = new SendAnswerTask().execute(id, answer, am).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return;
		}
		if (tempQuestion != null) {
			currQuestion = tempQuestion;
			writeCurrQuestion();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Failed to send message. "
					+ "Your message may be too short or unoriginal.");
			builder.setPositiveButton("OK", null);
			builder.setCancelable(true);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	// end AnswerActivity

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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			// return PlaceholderFragment.newInstance(position + 1);
			switch (position) {
			case 0:
				return PlaceholderFragment.newInstance(position + 1);
			case 1:
				return new AskFragment();
			case 2:
				AnswerFragment fragment = new AnswerFragment();
				Bundle args = new Bundle();
				args.putString("question_id", currQuestion.get("id"));
				args.putString("question_content", currQuestion.get("content"));
				fragment.setArguments(args);
				return fragment;
			case 3:
				return PlaceholderFragment.newInstance(position + 1);
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

}
