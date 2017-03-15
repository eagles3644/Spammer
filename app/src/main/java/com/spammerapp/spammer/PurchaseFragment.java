package com.spammerapp.spammer;

/**
 * Created by leona on 9/26/2015.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Currency;

public class PurchaseFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private IInAppBillingService mService;
    private String mAdFreePrice;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    public static PurchaseFragment newInstance(int sectionNumber) {
        PurchaseFragment fragment = new PurchaseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PurchaseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //get root view
        final View rootView = inflater.inflate(R.layout.fragment_purchases, container, false);

        //Get ad views
        AdView mAdView = (AdView) rootView.findViewById(R.id.purchaseAdView);
        AdView mAdView2 = (AdView) rootView.findViewById(R.id.purchaseAdView2);

        //Get Shared Prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Hide ad views if purchased ad free; otherwise request ads.
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

        //Connect to Play Billing
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //Setup for Play Billing Query
        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add("adFree");
        final Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(Constants.PURCHASE_AD_FREE, skuList);
        final Bundle[] skuDetails = new Bundle[1];

        //Play Billing Query
        new MyAsyncTask().execute(new Async() {
            @Override
            public void executeTask() {
                try {
                    skuDetails[0] = mService.getSkuDetails(3,
                            rootView.getContext().getPackageName(), "inapp", querySkus);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void postExecuteTask() {
                int response = skuDetails[0].getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails[0].getStringArrayList("DETAILS_LIST");
                    if (responseList != null) {
                        for (String thisResponse : responseList) {
                            JSONObject object = null;
                            try {
                                object = new JSONObject(thisResponse);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String sku = null;
                            if (object != null) {
                                try {
                                    sku = object.getString("productId");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            String price = null;
                            try {
                                price = object.getString("price");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (sku != null) {
                                if (sku.equals("adFree")) mAdFreePrice = price;
                            }
                        }
                    }
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            getActivity().unbindService(mServiceConn);
        }
    }
}