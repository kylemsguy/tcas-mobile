package com.kylemsguy.tcasmobile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.kylemsguy.tcasparser.SessionManager;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity {
	private static final String COOKIE_FILENAME = "cookies.txt";
	private final boolean enableSaveCredentials = false;

	private SessionManager sm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		sm = ((TCaSApp) getApplicationContext()).getSessionManager();

		if (enableSaveCredentials) {
			// Load cookies from file
			try {
				loadCookiesFromFile();
			} catch (IOException e) {
				System.out
						.println("cookies.txt not found. There are no cookies to load.");
			}

			// Check if logged in
			try {
				if (new GetLoggedInTask().execute(sm).get()) {
					Intent startMain = new Intent(this, MainActivity.class);
					startActivity(startMain);
					finish();
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO cookiemanager code here
			/*
			 * CookieSyncManager.createInstance(this);
			 * CookieSyncManager.getInstance().sync();
			 * 
			 * boolean loggedIn = false; try { loggedIn = new
			 * GetLoggedInTask().execute(sm).get(); } catch
			 * (InterruptedException | ExecutionException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 * 
			 * if (loggedIn) { Intent intent = new Intent(this,
			 * MainActivity.class); startActivity(intent); finish(); }
			 */
		}
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

	private boolean checkLoggedIn() {
		try {
			return new GetLoggedInTask().execute(sm).get();
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}

	private void loadCookiesFromFile() throws IOException {
		FileInputStream fis;
		StringBuilder sb = new StringBuilder();

		fis = openFileInput(COOKIE_FILENAME);

		int inputChar;
		while ((inputChar = fis.read()) > -1) {
			sb.append((char) inputChar);
		}

		System.out.println(sb.toString());

		List<String> inputData = Arrays.asList(sb.toString().split("\n"));

		sm.setCookies(inputData);
	}

	private void writeCookiesToFile() throws IOException {
		FileOutputStream fos = openFileOutput(COOKIE_FILENAME,
				Context.MODE_PRIVATE);

		for (String cookie : sm.getCookies()) {
			System.out.println(cookie);
			fos.write(cookie.getBytes());
		}
		fos.close();

	}

	private void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void loggestIn(View view) {
		EditText user = (EditText) findViewById(R.id.login_username);
		EditText pass = (EditText) findViewById(R.id.login_password);

		String username = user.getText().toString();
		String password = pass.getText().toString();

		// login
		try {
			new LoginTask().execute(username, password, sm).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// check if logged in
		boolean loggedIn = checkLoggedIn();

		if (!loggedIn) {
			showDialog("Login failed. Check your username or password.");
			return;
		} else {
			if (enableSaveCredentials) {
				try {
					System.out.println("Saving login credentials...");
					writeCookiesToFile();
				} catch (IOException e) {
					showDialog("Could not save login credentials.");
					e.printStackTrace();
				}
			}
		}

		// start the new activity
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		/*
		 * // DEBUG: get new question AsyncTask<SessionManager, Void, String>
		 * getQuestionTask = new GetQuestionTask() .execute(sm);
		 * 
		 * String result; try { result = getQuestionTask.get(); } catch
		 * (InterruptedException | ExecutionException e) { result =
		 * e.toString(); }
		 * 
		 * // DEBUG: Display whatever the result is as a toast
		 * Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
		 * .show();
		 */
		finish();
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
			View rootView = inflater.inflate(R.layout.fragment_login,
					container, false);
			return rootView;
		}
	}

}
