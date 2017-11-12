package instant.moveadapt.com.backedupnotes.ActionMode;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.LogManager;

import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Notita;

/**
 * Created by cristof on 12.07.2017.
 */

public class ActionModeMonitor {

    private static final String TAG = "[ActionModeMonitor]";

    private static Hashtable<Notita, Boolean> selectedItems;
    private static boolean hasItemAlreadySelected;
    private static Context context;

    public ActionModeMonitor(Context context){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null) {
            selectedItems = new Hashtable<Notita, Boolean>(20 * notite.size());
            for (Notita notita : notite) {
                selectedItems.put(notita, false);
            }
        }

        hasItemAlreadySelected = false;
        Log.d(TAG, "Created hashtable for notes");
    }

    public static void setActivated(int position, boolean mode){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null && position <= notite.size() && position >= 0) {
            if (notite != null) {
                    selectedItems.put(notite.get(position), mode);
            }
        }
    }

    public static boolean isSelected(){
        return hasItemAlreadySelected;
    }

    public static boolean getActivated(int position){
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null && position < notite.size() && position >= 0) {
            if (notite != null && selectedItems != null) {
                return selectedItems.get(notite.get(position));
            }
        }
        return false;
    }

    public static void setSelected(boolean hasItemAlreadySelected){
        ActionModeMonitor.hasItemAlreadySelected = hasItemAlreadySelected;
    }

    public static void resize(){
        Log.d(TAG, "resize()");
        if (selectedItems != null) {
            Hashtable<Notita, Boolean> newNotite = new Hashtable<Notita, Boolean>(selectedItems.size() * 10);
            Enumeration<Notita> keys = selectedItems.keys();
            if (keys != null) {
                while (keys.hasMoreElements()) {
                    Notita current = keys.nextElement();
                    newNotite.put(current, selectedItems.get(current));
                }
            }
            Log.d(TAG, "resize finished");
            selectedItems = null;
        }
    }
}
