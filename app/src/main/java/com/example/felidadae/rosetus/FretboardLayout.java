package com.example.felidadae.rosetus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;


public class FretboardLayout extends RelativeLayout {
    public ISynth synthDelegate;
    public FretboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void createNote(int x, int y, int ix, int iy) {
        NoteView note = new NoteView(this.getContext());

		/* Added x,y attributes to note*/
		note.x__ = ix;
		note.y__ = iy;

        //note.setBackgroundColor(getResources().getColor(R.color.noteUnactive));
        note.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    Log.d("TouchTest", "Touch down");
                    fretboard_onclick(view);
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    Log.d("TouchTest", "Touch up");
                    fretboard_onclick(view);
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					int newX = Math.round(event.getX());
					int newY = Math.round(event.getY());

					int deltaX = view.getLeft() - newX;
					int deltaY = view.getTop() - newY;

                    Log.d("TouchTest", "Touch up");
                    fretboard_onmove(view, (-1) * deltaX, (-1) * deltaY);
                }
                return true;
            };}
        );
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
        for (int ix = 0; ix < xN; ix++){
            for (int iy = 0; iy < yN; iy++) {
                int x = (int) (margin + ix * (noteSize + realNoteSpaceX));
                int y = (int) (margin + iy * (noteSize + realNoteSpaceY));
                createNote(x, y, ix, yN-iy);
            }
        }
    }
    public void fretboard_onclick(View view) {
		NoteView noteView = ((NoteView) view);
        if (!noteView.ifActive) {
            synthDelegate.attackNote(noteView.x__, noteView.y__);
            noteView.animate_alpha();
            noteView.ifActive = true;
        }
        else {
            synthDelegate.releaseNote(noteView.x__, noteView.y__);
            noteView.animate_alpha();
            noteView.ifActive = false;
        }
    }

    public void fretboard_onmove(View view, float deltaX, float deltaY) {
        NoteView noteView = ((NoteView) view);
        if (noteView.ifActive) {
            synthDelegate.bendNote(noteView.x__, noteView.y__, deltaX, deltaY);
        }
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
