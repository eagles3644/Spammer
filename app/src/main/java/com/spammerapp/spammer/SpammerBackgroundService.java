package com.spammerapp.spammer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by leona on 10/25/2015.
 */
public class SpammerBackgroundService extends Service {

    private String msgTo;
    private String msgSubject;
    private String msgBody;
    private int msgCount;
    private int msgHistRowId;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            MySQLHelper db = new MySQLHelper(getApplicationContext());
            Cursor cursor = db.getHistCursorByID(msgHistRowId);
            int alreadySent = cursor.getInt(cursor.getColumnIndex(MySQLHelper.HIST_COL_SENT_COUNT));
            NotificationManager notificationManager = new NotificationManager(getApplicationContext());
            if(prefs.getBoolean(Constants.PREF_SEND_PROGRESS_NOTIF, true)) {
                notificationManager.notifySendProgress(msgTo, alreadySent, msgCount);
            }
            GMailSender gMailSender = new GMailSender();
            for(int i=alreadySent; i<msgCount; i++){
                gMailSender.sendMail(msgSubject, msgBody, prefs.getString(Constants.PREF_ACCOUNT_NAME, ""), prefs.getString(Constants.PREF_TOKEN, ""), msgTo);
                db.updateHistSentCount(msgHistRowId, i);
                if(prefs.getBoolean(Constants.PREF_SEND_PROGRESS_NOTIF, true)) {
                    notificationManager.notifySendProgress(msgTo, i, msgCount);
                }
                editor.putInt(Constants.PREF_SENT_COUNT, prefs.getInt(Constants.PREF_SENT_COUNT, 0) + 1);
                editor.apply();
            }
            db.updateHistSentCount(msgHistRowId, msgCount);
            db.updateHistFinalSendTime(msgHistRowId, System.currentTimeMillis());
            notificationManager.removeSendProgressNotif();
            if(prefs.getBoolean(Constants.PREF_SEND_COMPLETE_NOTIF, true)) {
                notificationManager.notifySendComplete(msgTo, msgCount, msgCount);
            }
            editor.putInt(Constants.PREF_SENT_COUNT, prefs.getInt(Constants.PREF_SENT_COUNT, 0) + 1);
            editor.apply();
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate(){
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show();
        msgTo = intent.getStringExtra("msgTo");
        msgSubject = intent.getStringExtra("msgSubject");
        msgBody = intent.getStringExtra("msgBody");
        msgCount = intent.getIntExtra("msgCount", 0);
        msgHistRowId = intent.getIntExtra("msgHistRowId", 0);
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Send Complete!", Toast.LENGTH_SHORT).show();
    }
}
