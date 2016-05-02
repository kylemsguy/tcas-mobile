package com.kylemsguy.tcasmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.kylemsguy.tcasmobile.tasks.GetLoggedInTask;
import com.kylemsguy.tcasmobile.tasks.LogoutTask;

public class NewMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SessionManager sm;
    private ProfileManager pm;
    private AsyncTask mLogoutTask;
    private AsyncTask mProfileImageTask;

    private TextView userView;
    private TextView emailView;
    private ImageView profileImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set first item (home) as checked
        navigationView.getMenu().getItem(0).setChecked(true);

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
        Fragment fragment = new HomeFragment();

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
        Class fragmentClass = null;

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (id == R.id.nav_home) {
                // Handle the home action
                fragmentClass = HomeFragment.class;
            } else if (id == R.id.nav_ask) {
                fragmentClass = AskFragment.class;
            } else if (id == R.id.nav_answer) {
                fragmentClass = AnswerFragment.class;
            } else if (id == R.id.nav_messages) {
                fragmentClass = MessageFragment.class;
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

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void jumpToSection(View view) {

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
