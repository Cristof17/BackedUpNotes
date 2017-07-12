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

    private ArrayList<Boolean> selectedItems;
    private boolean hasItemAlreadySelected;

    public ActionModeMonitor(int maxSize){
        selectedItems = new ArrayList<Boolean>(maxSize);
        for (int i = 0; i < maxSize; ++i)
            selectedItems.add(new Boolean(false));
        hasItemAlreadySelected = false;
        Log.d(TAG, " Monitor booleans size = " + selectedItems.size());
    }

    public void setActivated(int position, boolean selected){
        selectedItems.set(position, selected);
        //first element
        if (!hasItemAlreadySelected){
            if(selected)
                hasItemAlreadySelected = true;
        } else {
            boolean foundAtLeasOneTrue = false;
            for (Boolean b : selectedItems){
                if (b == true){
                    foundAtLeasOneTrue = true; //just for code undestanding
                    return;
                }
            }
            if (!foundAtLeasOneTrue)
                hasItemAlreadySelected = false;
        }
    }

    public boolean isSelected(){
        return this.hasItemAlreadySelected;
    }

    public void expandToSize(int newSize){

        ArrayList<Boolean> newArrayList = new ArrayList<Boolean>(newSize);
        for (int i = 0; i < newSize; ++i)
            newArrayList.add(false);
        if (selectedItems != null){
            for (int i = 0; i < selectedItems.size(); ++i){
                newArrayList.set(i, selectedItems.get(i));
            }
        }
        selectedItems = null;
        selectedItems = newArrayList;
    }

    public boolean getActivated(int position){
        return selectedItems.get(position);
    }
}
