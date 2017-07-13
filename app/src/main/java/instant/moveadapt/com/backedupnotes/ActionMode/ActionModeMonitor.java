package instant.moveadapt.com.backedupnotes.ActionMode;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.LogManager;

/**
 * Created by cristof on 12.07.2017.
 */

public class ActionModeMonitor {

    private static final String TAG = "[ActionModeMonitor]";

    private static ArrayList<Boolean> selectedItems;
    private static boolean hasItemAlreadySelected;

    public ActionModeMonitor(int maxSize){
        selectedItems = new ArrayList<Boolean>(maxSize);
        for (int i = 0; i < maxSize; ++i)
            selectedItems.add(new Boolean(false));
        hasItemAlreadySelected = false;
        Log.d(TAG, " Monitor booleans size = " + selectedItems.size());
    }

    public static void setActivated(int position, boolean selected){
        if (selectedItems.size() != 0)
            selectedItems.set(position, selected);
    }

    public static void deleteActivated(int position){
        if (selectedItems != null){
            selectedItems.set(position, false);
            selectedItems.remove(position);
        }
    }

    public static boolean isSelected(){
        return hasItemAlreadySelected;
    }

    public static boolean getActivated(int position){
        if (selectedItems.size() > 0)
            return selectedItems.get(position);
        return false;
    }

    public static void setSelected(boolean hasItemAlreadySelected){
        ActionModeMonitor.hasItemAlreadySelected = hasItemAlreadySelected;
    }

    public static void refreshSize(int newSize){
        ArrayList<Boolean> newSelectedItems = new ArrayList<Boolean>();

        for (int i = 0; i < newSize; ++i){
            newSelectedItems.add(false);
        }

        for (int i = 0; i < selectedItems.size(); ++i){
            newSelectedItems.set(i, selectedItems.get(i));
        }
        selectedItems = null;
        selectedItems = newSelectedItems;
    }
}
