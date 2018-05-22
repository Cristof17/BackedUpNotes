package instant.moveadapt.com.backedupnotes.Cloud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.UUID;

import instant.moveadapt.com.backedupnotes.Database.DatabaseManager;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.Pojo.NoteManager;

public class CloudManager {
    private static final String TAG = "CLOUD_MANAGER";

    public static void login(final Context context, String email, String password, final LoginCallback loginCallback){

        if (context == null)
            return;
        if (email == null || email.equals(""))
            return;
        if (password == null || password.equals(""))
            return;
        if (loginCallback == null)
            return;

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null)
            return;

        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    try {
                        if (task.isSuccessful()) {
                            downloadNotesFromCloud(context);
                            deleteNotesToBeDeletedFromCloud(context);
                            updateCloudNotes(context, null, null);
                            loginCallback.onLoginSuccessful();
                        } else {
                            Log.d(TAG, task.getException().toString());
                            Log.d(TAG, task.getResult().toString());
                            loginCallback.onLoginFailed(task.getException().getLocalizedMessage());
                        }
                    }catch (Exception e){
                        if (e instanceof FirebaseAuthInvalidCredentialsException){
                            loginCallback.onLoginFailed(e.getLocalizedMessage());
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loginCallback.onLoginFailed(e.getLocalizedMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void downloadNotesFromCloud(final Context context) {

        if (context == null)
            return;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return;

        if (mAuth != null && mAuth.getCurrentUser() != null){
            //send dummy note
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference dbRef = db.getReference();
            final DatabaseReference childRef = dbRef.child(getRemoteNotesFolder());
            childRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> data = dataSnapshot.getChildren();
                    Iterator<DataSnapshot> dataIterator = data.iterator();
                    while (dataIterator.hasNext()){
                        Note currNote = dataIterator.next().getValue(Note.class);
                        if (!currNote.getText().toString().equals("Dummy")) {
                            DatabaseManager.saveNoteLocally(context, currNote);
                        }

                    }
                    //when uploading encrypted notes to the cloud the
                    //callback above fires although no download has been initiated
                    //and this results in saving junk
                    childRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            sendDummyNote(context);
        }
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static String getRemoteNotesFolder() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return null;

        if (mAuth != null && mAuth.getCurrentUser() != null){
            String childName = mAuth.getCurrentUser().getUid();
            return childName;
        }
        return null;
    }

    private static void sendDummyNote(Context context) {
       if (context == null)
           return;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return;
        Note dummyNote = new Note(UUID.fromString("45db7961-6102-4a25-9447-387f4562319f").toString(), "Dummy", -1);
        saveSingleNoteToCloud(dummyNote, null);
        deleteNoteFromCloud(context, dummyNote);
    }

    private static void saveSingleNoteToCloud(final Note currNote, final NoteUploadedCallback updateCallback) {

        if (currNote == null)
            return;

        Log.d(TAG, "Save single note to cloud");
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userDb = db.getReference();
        String remoteNotesFolderName = getRemoteNotesFolder();
        DatabaseReference ref = userDb.child(remoteNotesFolderName).child(currNote.id.toString());
        ref.setValue(currNote).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete() && task.isSuccessful()){
                    if (updateCallback != null) {
                        updateCallback.onNoteUploaded(currNote);
                    }
                }
            }
        });
    }

    public static void deleteNoteFromCloud(Context context, Note note){
        if (context == null || note == null)
            return;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            storeNoteForLaterDelete(context, note);
        }

        FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance();
        DatabaseReference db = firebaseDb.getReference();
        String childName = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = db.child(childName);
        userRef.child(note.id.toString()).removeValue();

    }

    public static void storeNoteForLaterDelete(Context context, Note note){
        if (note == null)
            return;
        ContentValues vals = new ContentValues();
        vals.put(NotesDatabase.DeleteNotesContract._ID, note.id.toString());
        vals.put(NotesDatabase.DeleteNotesContract.COLUMN_TEXT, note.getText());
        vals.put(NotesDatabase.DeleteNotesContract.COLUMN_TIMESTAMP, note.getTimestamp());
        Uri uri = context.getContentResolver().insert(NotesDatabase.DeleteNotesContract.URI, vals);
    }

    public static void deleteNotesToBeDeletedFromCloud(Context context) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null){
            Log.d(TAG, "Deleting pending notes from the cloud");
            Cursor c = context.getContentResolver().query(NotesDatabase.DeleteNotesContract.URI,
                    NotesDatabase.DeleteNotesContract.getTableColumns(),
                    null,
                    null,
                    null);

            if (c != null && c.getCount() > 0){
                do {
                    c.moveToNext();
                    Note currNote = NoteManager.convertToNote(c);
                    deleteNoteFromCloud(context, currNote);

                    /**
                     * Delete the note from To be deleted from cloud Table in the db
                     */
                    String whereClause = NotesDatabase.DeleteNotesContract._ID + " = ? ";
                    String selectionArgs[] = {currNote.id.toString()};
                    context.getContentResolver().delete(NotesDatabase.DeleteNotesContract.URI,
                            whereClause,
                            selectionArgs);
                }while (!c.isLast());
            }
        }
    }

    public static void updateCloudNotes(Context context, StartUploadCallback startCallback,
                                        NoteUploadedCallback updateCallback) {
        if (context == null)
            return;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return;

        saveNotesToCloud(context, startCallback, updateCallback);
    }

    public static void saveNotesToCloud(Context context, StartUploadCallback startUploadCallback,
                                        NoteUploadedCallback updateCallback){
        Log.d(TAG, "Save note to cloud");
        if (context == null)
            return;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return;
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            Cursor c = context.getContentResolver().query(NotesDatabase.DatabaseContract.URI,
                    NotesDatabase.DatabaseContract.getTableColumns(),
                    null,
                    null,
                    null);

            if (startUploadCallback != null)
                startUploadCallback.onStartUpload(c.getCount());

            if (c != null && c.getCount() > 0) {
                do {
                    c.moveToNext();
                    Note currNote = NoteManager.convertToNote(c);
                    saveSingleNoteToCloud(currNote, updateCallback);
                } while (!c.isLast());
                Log.d(TAG, "Uploaded notes");
            }
        }
    }

    public static void register(final Context context, String email, String password, final RegisterCallback registerCallback){
        if (context == null)
            return;
        if (email == null || email.equals(""))
            return;
        if (password == null || email.equals(""))
            return;
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(mAuth == null)
            return;

        if(registerCallback == null)
            return;

        try {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    try {
                        if (task.isSuccessful()) {
                            registerCallback.onRegisterSuccessful();
                        } else {
                            registerCallback.onRegiserFailed(task.getException().getMessage());
                        }
                    }catch (Exception e){
                        if (e instanceof FirebaseAuthWeakPasswordException){
                            registerCallback.onRegiserFailed(e.getLocalizedMessage());
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    registerCallback.onRegiserFailed(e.getLocalizedMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isLoggedIn(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth != null && auth.getCurrentUser() != null)
            return true;
        return false;
    }

    public static String getLoggedInUsername(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return null;
        return mAuth.getCurrentUser().getDisplayName();
    }

    public static String getLoggedInEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || mAuth.getCurrentUser() == null)
            return null;
        return mAuth.getCurrentUser().getEmail();
    }
}
