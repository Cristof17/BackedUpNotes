package instant.moveadapt.com.backedupnotes.Views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 10.07.2017.
 */

public class NoteStateView extends View {

    private int state;
    private Resources resources;

    public NoteStateView(Context context){
        super(context);
        this.state = Constants.STATE_LOCAL;
        this.resources = getResources();
    }

    public NoteStateView(Context context, AttributeSet attributes){
        super(context, attributes);
        this.state = Constants.STATE_LOCAL;
        resources = getResources();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (state == Constants.STATE_LOCAL){
            Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
            background.setColor(Color.RED);
            canvas.drawRect(0, 0, width, height, background);
        } else if (state == Constants.STATE_GLOBAL) {
            Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
            background.setColor(Color.BLUE);
            canvas.drawRect(0, 0, width, height, background);
        } else if (state == Constants.STATE_MODIFIED) {
            Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
            background.setColor(Color.GRAY);
            canvas.drawRect(0, 0, width, height, background);
        }

        if(state == Constants.STATE_LOCAL){
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.MONOSPACE);
            canvas.drawText(resources.getString(R.string.state_local), width/2, height/2, textPaint);
        } else if (state == Constants.STATE_GLOBAL){
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.MONOSPACE);
            canvas.drawText(resources.getString(R.string.state_local), width/2, height/2, textPaint);
        } else if (state == Constants.STATE_MODIFIED){
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.MONOSPACE);
            canvas.drawText(resources.getString(R.string.state_local), width/2, height/2, textPaint);
        }
    }

    public void setState(int newState){
        this.state = newState;
    }

    public int getState(){
        return this.state;
    }
}
