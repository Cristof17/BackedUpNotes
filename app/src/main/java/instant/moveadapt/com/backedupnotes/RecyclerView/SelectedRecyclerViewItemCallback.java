package instant.moveadapt.com.backedupnotes.RecyclerView;

import instant.moveadapt.com.backedupnotes.Pojo.Note;

/**
 * Created by cristof on 12.03.2018.
 */

public interface SelectedRecyclerViewItemCallback {

    public void addNoteForDeletion(Note note);
    public void removeNoteFromDeletion(Note note);
}
