package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Managers.PreferenceManager;
import instant.moveadapt.com.backedupnotes.R;
import instant.moveadapt.com.backedupnotes.Views.NoteStateView;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private int[] notesStates;

    public NoteListRecyclerViewAdapter(Context context){
        this.context = context;
        this.notesStates = NoteManager.getNotesStates(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private View rootView;

        public MyViewHolder(View v){
            super(v);
            this.rootView = v;
        }

        public View getRootView(){
            return this.rootView;
        }

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

        if (holder != null){

            Button state = (Button) holder.getRootView().findViewById(R.id.note_list_item_button);
            TextView tv = (TextView)holder.getRootView().findViewById(R.id.note_list_item_text_view);

            File noteFile = FileManager.getFileForIndex(context, position);
            int numFiles = FileManager.getNumNotes(context);

            try {
                BufferedReader reader = new BufferedReader(new FileReader(noteFile));
                String firstLine = reader.readLine();
                if (firstLine != null && !firstLine.equals("")){
                    String first10Chars;
                    if (firstLine.length() >= 10) {
                        first10Chars = firstLine.substring(0, 9);
                        tv.setText(first10Chars + "..");
                    } else {
                        first10Chars = firstLine;
                        tv.setText(first10Chars);
                    }

                    Resources resources = context.getResources();
                    if (NoteManager.getNoteStateForIndex(context, position) == Constants.STATE_LOCAL){
                        state.setText(resources.getString(R.string.state_local));
                        state.setBackgroundColor(Color.RED);
                    } else if (NoteManager.getNoteStateForIndex(context, position) == Constants.STATE_GLOBAL){
                        state.setText(resources.getString(R.string.state_cloud));
                        state.setBackgroundColor(Color.BLACK);
                    } else if (NoteManager.getNoteStateForIndex(context, position) == Constants.STATE_MODIFIED){
                        state.setText(resources.getString(R.string.state_modified));
                        state.setBackgroundColor(Color.LTGRAY);
                    }
                } else {
                    Resources resources = context.getResources();
                    tv.setText(resources.getString(R.string.note_unknown_title));
                    state.setVisibility(View.INVISIBLE);
                }

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return FileManager.getNumNotes(context);
    }
}
