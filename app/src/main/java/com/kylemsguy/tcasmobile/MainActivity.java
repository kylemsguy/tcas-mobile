package com.kylemsguy.tcasmobile;

import java.util.Locale;

import com.kylemsguy.tcasmobile.tasks.GetLoggedInTask;
import com.kylemsguy.tcasmobile.tasks.LogoutTask;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GetLoggedInTask.OnPostLoginCheckListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this
     * becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ActionBar actionBar;

    private SessionManager sm;

    private AsyncTask mLogoutTask;
    private AsyncTask mGetLoggedInTask;

    private AskFragment mAskFragment;
    private AnswerFragment mAnswerFragment;
    private MessageFragment mMessageFragment;

    private static final boolean DEBUG = false;

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

        // prevent destruction of fragments by scrolling offscreen
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());

        // Disable keyboard when unnecessary
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: // Home
                        // let's hide that keyboard
                        View currentFocus = getCurrentFocus();
                        if (currentFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                        break;
                    case 1: // AskActivity
                        break;
                    case 2: // AnswerActivity
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Get the SessionManager
        sm = ((TCaSApp) getApplicationContext()).getSessionManager();

    }

    /**
     * Methods for each Fragment
     */

    // BEGIN HomeActivity
    public void jumpToSection(View view) {
        switch (view.getId()) {
            case R.id.debug_jumpto_ask:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.debug_jumpto_answer:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.debug_jumpto_messages:
                mViewPager.setCurrentItem(3);
                break;
        }
    }

    // END HomeActivity

    // BEGIN AskActivity

    public void askQuestion(View view) {
        mAskFragment.askQuestion(view);
    }

    // END AskActivity


    // BEGIN AnswerActivity
    public void skipTemp(View view) {
        mAnswerFragment.skipTemp();
    }

    public void skipPerm(View view) {
        mAnswerFragment.skipPerm();
    }

    public void submitAnswer(View view) {
        mAnswerFragment.submitAnswer();
    }

    // end AnswerActivity

    // start MessageActivity


    public void prevPage(View v) {
        mMessageFragment.prevPage(v);
    }


    public void nextPage(View v) {
        mMessageFragment.nextPage(v);
    }


    // end MessageActivity

    /**
     * Misc. methods for MainActivity
     */

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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // save logout task in case we need to cancel
            mLogoutTask = new LogoutTask().execute(sm);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, null);
            Intent intent = new Intent(this, LoginActivity.class);
            // do stuff
            /*try { // temporary; change to a wheel spinning and dialog saying "logging out..."
                logout.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }*/
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // Save cookies to SharedPreferences
        //PrefUtils.saveListToPrefs(this, PrefUtils.PREF_COOKIES_KEY, sm.getCookies());
        //PrefUtils.saveCookieStoreToPrefs(this, PrefUtils.PREF_COOKIESTORE_KEY, sm.getCookieStore());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload cookies from SharedPreferences
        //List<String> cookies = PrefUtils.getStringListFromPrefs(this, PrefUtils.PREF_COOKIES_KEY);
        //sm.setCookies(cookies);

        // Reload cookies from SharedPreferences
        // TODO: deal with the tight coupling going on here o_o
        ((PersistentCookieStore) sm.getCookieStore()).getCookiesFromPrefs();

        // check if logged in
        mGetLoggedInTask = new GetLoggedInTask().execute(sm, this);

    }

    @Override
    public void onBackPressed() {
    /*
        // show a message asking if really want to close
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sure_quit);
        builder.setPositiveButton("OK", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE)
                    // Close app.
                    finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    */

        // move to previous page in ViewPager
        int currentPage = mViewPager.getCurrentItem();
        if (currentPage - 1 >= 0)
            mViewPager.setCurrentItem(currentPage - 1);
        else
            super.onBackPressed();

        // logout here
        //mLogoutTask = new LogoutTask().execute(sm);
    }

    /**
     * Callbacks for AsyncTasks
     */

    @Override
    public void onPostLoginCheck(boolean loggedIn) {
        if (!loggedIn) {
            // kick back to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            // let user know session expired
            Toast toast = Toast.makeText(getApplicationContext(), R.string.session_expired, Toast.LENGTH_SHORT);
            toast.show();

            // prevent user from returning to MainActivity
            finish();
        } else {
            // refresh data
            mAskFragment.loadQuestionList();

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

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
                    //System.out.println("0");
                    Intent prevIntent = getIntent();
                    String username = prevIntent.getStringExtra("username");
                    return HomeFragment.newInstance(username);
                case 1:
                    //System.out.println("1");
                    mAskFragment = AskFragment.newInstance();
                    return mAskFragment;
                case 2:
                    //System.out.println("2");
                    mAnswerFragment = AnswerFragment.newInstance();
                    return mAnswerFragment;
                case 3:
                    //System.out.println("3");
                    mMessageFragment = MessageFragment.newInstance();
                    return mMessageFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            // Returning 3 to disable MessageFragment
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
     * Getters and Setters
     */

    // None here.

    /**
     * Placeholder Items
     */

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

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
