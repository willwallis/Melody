package com.knewto.www.melody;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class TopTenActivity extends ActionBarActivity implements TopTenActivityFragment.OnSongSelected {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_ten_container, new TopTenActivityFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("artistName")) {
            String artistName = intent.getStringExtra("artistName");
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setSubtitle(artistName);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_ten, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void playSelectedSong (ArrayList<TopTrack> arrayOfTracks, int position) {
        // If called from top ten activity we can assume phone version
        Intent playerIntent = new Intent(this, EmbeddedPlayerActivity.class);
        playerIntent.putExtra("posValue", position);
        playerIntent.putParcelableArrayListExtra("trackData", arrayOfTracks);
        this.startActivity(playerIntent);
    }



    @Override @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Intent getParentActivityIntent() {
// add the clear top flag - which checks if the parent (main)
// activity is already running and avoids recreating it
        return super.getParentActivityIntent()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
