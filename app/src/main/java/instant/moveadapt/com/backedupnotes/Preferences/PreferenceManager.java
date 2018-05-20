package instant.moveadapt.com.backedupnotes.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by cristof on 24.03.2018.
 */

public class PreferenceManager {

    private static final String ENCRYPTED_PREF = "ENCRYPTED_PREFERENCE";
    private static final String ENCRYPTION_LOOKS_GOOD_PASSWORD ="ENCRYPTION_LOOKS_GOOD_PASSWORD";
    private static final String PREFERENCE_EXIT_WITHOUT_ENCRYPTION = "PREFERENCE_EXIT_WITHOUT_ENCRYPTION";

    public static boolean areEncrypted(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(ENCRYPTED_PREF, false);
    }

    public static void setEncrypted(Context context, boolean encrypted){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(ENCRYPTED_PREF, encrypted).commit();
    }

    public static void setLooksGoodPassword(Context context, String passwordToStore){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(ENCRYPTION_LOOKS_GOOD_PASSWORD, passwordToStore).commit();
    }

    public static String getLooksGoodPassword(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(ENCRYPTION_LOOKS_GOOD_PASSWORD, null);
    }

    public static void clearCachedPassword(Context context){
        instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.setLooksGoodPassword(
                context, null);
    }

    public static boolean exitWithoutEncrypt(Context context) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        boolean val = prefs.getBoolean(PREFERENCE_EXIT_WITHOUT_ENCRYPTION, false);
        return val;
    }

    public static void setExitWithoutEncrypt(Context context, boolean exitWithoutEncryption){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_EXIT_WITHOUT_ENCRYPTION, exitWithoutEncryption);
        editor.commit();
    }

    public static boolean arePartiallyDecrypted(Context context){
        if (PreferenceManager.getLooksGoodPassword(context) != null)
            return true;
        return false;
    }
}
