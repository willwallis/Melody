package com.knewto.www.melody;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PlayerActivity extends AppCompatActivity {
    MusicService musicService;
    boolean musicBound = false;
    boolean isBound;
    final String TAG = "Player Activty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        setContentView(R.layout.activity_player);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to MusicService
        doBindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the Music Service
        doUnbindService();
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        if(isFinishing()){
            Intent intentStopService = new Intent(this, MusicService.class );
            stopService(intentStopService);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // BINDING METHODS
    private void doBindToService() {
        if(!isBound){
            Intent bindIntent = new Intent(this, MusicService.class);
            isBound = bindService(bindIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void doUnbindService() {
        unbindService(musicConnection);
        isBound = false;
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {

            musicBound = false;
        }
    };


    // Player methods

    public void loadSong(Uri trackUrl){
        if (musicBound) {
            Log.v(TAG, "load song");
            musicService.loadSong(trackUrl);
        }
    }

    public void playSong(){
        if (musicBound) {
            Log.v(TAG, "play song");
            musicService.playSong();
        }
    }

    public boolean pauseSong(boolean onOff){
        if (musicBound){
            Log.v(TAG, "pause song: " + onOff);
            return musicService.pauseSong(onOff);
        }
        else
            return false;
    }

    public void scrubSong(int position){
        if (musicBound) {
            Log.v(TAG, "scrub song");
            musicService.scrubSong(position);
        }
    }

}
