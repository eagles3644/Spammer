package com.spammerapp.spammer;

/**
 * Created by leona on 9/26/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HistoryFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static HistoryCursorAdapter histCursorAdapter;
    private static Cursor histCursor;
    private static ListView histListView;
    private static RelativeLayout histExistView;
    private static RelativeLayout noHistExistView;
    private Context myAppContext;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private MySQLHelper db;

    public static HistoryFragment newInstance(int sectionNumber) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        //Get ad views
        AdView mAdView = (AdView) rootView.findViewById(R.id.historyAdView);
        AdView mAdView2 = (AdView) rootView.findViewById(R.id.histAdView2);

        //Set vars
        myAppContext = getContext().getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(myAppContext);
        editor = prefs.edit();
        db = new MySQLHelper(myAppContext);

        //Hide ad views if purchased ad free; otherwise request ads
        if (prefs.getBoolean(Constants.PREF_AD_FREE, false)){
            mAdView.setVisibility(View.GONE);
            mAdView2.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("3314B9F4E1E76116C88ED6C64B835176")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView2.loadAd(adRequest);
        }

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i("PrefChangeListener:", "Changed=" + key);
                if(key.equals(Constants.PREF_24_HOUR) && db.getHistRowCount() > 0){
                    refreshHistCursor(getContext().getApplicationContext());
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        histExistView = (RelativeLayout) rootView.findViewById(R.id.histTableView);
        noHistExistView = (RelativeLayout) rootView.findViewById(R.id.noHistView);
        if (db.getHistRowCount() == 0) {
            histExistView.setVisibility(View.GONE);
            noHistExistView.setVisibility(View.VISIBLE);
        } else {
            histExistView.setVisibility(View.VISIBLE);
            noHistExistView.setVisibility(View.GONE);
            histListView = (ListView) rootView.findViewById(R.id.histList);
            new MyAsyncTask().execute(new Async() {
                @Override
                public void executeTask() {
                    histCursor = db.histCursor();
                    histCursorAdapter = new HistoryCursorAdapter(myAppContext, histCursor, 0);
                }

                @Override
                public void postExecuteTask() {
                    histListView.setAdapter(histCursorAdapter);
                }
            });
            histListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            histListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    int checkedCount = histListView.getCheckedItemCount();
                    mode.setTitle(checkedCount + " Selected");
                    TextView histID = (TextView) histListView.getChildAt(position).findViewById(R.id.histRowID);
                    int intHistID = Integer.parseInt(histID.getText().toString());
                    Log.i("HistID = ", "" + intHistID + " " + checked);
                    histCursorAdapter.selectItem(intHistID, checked);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.history_context_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            new MyAsyncTask().execute(new Async() {
                                @Override
                                public void executeTask() {
                                    SparseBooleanArray selectedIds = histCursorAdapter.getSelectedIds();
                                    for (int i = (selectedIds.size() - 1); i >= 0; i--) {
                                        if (selectedIds.valueAt(i)) {
                                            MySQLHelper db = new MySQLHelper(myAppContext);
                                            db.deleteHistByID(selectedIds.keyAt(i));
                                        }
                                    }
                                }

                                @Override
                                public void postExecuteTask() {
                                    mode.finish();
                                    refreshHistCursor(myAppContext);
                                }
                            });
                            return true;
                        case R.id.action_view_dtl:
                            SparseBooleanArray selectedIds = histCursorAdapter.getSelectedIds();
                            if (selectedIds.size() == 1) {
                                editor = prefs.edit();
                                editor.putInt(Constants.PREF_HIST_ID, selectedIds.keyAt(0));
                                editor.apply();
                                Intent intent = new Intent(getContext().getApplicationContext(), HistoryDetailActivity.class);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            } else {
                                Toast.makeText(getContext().getApplicationContext(),
                                        "To view details, select only one item at a time.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    histCursorAdapter.removeSelection();
                }
            });
            histListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    refreshHistCursor(myAppContext);
                }
            });
        }
        return rootView;
    }

    public static void refreshHistCursor(final Context context){
        final Cursor[] newCursor = new Cursor[1];
        new MyAsyncTask().execute(new Async() {
            @Override
            public void executeTask() {
                MySQLHelper db = new MySQLHelper(context);
                newCursor[0] = db.histCursor();
            }

            @Override
            public void postExecuteTask() {
                histCursorAdapter.swapCursor(newCursor[0]);
                MySQLHelper db = new MySQLHelper(context);
                if (db.getHistRowCount() == 0) {
                    histExistView.setVisibility(View.GONE);
                    noHistExistView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}