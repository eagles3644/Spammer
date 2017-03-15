package com.spammerapp.spammer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by leona on 9/19/2015.
 */
public class MySQLHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SpammerApp.db";

    //Table vars
    public static final String TBL_HISTORY = "history";

    //Column Vars
    public static final String HIST_COL_HIST_ID = "_id";
    public static final String HIST_COL_SENDER = "sender";
    public static final String HIST_COL_RECEIVER = "receiver";
    public static final String HIST_COL_REQUEST_COUNT = "request_count";
    public static final String HIST_COL_SENT_COUNT = "sent_count";
    public static final String HIST_COL_REQUEST_TIME = "request_time";
    public static final String HIST_COL_FINAL_SEND_TIME = "final_send_time";
    public static final String HIST_COL_MSG_SUBJECT = "subject";
    public static final String HIST_COL_MSG_BODY = "body";
    public static final String HIST_COL_USER_CANCEL_IND = "cancel_ind";

    //Create SQL Statements
    private static final String SQL_CREATE_HISTORY_TBL = "CREATE TABLE " + TBL_HISTORY + " (" +
            HIST_COL_HIST_ID + " INTEGER PRIMARY KEY, " + HIST_COL_SENDER + " TEXT, " +
            HIST_COL_RECEIVER + " TEXT, " + HIST_COL_REQUEST_COUNT + " INTEGER, " +
            HIST_COL_SENT_COUNT + " INTEGER, " + HIST_COL_REQUEST_TIME + " INTEGER, " +
            HIST_COL_FINAL_SEND_TIME + " INTEGER, " + HIST_COL_MSG_SUBJECT + " TEXT, " +
            HIST_COL_MSG_BODY + " TEXT, " + HIST_COL_USER_CANCEL_IND + " TEXT)";

    public MySQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("Spammer:", "com.spammerapp.spammer.MySQLHelper-onCreate Version = " + DATABASE_VERSION);
        db.execSQL(SQL_CREATE_HISTORY_TBL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Log trigger of onUpgrade
        Log.e("Spammer:", "com.spammerapp.spammer.MySQLHelper-onUpgrade oldVersion = " + oldVersion + " newVersion = " + newVersion);

        //Declare local vars
        Cursor historyCursor;
        int histSenderIndex;
        int histReceiverIndex;
        int histReqCountIndex;
        int histSentCountIndex;
        int histReqTimeIndex;
        int histFinalSendTimeIndex;
        int histMsgSubjectIndex;
        int histMsgBodyIndex;
        int histCancelIndIndex;
        String alterSQL;
        String updateSQL;

        //History Table Check
        try {
            historyCursor = db.rawQuery("SELECT * FROM " + TBL_HISTORY, null, null);
        }
        catch(Exception exception){
            db.execSQL(SQL_CREATE_HISTORY_TBL);
            historyCursor = db.rawQuery("SELECT * FROM " + TBL_HISTORY, null, null);
        }

        //Get History Table Column Indexes
        histSenderIndex = historyCursor.getColumnIndex(HIST_COL_SENDER);
        histReceiverIndex = historyCursor.getColumnIndex(HIST_COL_RECEIVER);
        histReqCountIndex = historyCursor.getColumnIndex(HIST_COL_REQUEST_COUNT);
        histSentCountIndex = historyCursor.getColumnIndex(HIST_COL_SENT_COUNT);
        histReqTimeIndex = historyCursor.getColumnIndex(HIST_COL_REQUEST_TIME);
        histFinalSendTimeIndex = historyCursor.getColumnIndex(HIST_COL_FINAL_SEND_TIME);
        histMsgSubjectIndex = historyCursor.getColumnIndex(HIST_COL_MSG_SUBJECT);
        histMsgBodyIndex = historyCursor.getColumnIndex(HIST_COL_MSG_BODY);
        histCancelIndIndex = historyCursor.getColumnIndex(HIST_COL_USER_CANCEL_IND);

        //History Table Upgrades
        if(histSenderIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_SENDER + " TEXT";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_SENDER + "='Unknown'";
            db.execSQL(updateSQL);
        }
        if(histReceiverIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_RECEIVER + " TEXT";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_RECEIVER + "='Unknown'";
            db.execSQL(updateSQL);
        }
        if(histReqCountIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_REQUEST_COUNT + " INTEGER";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_REQUEST_COUNT + "=0";
            db.execSQL(updateSQL);
        }
        if(histSentCountIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_SENT_COUNT + " INTEGER";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_SENT_COUNT + "=0";
            db.execSQL(updateSQL);
        }
        if(histReqTimeIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_REQUEST_TIME + " INTEGER";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_REQUEST_TIME + "=0";
            db.execSQL(updateSQL);
        }
        if(histFinalSendTimeIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_FINAL_SEND_TIME + " INTEGER";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_FINAL_SEND_TIME + "=0";
            db.execSQL(updateSQL);
        }
        if(histMsgSubjectIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_MSG_SUBJECT + " TEXT";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_MSG_SUBJECT + "='Unknown'";
            db.execSQL(updateSQL);
        }
        if(histMsgBodyIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_MSG_BODY + " TEXT";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_MSG_BODY + "='Unknown'";
            db.execSQL(updateSQL);
        }
        if(histCancelIndIndex == -1){
            alterSQL = "ALTER TABLE " + TBL_HISTORY + " ADD COLUMN " + HIST_COL_USER_CANCEL_IND + " TEXT";
            db.execSQL(alterSQL);
            updateSQL = "UPDATE " + TBL_HISTORY + " SET " + HIST_COL_USER_CANCEL_IND + "='N'";
            db.execSQL(updateSQL);
        }

        //Close Cursors
        historyCursor.close();
    }

    public int getHistRowCount(){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                HIST_COL_HIST_ID
        };
        Cursor cursor = db.query(TBL_HISTORY, projection, null, null, null, null, null);
        cursor.moveToLast();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteHistRows(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TBL_HISTORY, null, null);
    }

    public void deleteHistByID(int ID){
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TBL_HISTORY + " WHERE " + HIST_COL_HIST_ID + "=" + ID;
        db.execSQL(query);
    }

    public int sumSentCount(){
        SQLiteDatabase db = getReadableDatabase();
        int sumSentCount;
        String query = "SELECT sum(" + HIST_COL_SENT_COUNT + ") FROM " + TBL_HISTORY;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            sumSentCount = cursor.getInt(0);
        } else {
            sumSentCount = 0;
        }
        cursor.close();
        return sumSentCount;
    }

    public Cursor histCursor(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                HIST_COL_HIST_ID,
                HIST_COL_SENDER,
                HIST_COL_RECEIVER,
                HIST_COL_REQUEST_COUNT,
                HIST_COL_SENT_COUNT,
                HIST_COL_REQUEST_TIME,
                HIST_COL_FINAL_SEND_TIME,
                HIST_COL_MSG_SUBJECT,
                HIST_COL_MSG_BODY,
                HIST_COL_USER_CANCEL_IND
        };
        String order = HIST_COL_HIST_ID + " DESC";
        Cursor cursor = db.query(TBL_HISTORY, projection, null, null, null, null, order);
        cursor.moveToLast();
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getHistCursorByID(int histRowID){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                HIST_COL_HIST_ID,
                HIST_COL_SENDER,
                HIST_COL_RECEIVER,
                HIST_COL_REQUEST_COUNT,
                HIST_COL_SENT_COUNT,
                HIST_COL_REQUEST_TIME,
                HIST_COL_FINAL_SEND_TIME,
                HIST_COL_MSG_SUBJECT,
                HIST_COL_MSG_BODY,
                HIST_COL_USER_CANCEL_IND
        };
        String order = HIST_COL_HIST_ID + " DESC";
        Cursor cursor = db.query(TBL_HISTORY, projection, HIST_COL_HIST_ID + "=" + histRowID, null, null, null, HIST_COL_HIST_ID);
        cursor.moveToLast();
        cursor.moveToFirst();
        return cursor;
    }

    public void insertNewHistoryRow(String sender, String receiver, int requestCount,
                                    long requestTime, String msgSubject, String msgBody){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HIST_COL_SENDER, sender);
        values.put(HIST_COL_RECEIVER, receiver);
        values.put(HIST_COL_REQUEST_COUNT, requestCount);
        values.put(HIST_COL_SENT_COUNT, 0);
        values.put(HIST_COL_REQUEST_TIME, requestTime);
        values.put(HIST_COL_FINAL_SEND_TIME, 0);
        values.put(HIST_COL_MSG_SUBJECT, msgSubject);
        values.put(HIST_COL_MSG_BODY, msgBody);
        values.put(HIST_COL_USER_CANCEL_IND, "N");
        db.insert(TBL_HISTORY, null, values);
    }

    public void updateHistSentCount(int histRowID, int countSent){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HIST_COL_SENT_COUNT, countSent);
        db.update(TBL_HISTORY, values, HIST_COL_HIST_ID + "=" + histRowID, null);
    }

    public void updateHistFinalSendTime(int histRowID, long finalSendTime){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HIST_COL_FINAL_SEND_TIME, finalSendTime);
        db.update(TBL_HISTORY, values, HIST_COL_HIST_ID + "=" + histRowID, null);
    }

    public void updateHistCancelInd(int histRowID, String cancelInd){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HIST_COL_USER_CANCEL_IND, cancelInd);
        db.update(TBL_HISTORY, values, HIST_COL_HIST_ID + "=" + histRowID, null);
    }

    public int maxHistRowID(){
        Cursor cursor = histCursor();
        cursor.moveToFirst();
        int rowID = cursor.getInt(cursor.getColumnIndex(HIST_COL_HIST_ID));
        cursor.close();
        return rowID;
    }
}
