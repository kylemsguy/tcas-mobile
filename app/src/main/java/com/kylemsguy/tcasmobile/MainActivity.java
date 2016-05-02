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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecentQuestionAdapter.OnJumpToAnswerQuestionListener {

    private SessionManager sm;
    private ProfileManager pm;
    private AsyncTask mLogoutTask;
    private AsyncTask mProfileImageTask;

    private TextView userView;
    private TextView emailView;
    private ImageView profileImgView;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // TODO: Cache the fragments
    private Fragment homeFragment;
    private Fragment askFragment;
    private Fragment answerFragment;
    private Fragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mProfileImageTask = new UpdateProfileImageTask().execute(pm);

        // Instantiate the main screen
        Fragment fragment = HomeFragment.newInstance(username);

        // Set up MainFragment
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content, fragment).commit();
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

    @SuppressWarnings("StatementWithEmptyBody")
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
                homeFragment = new HomeFragment();
                fragment = homeFragment;
            } else if (id == R.id.nav_ask) {
                toolbar.setTitle(R.string.title_activity_ask);
                // TODO: Fix this broken code and remove workaround
                /*if(askFragment == null){
                    askFragment = new AskFragment();
                }
                fragment = askFragment;*/
                // TODO Workaround below
                fragment = new AskFragment();
            } else if (id == R.id.nav_answer) {
                toolbar.setTitle(R.string.answer);
                if (answerFragment == null) {
                    answerFragment = AnswerFragment.newInstance();
                }
                fragment = answerFragment;
            } else if (id == R.id.nav_messages) {
                toolbar.setTitle(R.string.title_section4);
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                }
                fragment = messageFragment;
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
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().replace(R.id.content, fragment).commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void jumpToSection(View view) {

    }

    @Override
    public void jumpToAnswerQuestion(int id) {
        toolbar.setTitle(R.string.answer);
        // Set the Answer section as checked
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(true);

        // Replace current fragment with an AnswerFragment
        answerFragment = AnswerFragment.newInstance(id);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content, answerFragment).commit();
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
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
            profileImgView.setImageBitmap(scaledBitmap);
        }
    }
}
