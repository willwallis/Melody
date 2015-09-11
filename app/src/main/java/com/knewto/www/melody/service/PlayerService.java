package com.knewto.www.melody.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by willwallis on 9/10/15.
 */
public class PlayerService extends IntentService {

    public PlayerService() {
        super("PlayerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v("Intent Player Service", "Intent recieved");
    }


}
