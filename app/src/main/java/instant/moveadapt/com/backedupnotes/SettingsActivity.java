package instant.moveadapt.com.backedupnotes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;

import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.Preferences.MyPreferenceFragment;

public class SettingsActivity extends AppCompatActivity{

    private static final String TAG = "SETTINGS_ACTIVITY";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_layout);
        setSupportActionBar((Toolbar) findViewById(R.id.settings_activity_layout_toolbar));

        if (((AppCompatActivity)this).getSupportActionBar() != null){
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.settings);
        }

        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.settings_activity_layout_fragment, new MyPreferenceFragment()).commit();
                }
            }
        };
        
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(listener);
    }

    @Override
    protected void onResume() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.settings_activity_layout_fragment, new MyPreferenceFragment()).commit();
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (auth != null){
            auth.removeAuthStateListener(listener);
        }
    }
}
