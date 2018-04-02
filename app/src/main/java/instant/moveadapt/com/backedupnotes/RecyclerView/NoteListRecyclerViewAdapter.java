package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.EditNoteActivity;
import instant.moveadapt.com.backedupnotes.NewNoteActivity;
import instant.moveadapt.com.backedupnotes.NotesListActivity;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder> {

    private NotesListActivity context;
    /*
     * This cursor is used to cache notes from database
     */
    private Cursor cursor;

    private SelectedRecyclerViewItemCallback selectedItemCallback;

    public NoteListRecyclerViewAdapter(NotesListActivity context, SelectedRecyclerViewItemCallback selectedItemCallback){
        this.context = context;
        this.selectedItemCallback = selectedItemCallback;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.note_item_layout, parent, false);

        /*
         * Note object is null when creating viewHolder
         * It is initialized at binding time
         */
        MyViewHolder newViewHolder = new MyViewHolder(rootView, null);
        return newViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final View rootView = holder.rootView;
        TextView textView = (TextView) rootView.findViewById(R.id.note_list_item_text_view);

        /*
         * Set the text of the textView to the specific note text for the specific position
         */
        if (cursor == null){

            /*
             * Query the database to get the number of notes stored on the phone
             */
            String sortOrder = NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP + " DESC ";
            cursor = context.getContentResolver().query(
                    NotesDatabase.DatabaseContract.URI,
                    NotesDatabase.DatabaseContract.getTableColumns(),
                    null,
                    null,
                    sortOrder);
        }

        /*
         * Use do while because the cursor has position -1 at first so
         * wheather or not the cursor has no elements, it has to move
         * to 0 from -1
         */
        cursor.moveToPosition(position);

        Note nota = convertToNote(cursor);
        textView.setText(nota.timestamp %1000000 + " " + nota.text);

        /*
         * Set the note for this viewHolder
         */
        holder.note = nota;

        /*
         * Set the click listener for this layout
         */
        rootView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                /*
                 * Check if the action mode has been fired
                 *
                 * If it hasn't been fired
                 */
                if (context.actionMode == null) {

                    /*
                     * Check if the notes are encrypted
                     */
                    if (notesAreEncrypted()){
                        /*
                         * Do not allow editting show the message to decrypt first
                         */
                        Toast.makeText(context, "Notes need to be decrypted to be edited",
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        /*
                         * Edit the note
                         */
                        Intent editNoteIntent = new Intent(context, EditNoteActivity.class);
                        Bundle extras = new Bundle();
                        extras.putParcelable("note", holder.note);
                        editNoteIntent.putExtras(extras);
                        context.startActivity(editNoteIntent);
                    }

                }else{

                    /*
                     * Action mode has been fired
                     *
                     */
                    rootView.setSelected(!rootView.isSelected());
                    if (rootView.isSelected()){
                        selectedItemCallback.addNoteForDeletion(holder.note);
                    }else{
                        selectedItemCallback.removeNoteFromDeletion(holder.note);
                    }
                }
            }
        });

        /*
         * Set the long click listener for this item
         */
        rootView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {

                if (context.actionMode == null){
                    context.actionMode = context.startActionMode(context.actionModeCallback);
                    rootView.setSelected(true);
                    selectedItemCallback.addNoteForDeletion(holder.note);
                }
                return true;
            }
        });

        if (context.isSelected(nota)){
            rootView.setSelected(true);
        }else{
            rootView.setSelected(false);
        }
    }

    private boolean notesAreEncrypted(){
        boolean areEncrypted = PreferenceManager.areEncrypted(context);
        return areEncrypted;
    }

    @Override
    public int getItemCount() {

        if (cursor == null) {

            /*
             * Query the database to get the number of notes stored on the phone
             */
            String sortOrder = NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP + " DESC ";
            cursor = context.getContentResolver().query(
                    NotesDatabase.DatabaseContract.URI,
                    NotesDatabase.DatabaseContract.getTableColumns(),
                    null,
                    null,
                    sortOrder);

            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } else {
            if (cursor.getPosition() == cursor.getCount()-1){
                cursor.moveToFirst();
                cursor.moveToPrevious();
            }
            return cursor.getCount();
        }
    }

    public void resetCursor(){
        if (cursor != null){
            cursor.close();
            cursor = null;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public View rootView;
        public Note note;

        public MyViewHolder(View v, Note note){
            super(v);
            this.rootView = v;
            this.note = note;
        }
    }

    private Note convertToNote(Cursor c){
        if (c == null){
            return null;
        }
        Note n = null;

        String text = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TEXT));
        long timestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP)));
        UUID id = UUID.fromString(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract._ID)));
        n = new Note(id, text, timestamp);

        return n;
    }
}
