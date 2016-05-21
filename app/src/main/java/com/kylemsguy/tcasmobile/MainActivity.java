package com.kylemsguy.tcasmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.ProfileManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;
import com.kylemsguy.tcasmobile.tasks.LogoutTask;

import java.util.Stack;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        RecentQuestionAdapter.OnJumpToAnswerQuestionListener {

    private static final String[] SCREEN_FRAGMENT_IDS =
            {"HOME_FRAGMENT", "ASK_FRAGMENT", "ANSWER_FRAGMENT", "MESSAGE_FRAGMENT"};

    private static final int[] TOOLBAR_TITLE_IDS =
            {
                    R.string.title_activity_main,
                    R.string.title_activity_ask,
                    R.string.answer,
                    R.string.title_section4,
            };

    private static final String SAVED_FRAGMENT_KEY = "__CURRENT_FRAGMENT__";
    private static final String CURRENT_SCREEN_INDEX_KEY = "__CURRENT_SCREEN_ID_KEY__";

    private SessionManager sm;
    private ProfileManager pm;
    private AsyncTask mLogoutTask;
    private AsyncTask mProfileImageTask;

    private TextView userView;
    private TextView emailView;
    private ImageView profileImgView;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // TODO replace with an enum?
    private int currentScreenIndex = 0;

    private Fragment currentFragment;
    private Stack<Integer> screenHistoryStack = new Stack<>();
    private Stack<Fragment> fragmentHistoryStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up layout
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_main);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set first item (home) as checked
        navigationView.getMenu().getItem(0).setChecked(true);

        // Disable the 4th item (Messages) as it's not finished yet
        navigationView.getMenu().getItem(3).setVisible(false);
        //navigationView.getMenu().getItem(3).setEnabled(false);

        // Get header
        View headerLayout = navigationView.getHeaderView(0);

        // Get the Managers
        sm = ((TCaSApp) getApplicationContext()).getSessionManager();
        pm = ((TCaSApp) getApplicationContext()).getProfileManager();

        // Get references to various things in the header
        userView = (TextView) headerLayout.findViewById(R.id.headerUsername);
        emailView = (TextView) headerLayout.findViewById(R.id.headerEmail);
        profileImgView = (ImageView) headerLayout.findViewById(R.id.headerProfileImg);

        // Instantiate the various things in the header
        String username = PrefUtils.getFromPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, null);
        userView.setText(username);
        userView.setTypeface(userView.getTypeface(), Typeface.BOLD);
        emailView.setVisibility(View.GONE);

        // Set profile image
        // TODO cache this
        mProfileImageTask = new UpdateProfileImageTask().execute(pm);

        // Get FragmentManager
        FragmentManager fm = getSupportFragmentManager();

        // Add back stack change listener
        fm.addOnBackStackChangedListener(this);

        // Check if we already have a saved fragment state
        String screenId;
        if (savedInstanceState != null) {
            currentFragment = fm.getFragment(savedInstanceState, SAVED_FRAGMENT_KEY);
            currentScreenIndex = savedInstanceState.getInt(CURRENT_SCREEN_INDEX_KEY);
            screenId = SCREEN_FRAGMENT_IDS[currentScreenIndex];
            // set whichever item should be checked
            navigationView.getMenu().getItem(0).setChecked(false);
            navigationView.getMenu().getItem(currentScreenIndex).setChecked(true);

        } else {
            currentFragment = HomeFragment.newInstance(username);
            screenId = SCREEN_FRAGMENT_IDS[0];
            // Instantiate the current screen
            fm.beginTransaction().replace(R.id.content, currentFragment, screenId).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save reference to current fragment
        getSupportFragmentManager().putFragment(outState, SAVED_FRAGMENT_KEY, currentFragment);
        outState.putInt(CURRENT_SCREEN_INDEX_KEY, currentScreenIndex);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (id == R.id.nav_home) {
                // Handle the home action
                // TODO: make better way to refresh recent questions on home
                toolbar.setTitle(R.string.title_activity_main);
                fragment = new HomeFragment();
                currentScreenIndex = 0;
            } else if (id == R.id.nav_ask) {
                toolbar.setTitle(R.string.title_activity_ask);
                fragment = new AskFragment();
                currentScreenIndex = 1;
            } else if (id == R.id.nav_answer) {
                toolbar.setTitle(R.string.answer);
                fragment = AnswerFragment.newInstance();
                currentScreenIndex = 2;
            } else if (id == R.id.nav_messages) {
                toolbar.setTitle(R.string.title_section4);
                fragment = MessageFragment.newInstance();
                currentScreenIndex = 3;
            } else if (id == R.id.nav_logout) {
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
                return true;
            }

            if (fragment == null) {
                System.out.println("MainActivity: Invalid menu choice" + id);
            } else {
                // Store the selection id
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.content, fragment, SCREEN_FRAGMENT_IDS[currentScreenIndex])
                        .addToBackStack(null)
                        .commit();
                screenHistoryStack.push(currentScreenIndex);
                fragmentHistoryStack.push(fragment);
                currentScreenIndex = 2;
                currentFragment = fragment;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void jumpToSection(View view) {
        // TODO Legacy function just here to satisfy old layouts
    }

    @Override
    public void jumpToAnswerQuestion(int id) {
        toolbar.setTitle(R.string.answer);
        // Set the Answer section as checked
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(true);

        // Replace current fragment with an AnswerFragment
        AnswerFragment answerFragment = AnswerFragment.newInstance(id);
        screenHistoryStack.push(currentScreenIndex);
        fragmentHistoryStack.push(currentFragment);
        currentScreenIndex = 2;
        currentFragment = answerFragment;
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.content, answerFragment, SCREEN_FRAGMENT_IDS[2])
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        int fmBackStackLength = fm.getBackStackEntryCount();
        if (fmBackStackLength < screenHistoryStack.size()) {
            if (screenHistoryStack.empty() || fragmentHistoryStack.empty()) {
                System.out.println("MainActivity: Screen/Fragment history stack empty?!");
                return;
            }
            currentScreenIndex = screenHistoryStack.pop();
            currentFragment = fragmentHistoryStack.pop();
            navigationView.getMenu().getItem(currentScreenIndex).setChecked(true);
            toolbar.setTitle(TOOLBAR_TITLE_IDS[currentScreenIndex]);
        }
    }

    private class UpdateProfileImageTask extends AsyncTask<ProfileManager, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(ProfileManager... params) {
            ProfileManager pm = params[0];

            try {
                return pm.getProfileImage();
            } catch (Exception e) {
                System.err.println("ChangeProfileImageActivity: Error getting Profile Image from server");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
                profileImgView.setImageBitmap(scaledBitmap);
            }
        }
    }
}
