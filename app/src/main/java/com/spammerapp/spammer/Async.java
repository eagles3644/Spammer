package com.spammerapp.spammer;

import android.os.AsyncTask;

/**
 * Created by leona on 9/19/2015.
 */
public interface Async {
    void executeTask();
    void postExecuteTask();
}

class MyAsyncTask extends AsyncTask<Async, Void, Async> {
    @Override
    protected Async doInBackground(Async... runnables){
        runnables[0].executeTask();
        return runnables[0];
    }

    @Override
    protected void onPostExecute(Async runnable){
        runnable.postExecuteTask();
    }
}

