package com.example.felidadae.rosetus;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.concurrent.Callable;


public class FretboardLayout extends RelativeLayout {
    public FretboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void createNote(int x, int y) {
        View note = new NoteView(this.getContext());
//        note.setBackgroundColor(getResources().getColor(R.color.noteUnactive));
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fretboard_onclick(v);
            }
        });
        RelativeLayout.LayoutParams params =
            new RelativeLayout.LayoutParams(noteSize, noteSize);
        params.leftMargin = x;
        params.topMargin  = y;
        this.addView(note, params);
    }
    public void prepareNotes() {
        int width = this.getWidth();
        int height = this.getHeight();

        //case on width and height

        //Read from R.dimen
        noteSize = (int) (S * (int) getResources().getDimension(R.dimen.noteRadius));
        minNoteSpace = (int) (S * (int) getResources().getDimension(R.dimen.minNoteSpace));
        margin = (int) (S * (int) getResources().getDimension(R.dimen.fretboardMargin));

        xN = (int) ((width - 2 * margin + minNoteSpace) / (noteSize + minNoteSpace));
        yN = (int) ((height - 2 * margin + minNoteSpace) / (noteSize + minNoteSpace));
        if (xN == 1) {
            realNoteSpaceX = 0;
        } else {
            realNoteSpaceX = ((width - (2 * margin)) - (xN * noteSize)) / (xN - 1);
        }
        if (yN == 1) {
            realNoteSpaceY = 0;
        } else {
            realNoteSpaceY = ((height - (2 * margin)) - (yN * noteSize)) / (yN - 1);
        }
        Log.i("FretboardLayout", "(xN, yN) <- (" + xN + ", " + yN + ")");
        Log.i("FretboardLayout", "(width, height) <- (" + width + ", " + height + ")");
//        createNote(margin,0);
//        createNote(width-margin,0);
        for (int ix = 0; ix < xN; ix++){
            for (int iy = 0; iy < yN; iy++) {
                int x = (int) (margin + ix * (noteSize + realNoteSpaceX));
                int y = (int) (margin + iy * (noteSize + realNoteSpaceY));
                createNote(x, y);
            }
        }
    }

    public void fretboard_onclick(View view) {
        ((NoteView) view).animate__alpha();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        prepareNotes();
    }

    private float S=1;
    private int noteSize;
    private int minNoteSpace;
    private int margin;
    private int xN, yN;
    private int realNoteSpaceX, realNoteSpaceY;
}
