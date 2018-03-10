package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
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
import instant.moveadapt.com.backedupnotes.EditNoteActivity;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Notita;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder> implements View.OnLongClickListener, View.OnClickListener{

    private Context context;
    private ActionModeMonitor actionModeMonitor;
    private RecyclerView recyclerView;
    private Activity activity;
    private ActionMode.Callback actionModeCallback;

    public NoteListRecyclerViewAdapter(Context context, RecyclerView recyclerView, Activity activity, ActionMode.Callback actionModeCallback){
        this.context = context;
        this.recyclerView = recyclerView;
        this.activity = activity;
        this.actionModeCallback = actionModeCallback;
        this.actionModeMonitor = new ActionModeMonitor(context);
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
        }
        //this callback consumed the event return true
        return true;
    }

    @Override
    public void onClick(View v) {
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (v instanceof CardView){
            int position = recyclerView.getChildAdapterPosition(v);
            if (actionModeMonitor.isSelected()){
                actionModeMonitor.setActivated(position, (!actionModeMonitor.getActivated(context, position)));
            } else {
                //edit note activity
                View rootView = recyclerView.findContainingItemView(v);
                int viewPosition = recyclerView.getChildAdapterPosition(rootView);
                Intent editNoteIntent = new Intent(context, EditNoteActivity.class);
                editNoteIntent.putExtra(Constants.INTENT_EDIT_FILE_POSITION, notite.get(viewPosition).getId());
                context.startActivity(editNoteIntent);
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
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (holder != null){

            View rootView = holder.getRootView();
            TextView tv = (TextView)holder.getRootView().findViewById(R.id.note_list_item_text_view);
            Notita notita = notite.get(position);
            tv.setText(notita.getNote());
            /*
                Put the colors
             */
            if (actionModeMonitor.getActivated(context, position)) {
                rootView.setActivated(actionModeMonitor.getActivated(context, position));
                rootView.setBackgroundColor(Color.RED);
            } else {
                rootView.setActivated(actionModeMonitor.getActivated(context, position));
                rootView.setBackgroundColor(Color.WHITE);
            }
            if (notita.getModified() == true){
                Resources res = context.getResources();
                int color = ContextCompat.getColor(context, R.color.colorAccent);
                tv.setTextColor(color);
            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d("NotesListRecycler", "getItemCount()");
        ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(context);
        if (notite != null)
            return notite.size();
        return 0;
    }


}
