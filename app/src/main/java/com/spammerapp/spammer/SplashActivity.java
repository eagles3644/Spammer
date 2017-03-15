package com.spammerapp.spammer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private Toast checkingToast = null;
    private boolean backPressedToExitOnce = false;
    private Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide title since using dialog theme
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Get action bar
        ActionBar actionBar = getSupportActionBar();

        //Show actionbar icon
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        //Show content
        setContentView(R.layout.activity_splash);

        //Disable dialog like dismiss
        //setFinishOnTouchOutside(false);

        //Get objects from xml
        final TextView splashText = (TextView) findViewById(R.id.SplashText);
        final ProgressBar splashProgress = (ProgressBar) findViewById(R.id.SplashProgress);

        //Local Vars
        String textToDisplay = "Please wait...";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int dbVersionPref = prefs.getInt(Constants.PREF_DB_VERSION, 0);
        Boolean alreadyInstalled = prefs.getBoolean(Constants.PREF_ALREADY_INSTALLED, false);
        final int dbVersionSQL = MySQLHelper.DATABASE_VERSION;
        final SharedPreferences.Editor editor = prefs.edit();
        final MySQLHelper[] mySQLHelper = new MySQLHelper[1];

        //Database upgrade tasks
        if(!alreadyInstalled || dbVersionSQL > dbVersionPref){

            //Check if SQL database was ever installed
            if(!alreadyInstalled) {
                textToDisplay = "Installing, please wait...";
            } else {
                textToDisplay = "Upgrading, please wait...";
            }

            //Set progress text
            splashText.setText(textToDisplay);

            //Perform Async Database Work
            new MyAsyncTask().execute(new Async() {
                @Override
                //Background task
                public void executeTask() {
                    //Open SQL database to trigger install or upgrade logic
                    mySQLHelper[0] = new MySQLHelper(getApplicationContext());
                }

                @Override
                //Post Background Task
                public void postExecuteTask() {
                    //Close SQL database
                    mySQLHelper[0].close();

                    //Update prefs
                    editor.putBoolean(Constants.PREF_ALREADY_INSTALLED, true);
                    editor.putInt(Constants.PREF_DB_VERSION, dbVersionSQL);
                    editor.apply();

                    //Execute checkConnectivity local void
                    checkConnectivity(splashText, splashProgress);
                }
            });
        } else {
            checkConnectivity(splashText, splashProgress);
        }
    }

    private void checkConnectivity(TextView splashText, ProgressBar splashProgress) {

        //Create connectivity checker object
        ConnectivityChecker connectivityChecker = new ConnectivityChecker(getApplicationContext());

        //Show progress bar if hidden
        if(splashProgress.getVisibility() == View.GONE){
            splashProgress.setVisibility(View.VISIBLE);
        }

        //Update progress text
        splashText.setText("Checking for internet connection...");

        //Check for internet connection
        if (connectivityChecker.isInternetConnected()) {
            //Create main activity intent
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

            //Start main activity
            startActivityForResult(mainActivityIntent, 2);

            //Finish splash activity
            finish();

            //Check if Toast not null
            if(checkingToast != null){
                //Cancel checking toast
                checkingToast.cancel();

                //Show Connection Toast
                Toast.makeText(this, "Internet Connectivity Found", Toast.LENGTH_SHORT).show();
            }
        } else {

            //Hide progress bar
            splashProgress.setVisibility(View.GONE);

            //Update progress text
            splashText.setText("No Internet Connection Available");
            splashText.setTypeface(null, Typeface.BOLD);
            splashText.setTextSize(20);

            //Get textView from xml
            TextView textViewNoInternet = (TextView) findViewById(R.id.SplashTextNoInternet);

            //set text
            textViewNoInternet.setText("An internet connection is required to use this application. "
                    + "Please try again when you have better cell service or are connected to WiFi.");

            //show hidden textView
            textViewNoInternet.setVisibility(View.VISIBLE);

            //Check if Toast not null
            if(checkingToast != null){
                //Cancel checking toast
                checkingToast.cancel();

                //Re-purpose Toast
                checkingToast = Toast.makeText(this, "No Internet Connectivity Found", Toast.LENGTH_SHORT);

                //Show new toast text
                checkingToast.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.splash_refresh_connectivity) {
            //Create Toasts
            checkingToast = Toast.makeText(this, "Checking for internet connection...", Toast.LENGTH_SHORT);

            //Show checking toast
            checkingToast.show();

            //Get objects from xml
            TextView splashText = (TextView) findViewById(R.id.SplashText);
            ProgressBar splashProgress = (ProgressBar) findViewById(R.id.SplashProgress);

            //Re-check connectivity
            checkConnectivity(splashText, splashProgress);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Log.d("SplashActivity:", "onBackPressed-Back button was pressed.");
        if (backPressedToExitOnce) {
            super.onBackPressed();
        } else {
            this.backPressedToExitOnce = true;
            showToast("Press again to quit.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);
        }
    }

    private void showToast(String message) {
        if(checkingToast != null){
            checkingToast.cancel();
        }

        if (toast == null) {
            // Create toast if found null, it would be the case of first call only
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else if (toast.getView() == null) {
            // Toast not showing, so create new one
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else {
            // Updating toast message is showing
            toast.setText(message);
        }

        // Showing toast finally
        this.toast.show();
    }

    private void killToast() {
        if (this.toast != null) {
            this.toast.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==2){
            finish();
        }
    }
}
