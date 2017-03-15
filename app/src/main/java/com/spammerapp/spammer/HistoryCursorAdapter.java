package com.spammerapp.spammer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryCursorAdapter extends CursorAdapter {

    private SparseBooleanArray selectedItemIds = new SparseBooleanArray();

    public HistoryCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_row, null);
        ViewHolder holder = new ViewHolder();
        holder.id = (TextView) view.findViewById(R.id.histRowID);
        holder.receiver = (TextView) view.findViewById(R.id.histReceiver);
        holder.requestCount = (TextView) view.findViewById(R.id.histCountRequested);
        holder.requestTime = (TextView) view.findViewById(R.id.histRequestTime);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(final View convertView, final Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        int histRowID = cursor.getInt(cursor.getColumnIndex(MySQLHelper.HIST_COL_HIST_ID));
        String histReceiver = cursor.getString(cursor.getColumnIndex(MySQLHelper.HIST_COL_RECEIVER));
        int histRequestCount = cursor.getInt(cursor.getColumnIndex(MySQLHelper.HIST_COL_REQUEST_COUNT));
        long histRequestTime = cursor.getLong(cursor.getColumnIndex(MySQLHelper.HIST_COL_REQUEST_TIME));
        Locale locale = Locale.getDefault();
        DateFormat shortDatetime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        String strHistRequestTime;
        if(prefs.getBoolean(Constants.PREF_24_HOUR, false)){
            DateFormat shortDate = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", locale);
            strHistRequestTime = shortDate.format(histRequestTime) + " " + time24.format(histRequestTime);
            if(isSameDay(histRequestTime)){
                strHistRequestTime = time24.format(histRequestTime);
            }
        } else {
            strHistRequestTime = shortDatetime.format(histRequestTime);
            if(isSameDay(histRequestTime)){
                strHistRequestTime = shortTime.format(histRequestTime);
            }
        }
        holder.id.setText(String.valueOf(histRowID));
        holder.receiver.setText(histReceiver);
        holder.requestCount.setText(numberFormat.format(histRequestCount));
        holder.requestTime.setText(strHistRequestTime);
    }

    private boolean isSameDay(Long histRequestTime){
        Calendar today = Calendar.getInstance();
        today.getTimeInMillis();
        Calendar requestTime = Calendar.getInstance();
        requestTime.setTimeInMillis(histRequestTime);
        return today.get(Calendar.YEAR) == requestTime.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == requestTime.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    public void selectItem(int id, boolean value){
        if (value){
            selectedItemIds.put(id, true);
        } else {
            selectedItemIds.delete(id);
        }
    }

    public void removeSelection(){
        selectedItemIds = new SparseBooleanArray();
    }

    public int getSelectedCount(){
        return selectedItemIds.size();
    }

    public SparseBooleanArray getSelectedIds(){
        return selectedItemIds;
    }

    static class ViewHolder{
        TextView id;
        TextView receiver;
        TextView requestCount;
        TextView requestTime;
    }
}
