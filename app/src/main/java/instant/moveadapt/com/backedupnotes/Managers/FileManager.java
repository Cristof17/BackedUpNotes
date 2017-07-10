package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import instant.moveadapt.com.backedupnotes.Constants;

/**
 * Created by cristof on 13.06.2017.
 */

public class FileManager {

    public static final String TAG = "[NOTES_MANAGER]";

    public static File createNotesFolder(Context context){
        File dir = context.getFilesDir();
//        File dir = Environment.getExternalStorageDirectory();
        Log.d(TAG, "External storage dir = " + dir.getAbsolutePath());
        File notesFolder = new File(dir, Constants.NOTES_FOLDER);
        if (!notesFolder.exists()){
            boolean created = notesFolder.mkdir();
            Log.d(TAG, "Creating notes folder = " + created);
        }
        return notesFolder;
    }

    public static File createNewNoteFile(Context context){
        File baseDir = createNotesFolder(context);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-mm-ss");
        Date date = new Date();
        String newNoteFileName = dateFormat.format(date);
        File newNoteFile = new File(baseDir, newNoteFileName);
        if (!newNoteFile.exists()){
            try {
                boolean created = newNoteFile.createNewFile();
                Log.d(TAG, "Creating file " + newNoteFileName + " " + created);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return newNoteFile;
    }

    public static int getNumNotes(Context context) {
        File notesDir = createNotesFolder(context);
        File[] notes = notesDir.listFiles();
        return notes.length;
    }

    public static File getFileForIndex(Context context, int index){
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        if (index >= files.length) {
            Log.d(TAG, "Cannot return file for " + index + "because index is to big for the files array");
            return null;
        }
        return files[index];
    }
}
