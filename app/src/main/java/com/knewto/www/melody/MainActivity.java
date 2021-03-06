package com.knewto.www.melody;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.knewto.www.melody.service.SongService;

public class MainActivity extends ActionBarActivity implements MainActivityFragment.OnArtistSelected, TopTenActivityFragment.OnSongSelected {

    private static final String TOPTENFRAGMENT_TAG = "TTTAG";
    private static final String PLAYERFRAGMENT_TAG = "PDTAG";
    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.top_ten_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the top ten view in this activity by
            // adding or replacing the top ten fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_ten_container, new TopTenActivityFragment(), TOPTENFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        handleIntent(getIntent());
        // Start Music Service
        Intent songIntent = new Intent(this, SongService.class);
        songIntent.putExtra("twoPane", mTwoPane);
        startService(songIntent);
        // If called from now playing, open player
        Intent intent = this.getIntent();
        if(intent != null && intent.hasExtra("openPlayer")) {
            if(intent.getBooleanExtra("openPlayer", false))
                playSelectedSong("", 0);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean nowPlaying = preferences.getBoolean("now_playing", false);
        Log.v("MainActivity", "Now Playing: " + nowPlaying);
        if(!nowPlaying)
            menu.removeItem(R.id.action_now_playing);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            // Assumes current activity is the searchable activity
            searchView.setQueryHint(getResources().getString(R.string.search_hint));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_now_playing){
            playSelectedSong("", 0);
            return true;
        }
        // Removed settings but kept method for P2. Will remove if not required.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    public void displayTopTen (String id, String name) {
        // Do something here based on input from fragment.
        if(mTwoPane) {
            // tablet version
            Bundle args = new Bundle();
            args.putString("artistId", id);
            args.putString("artistName", name);

            TopTenActivityFragment fragment = new TopTenActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_ten_container, fragment, TOPTENFRAGMENT_TAG)
                    .commit();
        }
        else {
        // phone version
                Intent detailIntent = new Intent(this, TopTenActivity.class);
                detailIntent.putExtra("artistId", id);
                detailIntent.putExtra("artistName", name);
                this.startActivity(detailIntent);}

    }

    public void playSelectedSong (String artistId, int position) {
        if(mTwoPane){
            // tablet version, show dialog
            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putString("artistId", artistId);

            DialogFragment dialogPlayerFragment = new DialogPlayerFragment();
            dialogPlayerFragment.setArguments(args);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            // Remove any existing instances of the dialog
            Fragment prev = getSupportFragmentManager().findFragmentByTag(PLAYERFRAGMENT_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            dialogPlayerFragment.show(ft, PLAYERFRAGMENT_TAG);

        }
        else {
            // phone version, open dialog as fragment  -- note will never be called
            Intent playerIntent = new Intent(this, EmbeddedPlayerActivity.class);
            playerIntent.putExtra("artistId", artistId);
            playerIntent.putExtra("posValue", position);
            this.startActivity(playerIntent);
        }
    }

    /**
     * Calls the show results method with the Search intent query.
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    /**
     * Calls the Spotify Artist Search API in the Main Activity Fragment.
     * @param query
     */
    private void showResults(String query) {
        if(isNetworkAvailable()) {
            MainActivityFragment fragmentMain = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
            fragmentMain.artistSearch(query);
        }
        else {
            // Error message for network failure
            String toastText = "Network Unavailable"; // what the toast should display
            Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);  // create the toast
            toast.show(); // display the toast
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onResume(){
        invalidateOptionsMenu();
        super.onResume();
    }

}
