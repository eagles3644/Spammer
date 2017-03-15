package com.spammerapp.spammer;

/**
 * Created by leona on 9/26/2015.
 */

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.w3c.dom.Text;

public class DashboardFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static DashboardFragment newInstance(int sectionNumber) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //Get ad views
        AdView mAdView = (AdView) rootView.findViewById(R.id.dashAdView);
        AdView mAdView2 = (AdView) rootView.findViewById(R.id.dashAdView2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Get sent count
        final TextView sentCount = (TextView) rootView.findViewById(R.id.dashCounter);

        //Get sent count from database
        final int[] sumSent = new int[1];
        new MyAsyncTask().execute(new Async() {
            @Override
            public void executeTask() {
                MySQLHelper db = new MySQLHelper(getContext());
                sumSent[0] = db.sumSentCount();

            }

            @Override
            public void postExecuteTask() {
                sentCount.setText(sumSent[0]);
            }
        });

        //Get compose button
        ImageButton composeButton = (ImageButton) rootView.findViewById(R.id.dashFAB);

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

        //Set compose button click action
        composeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), ComposeActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}