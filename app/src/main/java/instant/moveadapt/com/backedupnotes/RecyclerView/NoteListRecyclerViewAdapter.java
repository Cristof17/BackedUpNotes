package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import instant.moveadapt.com.backedupnotes.ActionMode.ActionModeMonitor;
import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder> implements View.OnLongClickListener, View.OnClickListener{

    private Context context;
    private ArrayList<Integer> notesStates;
    private ActionModeMonitor actionModeMonitor;
    private RecyclerView recyclerView;
    private Activity activity;
    private ActionMode.Callback actionModeCallback;

    public NoteListRecyclerViewAdapter(Context context, RecyclerView recyclerView, Activity activity, ActionMode.Callback actionModeCallback){
        this.context = context;
        this.notesStates = NoteManager.getNotesStates(context);
        this.recyclerView = recyclerView;
        this.actionModeMonitor = new ActionModeMonitor(FileManager.getNumNotes(context));
        this.activity = activity;
        this.actionModeCallback = actionModeCallback;
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
    public boolean onLongClick(View v) {
        if (v instanceof CardView){
            int longClickAdapterPosition = recyclerView.getChildAdapterPosition(v);
            if (!actionModeMonitor.isSelected()){
                activity.startActionMode(actionModeCallback);
            }
            actionModeMonitor.setActivated(longClickAdapterPosition, true);
            notifyDataSetChanged();
            Toast.makeText(context, "Position " + longClickAdapterPosition + " activated " + actionModeMonitor.getActivated(longClickAdapterPosition), Toast.LENGTH_LONG).show();
        }
        //this callback consumed the event return true
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof CardView){
            int position = recyclerView.getChildAdapterPosition(v);
            if (actionModeMonitor.isSelected()){
                actionModeMonitor.setActivated(position, (!actionModeMonitor.getActivated(position)));
                boolean activated = actionModeMonitor.getActivated(position);
            } else {
                //edit note activity
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.note_item_layout, parent, false);
        rootView.setOnLongClickListener(this);
        rootView.setOnClickListener(this);
        MyViewHolder newViewHolder = new MyViewHolder(rootView);
        return newViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        if (holder != null){

            View rootView = holder.getRootView();
            TextView tv = (TextView)holder.getRootView().findViewById(R.id.note_list_item_text_view);
            File noteFile = FileManager.getFileForIndex(context, position);

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
                        tv.setTextColor(Color.parseColor("#990000"));
                    } else if (NoteManager.getNoteStateForIndex(context, position) == Constants.STATE_GLOBAL){
                        tv.setTextColor(Color.parseColor("#000000"));
                    }
                } else {
                    Resources resources = context.getResources();
                    tv.setText(resources.getString(R.string.note_unknown_title));
                }

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            /*
                Put the colors
             */
            if (actionModeMonitor.getActivated(position)) {
                rootView.setActivated(actionModeMonitor.getActivated(position));
                rootView.setBackgroundColor(Color.RED);
            } else {
                rootView.setActivated(actionModeMonitor.getActivated(position));
                rootView.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        int numberOfItemsInTheList = FileManager.getNumNotes(context);
        if (actionModeMonitor != null){
            actionModeMonitor.expandToSize(numberOfItemsInTheList);
        }
        return numberOfItemsInTheList;
    }
}
