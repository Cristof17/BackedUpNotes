package instant.moveadapt.com.backedupnotes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;

import instant.moveadapt.com.backedupnotes.Preferences.MyPreferenceFragment;

public class SettingsActivity extends AppCompatActivity{

    private static final String TAG = "SETTINGS_ACTIVITY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        setSupportActionBar((Toolbar) findViewById(R.id.settings_activity_layout_toolbar));
        if (((AppCompatActivity)this).getSupportActionBar() != null){
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.login_activity_layout_toolbar, new MyPreferenceFragment()).commit();
    }

}
