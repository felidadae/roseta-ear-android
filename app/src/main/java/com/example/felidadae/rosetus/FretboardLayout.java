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

	public void logNote(View view, String event_type) {
		NoteView noteView = ((NoteView) view);
		Log.d(
			"NoteTouchEvent", 
			String.format("%s of note (%d, %d)", 
				event_type, noteView.x__, noteView.y__));
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
                    fretboard_onclick(view, event);
					logNote(view, "EVENT_DOWN");
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    fretboard_onclick(view, event);
					logNote(view, "EVENT_UP");
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    fretboard_onmove(view, event);
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

        xN = (int) ((width  - 2 * margin + minNoteSpace) / (noteSize + minNoteSpace));
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
                int left = (int) (margin + ix * (noteSize + realNoteSpaceX));
                int top = (int) (margin + iy * (noteSize + realNoteSpaceY));
                createNote(left, top, ix, yN-1-iy);
				Log.i("FretboardLayout", String.format("create note with indexes: (x: %d, y: %d) with coordinates (left: %d, top: %d)", ix, yN-1-iy, left, top));
            }
        }
    }
    public void fretboard_onclick(View view, MotionEvent event) {
		NoteView noteView = ((NoteView) view);
        if (!noteView.ifActive) {
            synthDelegate.attackNote(noteView.x__, noteView.y__);
            noteView.animate_alpha();
            noteView.ifActive = true;
			noteView.initial_move_x = (int) event.getX();
			noteView.initial_move_y = (int) event.getY();
        }
        else {
            synthDelegate.releaseNote(noteView.x__, noteView.y__);
            noteView.animate_alpha();
            noteView.ifActive = false;
			synthDelegate.unbendNote(noteView.x__, noteView.y__);
        }
    }

    public void fretboard_onmove(View view, MotionEvent event ) {
        NoteView noteView = ((NoteView) view);

		int newX = Math.round(event.getX());
		int newY = Math.round(event.getY());
		int deltaX = (-1) * (noteView.initial_move_x - newX) /3;
		int deltaY = (-1) * (noteView.initial_move_y - newY) /3;

        if (noteView.ifActive) {
            synthDelegate.bendNote(noteView.x__, noteView.y__, deltaX, deltaY);
        }
		logNote(view, String.format("EVENT_MOVE with value (%d, %d)", deltaX, deltaY));
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
