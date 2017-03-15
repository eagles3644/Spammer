package com.spammerapp.spammer;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComposeActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String msgTo;
    private String msgSubject;
    private String msgBody;
    private String msgFrom;
    private int msgCount = 0;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set content view
        setContentView(R.layout.activity_compose);

        //Get ad views
        AdView mAdView = (AdView) findViewById(R.id.compAdView);
        AdView mAdView2 = (AdView) findViewById(R.id.compAdView2);

        //Get Shared Prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        editor = prefs.edit();

        //Hide ad views if purchased ad free; otherwise request ads.
        if(prefs.getBoolean(Constants.PREF_AD_FREE, false)){
            mAdView.setVisibility(View.GONE);
            mAdView2.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("3314B9F4E1E76116C88ED6C64B835176")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView2.loadAd(adRequest);
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-6909068111618447/3755893418");
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    finish();
                }
            });
            requestNewInterstitial();
        }

        NumberPicker compCount = (NumberPicker) findViewById(R.id.compCount);
        compCount.setMinValue(0);
        compCount.setMaxValue(1000);
        compCount.setWrapSelectorWheel(true);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("3314B9F4E1E76116C88ED6C64B835176")
                .build();
        mInterstitialAd.loadAd(adRequest);
        Log.e("requestNewInterstitial", "Requested....");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
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
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            return true;
        }

        if(id == R.id.action_send) {
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED){
                assembleEmailData();
            } else {
                ActivityCompat.requestPermissions(this
                        , new String[]{Manifest.permission.GET_ACCOUNTS}
                        , Constants.PERMIT_REQ_GET_ACCOUNTS);
            }
        }

        if (id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void assembleEmailData() {
        //create locals
        boolean valMsgTo = false;
        boolean valMsgSubject = false;
        boolean valMsgBody = false;
        boolean valMsgCount = false;
        //Get user inputs
        EditText compTo = (EditText) findViewById(R.id.compTo);
        EditText compSubject = (EditText) findViewById(R.id.compSubject);
        EditText compBody = (EditText) findViewById(R.id.compBody);
        NumberPicker compCount = (NumberPicker) findViewById(R.id.compCount);
        //Set vars equal to values
        msgTo = compTo.getText().toString();
        msgSubject = compSubject.getText().toString();
        msgBody = compBody.getText().toString();
        msgCount = compCount.getValue();
        //Validate to
        if(!msgTo.isEmpty() && isEmailValid(msgTo)){
            valMsgTo = true;
        } else {
            Toast.makeText(this.getApplicationContext()
                    , "The 'To' line is blank or contains an invalid email address."
                    , Toast.LENGTH_LONG).show();
        }
        //Validate subject if to is valid
        if(valMsgTo){
            if(!msgSubject.isEmpty()) {
                valMsgSubject = true;
            } else {
                Toast.makeText(this.getApplicationContext()
                        , "The 'Subject' line cannot be blank."
                        , Toast.LENGTH_LONG).show();
            }
        }
        //Validate body if to and subject are valid
        if(valMsgTo && valMsgSubject){
            if(!msgBody.isEmpty()) {
                valMsgBody = true;
            } else {
                Toast.makeText(this.getApplicationContext()
                        , "The 'Body' cannot be blank."
                        , Toast.LENGTH_LONG).show();
            }
        }
        //Validate count if to, subject, and body are valid
        if(valMsgTo && valMsgSubject && valMsgBody){
            if(msgCount > 0){
                valMsgCount = true;
            } else {
                Toast.makeText(this.getApplicationContext()
                        , "The 'Quantity' cannot be 0."
                        , Toast.LENGTH_LONG).show();
            }
        }
        //Proceed to choosing sending account if all are valid
        if(valMsgTo && valMsgSubject && valMsgBody && valMsgCount){
            if(prefs.getBoolean(Constants.PREF_CONFIRM_ACCOUNT, true)){
                pickUserAccount();
            } else {
                if(prefs.getString(Constants.PREF_ACCOUNT_NAME, "EMPTY").equals("EMPTY") || prefs.getString(Constants.PREF_TOKEN, "EMPTY").equals("EMPTY")){
                    pickUserAccount();
                } else {
                    msgFrom = prefs.getString(Constants.PREF_ACCOUNT_NAME, "EMPTY");
                    sendMailBackground();
                }
            }
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, Constants.GMAIL_ACCOUNT_CODE);
    }

    private void sendMailBackground() {
        requestToken();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == Constants.GMAIL_ACCOUNT_CODE) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                msgFrom = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                editor.putString(Constants.PREF_ACCOUNT_NAME, msgFrom);
                editor.apply();
                // With the account name acquired, go get the auth token
                requestToken();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(getApplicationContext(), "No account selected. Cannot send mail.", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == Constants.GMAIL_AUTHORIZATION_CODE){
            requestToken();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case Constants.PERMIT_REQ_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequestResult", "GET_ACCOUNT permission was permitted!");
                    assembleEmailData();
                } else {
                    String toastText = "GET_ACCOUNTS permission was denied. You are required to permit " +
                            "this access in order to send emails.";
                    Toast.makeText(this.getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private void requestToken() {
        invalidateToken();
        Account userAccount = null;
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
        String user = prefs.getString(Constants.PREF_ACCOUNT_NAME, "");
        for (int i=0; i <= accounts.length-1; i++) {
            if (accounts[i].name.equals(user)) {
                userAccount = accounts[i];
                break;
            }
        };
        AccountManager.get(getApplicationContext()).getAuthToken(userAccount, "oauth2:" + Constants.SCOPE_GMAIL_MAIL
                + " " + Constants.SCOPE_GMAIL_MODIFY
                + " " + Constants.SCOPE_GMAIL_COMPOSE
                , null
                , this
                , new OnTokenAcquired(), null);
    }

    private void invalidateToken() {
        String token = prefs.getString(Constants.PREF_TOKEN, "");
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        accountManager.invalidateAuthToken("com.google", token);
        Log.v("Compose", "invalidating token............");
        editor.putString(Constants.PREF_TOKEN, null);
        editor.apply();
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, Constants.GMAIL_AUTHORIZATION_CODE);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);
                    Log.v("Compose", "Getting new token............");
                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.PREF_TOKEN, token);
                    editor.apply();
                    final int[] histRowID = new int[1];
                    new MyAsyncTask().execute(new Async() {
                        @Override
                        public void executeTask() {
                            MySQLHelper db = new MySQLHelper(getApplicationContext());
                            db.insertNewHistoryRow(prefs.getString(Constants.PREF_ACCOUNT_NAME, ""), msgTo,
                                    msgCount, System.currentTimeMillis(), msgSubject, msgBody);
                            histRowID[0] = db.maxHistRowID();
                        }

                        @Override
                        public void postExecuteTask() {
                            Intent intent = new Intent(getApplicationContext(), SpammerBackgroundService.class);
                            intent.putExtra("msgTo", msgTo);
                            intent.putExtra("msgSubject", msgSubject);
                            intent.putExtra("msgBody", msgBody);
                            intent.putExtra("msgCount", msgCount);
                            intent.putExtra("msgHistRowId", histRowID[0]);
                            startService(intent);
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Sending spam...", Toast.LENGTH_LONG).show();
                    if(prefs.getBoolean(Constants.PREF_AD_FREE, false)){
                        finish();
                    } else {
                        if(mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            finish();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
