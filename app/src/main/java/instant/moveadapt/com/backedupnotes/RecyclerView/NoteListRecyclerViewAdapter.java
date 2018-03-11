package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder>{

    private Context context;
    /*
     * This cursor is used to cache notes from database
     */
    private Cursor cursor;

    public NoteListRecyclerViewAdapter(Context context){
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.note_item_layout, parent, false);
        MyViewHolder newViewHolder = new MyViewHolder(rootView);
        return newViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        View rootView = holder.rootView;
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
        do{
            cursor.moveToNext();
        }while (cursor.getPosition() == position-1);

        Note nota = convertToNote(cursor);
        textView.setText(nota.text + "");

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
            return cursor.getCount();
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        public View rootView;

        public MyViewHolder(View v){
            super(v);
            this.rootView = v;
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
