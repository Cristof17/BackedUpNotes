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

    public static boolean areEncrypted(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(ENCRYPTED_PREF, false);
    }

    public static void setEncrypted(Context context, boolean encrypted){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(ENCRYPTED_PREF, encrypted).commit();
    }
}
