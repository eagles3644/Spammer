package com.spammerapp.spammer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private SharedPreferences.Editor editor;
    private MySQLHelper db;
    private Locale locale;
    private NumberFormat numberFormat;
    private static long milliReqTime;
    private static long milliSentTime;
    private static String strSender;
    private static String strReceiver;
    private static String strSubject;
    private static String strBody;
    private static int intReqCount;
    private static int intSentCount;
    private static String strCancelInd;
    private static String strDuration;
    private static String strStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        //Get ad views
        AdView mAdView = (AdView) findViewById(R.id.histDtlAdView);

        //Set vars
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();
        db = new MySQLHelper(getApplicationContext());
        locale = Locale.getDefault();
        numberFormat = NumberFormat.getNumberInstance(locale);

        //Hide ad views if purchased ad free; otherwise request ads
        if (prefs.getBoolean(Constants.PREF_AD_FREE, false)){
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("3314B9F4E1E76116C88ED6C64B835176")
                    .build();
            mAdView.loadAd(adRequest);
        }

        //Enable preference change listener
        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //change time format if preference changed
                if(key.equals(Constants.PREF_24_HOUR)){
                    changeTimeFormat();
                }
            }
        };

        //register preference change listener
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener);

        //get history row id
        int histRowID = prefs.getInt(Constants.PREF_HIST_ID, 0);

        //return history row
        Cursor cursor = db.getHistCursorByID(histRowID);

        //get values from history row
        milliReqTime = cursor.getLong(cursor.getColumnIndex(MySQLHelper.HIST_COL_REQUEST_TIME));
        milliSentTime = cursor.getLong(cursor.getColumnIndex(MySQLHelper.HIST_COL_FINAL_SEND_TIME));
        strSender = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_SENDER));
        strReceiver = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_RECEIVER));
        strSubject = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_MSG_SUBJECT));
        strBody = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_MSG_BODY));
        intReqCount = cursor.getInt(cursor.getColumnIndex(MySQLHelper.HIST_COL_REQUEST_COUNT));
        intSentCount = cursor.getInt(cursor.getColumnIndex(MySQLHelper.HIST_COL_SENT_COUNT));
        strCancelInd = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_USER_CANCEL_IND));

        //close history row db connection
        cursor.close();

        //Set status of mail sending
        if(intReqCount == intSentCount) {
            strStatus = "Completed Successfully";
        } else if(strCancelInd.equals("Y")){
            strStatus = "Cancelled - User Request";
        } else {
            strStatus = "Cancelled - System Error";
        }

        //Get text views
        //Get text views
        TextView sender = (TextView) findViewById(R.id.dtlSender);
        TextView receiver = (TextView) findViewById(R.id.dtlReceiver);
        TextView subject = (TextView) findViewById(R.id.dtlSubject);
        TextView body = (TextView) findViewById(R.id.dtlBody);
        TextView reqCount = (TextView) findViewById(R.id.dtlReqCount);
        TextView sentCount = (TextView) findViewById(R.id.dtlSentCount);
        TextView status = (TextView) findViewById(R.id.dtlStatus);

        //Set text view values equal to history row data
        sender.setText(strSender);
        receiver.setText(strReceiver);
        subject.setText(strSubject);
        body.setText(strBody);
        reqCount.setText(numberFormat.format(intReqCount));
        sentCount.setText(numberFormat.format(intSentCount));
        status.setText(strStatus);

        //Format time to proper format
        changeTimeFormat();

        //Calculate the duration it took to send all mails
        calculateDuration();
    }

    private void calculateDuration() {

        //get duration in milis
        long duration = milliSentTime - milliReqTime;

        //set locals
        long oneSecond = 1000;
        long oneMinute = (oneSecond * 60);
        long oneHour = (oneMinute * 60);

        strDuration = "";

        //Calculate number of hours in duration if took over an hour
        if(duration >= oneHour){
            //Get number of hours
            long hours = (duration / oneHour);
            //Check if more than one hour
            if(hours > 1){
                strDuration = ", " + hours + " hours";
            } else {
                strDuration = ", " + hours + " hour";
            }
            //decrease duration for next calculation
            duration = (duration - (oneHour * hours));
        }

        //Calculate number of minutes in duration if took over a minute
        if(duration >= oneMinute){
            //Get number of minutes
            long minutes = (duration / oneMinute);
            //Check if more than one minute
            if(minutes > 1){
                strDuration = strDuration + ", " + minutes + " minutes";
            } else {
                strDuration = strDuration + ", " + minutes + " minute";
            }
            //decrease duration for next calculation
            duration = (duration - (oneMinute * minutes));
        }

        //Calculate number of seconds in duration if took over a second
        if(duration >= oneSecond){
            //Calculate number of seconds
            long seconds = (duration / oneSecond);
            //Check if more than one second
            if(seconds > 1){
                strDuration = strDuration + ", " + seconds + " seconds";
            } else {
                strDuration = strDuration + ", " + seconds + " second";
            }
            //decrease duration
            duration = (duration - (oneSecond * seconds));
        }

        //Calculate number of milliseconds in duration
        if(duration > 0) {
            //Check if more than one millisecond
            if(duration == 1){
                strDuration = strDuration + ", " + duration + " millisecond";
            } else {
                strDuration = strDuration + ", " + duration + " milliseconds";
            }
        }

        //Get duration text view
        TextView tvDuration = (TextView) findViewById(R.id.dtlDuration);

        //Set value of duration text view; used substring to chop off remaining space and comma
        if(!strDuration.isEmpty()){
            tvDuration.setText(strDuration.substring(2, strDuration.length()));
        }
    }

    private void changeTimeFormat() {
        //Get text views that display time
        TextView requestTime = (TextView) findViewById(R.id.dtlReqTime);
        TextView sentTime = (TextView) findViewById(R.id.dtlSentTime);

        //Create local strings to format time
        String strReqTime;
        String strSentTime;

        //Set date formats based on local and day
        DateFormat shortDate = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", locale);
        DateFormat shortDatetime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

        //Format time based on shared preference
        if(prefs.getBoolean(Constants.PREF_24_HOUR, false)){
            //Choose proper format for request time based on day
            if(isSameDay(milliReqTime)){
                strReqTime = time24.format(milliReqTime);
            } else {
                strReqTime = shortDate.format(milliReqTime) + " " + time24.format(milliReqTime);
            }

            //Choose proper format for sent time based on day
            if(isSameDay(milliSentTime)){
                strSentTime = time24.format(milliSentTime);
            } else {
                strSentTime = shortDate.format(milliSentTime) + " " + time24.format(milliSentTime);
            }

        } else {
            //Choose proper format for request time based on day
            if(isSameDay(milliReqTime)){
                strReqTime = shortTime.format(milliReqTime);
            } else {
                strReqTime = shortDatetime.format(milliReqTime);
            }

            //Choose proper format for sent time based on day
            if(isSameDay(milliSentTime)){
                strSentTime = shortTime.format(milliSentTime);
            } else {
                strSentTime = shortDatetime.format(milliSentTime);
            }
        }

        //Set text for text views
        requestTime.setText(strReqTime);
        if(milliSentTime > 0) {
            sentTime.setText(strSentTime);
        } else {
            sentTime.setText("");
        }
    }

    private boolean isSameDay(Long milliTime){
        //Get calendar instances
        Calendar today = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        //Set calendar instances' millis
        today.getTimeInMillis();
        cal.setTimeInMillis(milliTime);

        //Compare to see if same day
        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            return true;
        }

        if (id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
