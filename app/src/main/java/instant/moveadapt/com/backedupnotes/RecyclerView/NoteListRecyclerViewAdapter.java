package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.MyViewHolder> implements View.OnLongClickListener, View.OnClickListener{

    private Context context;
    private RecyclerView recyclerView;
    private Activity activity;
    private ActionMode.Callback actionModeCallback;

    public NoteListRecyclerViewAdapter(Context context, RecyclerView recyclerView, Activity activity){
        this.context = context;
        this.recyclerView = recyclerView;
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
        return true;
    }

    @Override
    public void onClick(View v) {
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
    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
