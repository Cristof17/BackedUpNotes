package instant.moveadapt.com.backedupnotes.Encrypt;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Created by cristof on 13.05.2018.
 */
public class EncryptManager {


    private static String TAG = "ENCRYPTION_MANAGER";

    public static boolean notesAreEncrypted(Context context){
        boolean areEncrypted = instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.
                areEncrypted(context);
        return areEncrypted;
    }

    public static String generateKey(Context context) {

        KeyGenerator keyGenerator = null;
        AlgorithmParameterSpec specs;
        SecretKey key;
        Cipher cipher;
        ByteArrayOutputStream bos;
        ByteArrayInputStream bis;
        int chunkSize = 1024;
        int bisAvaialable;
        byte[] result;
        byte[] part;
        byte[] partialSolution;
        byte[] partResult;

        try {
            keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            specs = new KeyGenParameterSpec.Builder(
                    "key",
                    KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(false)
                    .build();
            keyGenerator.init(specs);
            key = keyGenerator.generateKey();

            bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(key);
            oos.close();
            String keyString = Base64.encodeToString(bos.toByteArray(), 0);

            android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("Key", keyString).commit();
            return keyString;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean notesAreCorrectlyDecrypted(Context context){
        return instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.getLooksGoodPassword(context) == null;
    }

    public static SecretKey getKey(Context context){
        String keyJSON = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("Key", null);
        SecretKey key = convertToKey(keyJSON);
        return key;
    }

    private static SecretKey convertToKey(String keyString){
        if (keyString == null)
            return null;
        final byte[] keyBytes = Base64.decode(keyString, 0);
        ByteArrayInputStream bis = new ByteArrayInputStream(keyBytes);
        SecretKey key = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            key = (SecretKey) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return key;

    }

    private static void encryptSingleNote(Context context, Note note, String password, SecretKey key){
        try {
            ContentValues vals = new ContentValues();
            String encryptedText = encrypt(note.text.getBytes("UTF-8"), password, key);
            if (encryptedText == null) {
                Toast.makeText(context, "Error when encrypting " +
                        note.text, Toast.LENGTH_SHORT).show();
                return;
            }
            vals.put(NotesDatabase.DatabaseContract.COLUMN_TEXT,
                    encryptedText);
            ContentResolver resolver = context.getContentResolver();
            String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
            String[] whereArgs = new String[] {note.id.toString()};

            resolver.update(NotesDatabase.DatabaseContract.URI,
                    vals,
                    whereClause,
                    whereArgs
            );
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    public static void encryptAllNotes(Context context, String password, SecretKey key, CryptStartCallback
                                       startCallback,final CryptUpdateCallback updateCallback){

        ContentObserver observer = null;

        setEncrypted(context, true);
        if (startCallback != null)
            startCallback.cryptographyOperationStarted(true);
        if (updateCallback != null) {
            observer = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange, uri);
                    if (uri.equals(NotesDatabase.DatabaseContract.URI)) {
                        updateCallback.cryptUpdate(true);
                    }
                }
            };
            context.getContentResolver().registerContentObserver(NotesDatabase.DatabaseContract.URI,
                    false,
                    observer);
        }

        Cursor c = context.getContentResolver().query(NotesDatabase.DatabaseContract.URI,
                NotesDatabase.DatabaseContract.getTableColumns(),
                null,
                null,
                null);

        if (c != null && c.getCount() > 0) {
            do {
                c.moveToNext();
                Note note = convertToNote(c);
                encryptSingleNote(context, note, password, key);
            } while (! c.isLast());
        }
        if (updateCallback != null)
            context.getContentResolver().unregisterContentObserver(observer);
    }

    public static void decryptAllNotes(Context context, String password, SecretKey key,
                                       CryptStartCallback startCallback, final CryptUpdateCallback updateCallback){

        ContentObserver observer = null;

        setEncrypted(context, false);
        if (startCallback != null)
            startCallback.cryptographyOperationStarted(false);
        if (updateCallback != null) {
            observer = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange, uri);
                    if (uri.equals(NotesDatabase.DatabaseContract.URI)) {
                        updateCallback.cryptUpdate(false);
                    }
                }
            };
            context.getContentResolver().registerContentObserver(NotesDatabase.DatabaseContract.URI,
                    false, observer);
        }

        Cursor c = context.getContentResolver().query(NotesDatabase.DatabaseContract.URI,
                NotesDatabase.DatabaseContract.getTableColumns(),
                null,
                null,
                null);

        if (c != null && c.getCount() > 0) {
            do {
                c.moveToNext();
                Note note = convertToNote(c);
                decryptSingleNote(context, note, password, key);
            } while (!c.isLast());
        }

        if (updateCallback != null)
            context.getContentResolver().unregisterContentObserver(observer);
    }

    private static void decryptSingleNote(Context context, Note note, String password, SecretKey key){
        try {

            ContentValues vals = new ContentValues();
            String decryptedText = decrypt(note.text, password, key);
            if (decryptedText == null) {
                Log.e(TAG, "Error when decrypting " +
                        note.text);
                return;
            }
            vals.put(NotesDatabase.DatabaseContract.COLUMN_TEXT,
                    decryptedText);
            ContentResolver resolver = context.getContentResolver();
            String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
            String[] whereArgs = new String[] {note.id.toString()};
            resolver.update(NotesDatabase.DatabaseContract.URI,
                    vals,
                    whereClause,
                    whereArgs
            );
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    private static String encrypt(byte[] data, String password, SecretKey key) throws UnsupportedEncodingException {

        Cipher cipher;
        ByteArrayOutputStream bos;
        ByteArrayInputStream bis;;
        int chunkSize = 1024;
        int bisAvaialable;
        byte[] result;
        byte[] part;
        byte[] partialSolution;
        byte[] partResult;

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(password.getBytes("UTF-8")));

            //encrypt
            bis = new ByteArrayInputStream(data);
            bos = new ByteArrayOutputStream();
            bisAvaialable = bis.available();
            part = new byte[bisAvaialable];
            partResult = new byte[bisAvaialable];

            while (bisAvaialable > 2 * chunkSize){
                part = new byte[chunkSize];
                bis.read(part, 0, chunkSize);
                partResult = cipher.update(part);
                bos.write(partResult);
                bisAvaialable = bis.available();
            }
            part = new byte[bisAvaialable];
            bis.read(part, 0, bisAvaialable);
            partResult = cipher.doFinal(part);
            bos.write(partResult);
            result = bos.toByteArray();
            Log.d(TAG, "Encrypted text = " + new String(result));
            String keyString = Base64.encodeToString(bos.toByteArray(), 0);
            return keyString;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String decrypt(String base64Data, String password, SecretKey key) throws UnsupportedEncodingException {

        Cipher cipher;
        ByteArrayOutputStream bos;
        ByteArrayInputStream bis;
        ;
        int chunkSize;
        int bisAvaialable;
        byte[] result;
        byte[] part;
        byte[] partialSolution;
        byte[] partResult;
        byte[] dataBytes = Base64.decode(base64Data.getBytes(), 0);
        try {

            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(password.getBytes("UTF-8")));

            //decrypt
            bis = new ByteArrayInputStream(dataBytes);
            bos = new ByteArrayOutputStream();
            bisAvaialable = bis.available();
            part = new byte[bisAvaialable];
            partResult = new byte[bisAvaialable];
            chunkSize = 1024;
            while (bisAvaialable > chunkSize) {
                part = new byte[chunkSize];
                bis.read(part, 0, chunkSize);
                partResult = cipher.update(part);
                bos.write(partResult);
                bisAvaialable = bis.available();
            }
            part = new byte[bisAvaialable];
            bis.read(part, 0, bisAvaialable);
            partResult = cipher.doFinal(part);
            bos.write(partResult);
            Log.d(TAG, "Decrypted = " + new String(bos.toByteArray()));
            return new String(bos.toByteArray());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setEncrypted(Context context, boolean encrypted) {
        instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.setEncrypted(
                context,
                encrypted);
    }

    private static Note convertToNote(Cursor c){
        if (c == null){
            return null;
        }
        Note n = null;

        String text = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TEXT));
        long timestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP)));
        String id = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract._ID));
        n = new Note(id, text, timestamp);

        return n;
    }

}
