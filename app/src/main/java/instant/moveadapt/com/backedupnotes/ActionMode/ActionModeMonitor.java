package instant.moveadapt.com.backedupnotes.ActionMode;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;
import java.util.logging.LogManager;

import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Notita;

/**
 * Created by cristof on 12.07.2017.
 */

public class ActionModeMonitor {

    private static final String TAG = "[ActionModeMonitor]";

    private static Hashtable<UUID, Boolean> selectedItems;
    private static boolean hasItemAlreadySelected;
    private static Context context;
    private static int max;
    private static int occupied;

    public ActionModeMonitor(Context context){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null) {
            max = 10 * notite.size();
        }else
            max = 10;
        occupied = 0;
        if (notite != null) {
            selectedItems = new Hashtable<UUID, Boolean>(max);
            for (Notita notita : notite) {
                selectedItems.put(notita.getUuid(), new Boolean(false));
                occupied++;
            }
        }

        hasItemAlreadySelected = false;
        Log.d(TAG, "Created hashtable for notes");
    }

    public static void addNote(Notita newNote){
        if (newNote != null) {
            UUID newNoteUUID = newNote.getUuid();
            if (selectedItems != null) {
                if (newNote != null) {
                    selectedItems.put(newNoteUUID, new Boolean(false));
                    occupied++;
                }
            }
        }
    }

    public static void setActivated(int position, boolean mode){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null && position < notite.size() && position >= -1) {
            if (notite != null) {
                if (selectedItems != null) {
                    selectedItems.put(notite.get(position).getUuid(), new Boolean(mode));
                }
            }
        }
    }

    public static boolean isSelected(){
        return hasItemAlreadySelected;
    }

    public static boolean getActivated(Context context, int position){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null && position < notite.size() && position >= 0) {
            if (notite != null && selectedItems != null) {
                Notita val = notite.get(position);
                return false;
            }
        }
        return false;
    }

    public static void setSelected(boolean hasItemAlreadySelected){
        ActionModeMonitor.hasItemAlreadySelected = hasItemAlreadySelected;
    }

    public static void resize(){
        Log.d(TAG, "resize()");
        Log.d(TAG, "Load factor = " + 0.7 * max);
        if (0.7f*max <= occupied) {
            Log.d(TAG, "Resizing hashtable = " + 0.7 * max + " occupied = " + occupied);
            if (selectedItems != null) {
                max = max * 10;
                Hashtable<UUID, Boolean> newNotite = new Hashtable<UUID, Boolean>();
                Enumeration<UUID> keys = selectedItems.keys();
                if (keys != null) {
                    while (keys.hasMoreElements()) {
                        UUID current = keys.nextElement();
                        newNotite.put(current, new Boolean (selectedItems.get(current)));
                    }
                }
                Log.d(TAG, "resize finished");
                selectedItems = null;
            }
        }else{
            Log.d(TAG, "No need to resize; Load factor = " + 0.7 * max + " occupied = " + occupied);
        }
    }
}
