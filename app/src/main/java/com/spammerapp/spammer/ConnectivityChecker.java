package com.spammerapp.spammer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by leona on 9/19/2015.
 */
public class ConnectivityChecker {

    Context myContext;

    public ConnectivityChecker(Context context) {
        myContext = context;
    }

    public boolean isInternetConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
