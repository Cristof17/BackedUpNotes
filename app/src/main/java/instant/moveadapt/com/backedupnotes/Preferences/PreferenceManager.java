package instant.moveadapt.com.backedupnotes.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cristof on 24.03.2018.
 */

public class PreferenceManager {

    private static final String ENCRYPTED_PREF = "ENCRYPTED_PREFERENCE";

    public static boolean areEncrypted(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(ENCRYPTED_PREF, false);
    }

    public static void setEncrypted(Context context, boolean encrypted){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(ENCRYPTED_PREF, encrypted).commit();
    }
}
