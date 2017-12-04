package com.example.felidadae.rosetus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.HashMap;


public class FretboardLayout extends RelativeLayout {
    public ISynth synthDelegate;
    public void setSynthDelegate(ISynth synthDelegate) {
        this.synthDelegate = synthDelegate;
        this.looper = new Looper(synthDelegate);
    }
    public FretboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void createNotes() {
        int width  = this.getWidth();
        int height = this.getHeight();

        noteSize     = (int) (S * (int) getResources().getDimension(R.dimen.noteRadius));
        minNoteSpace = (int) (S * (int) getResources().getDimension(R.dimen.minNoteSpace));
        margin       = (int) (S * (int) getResources().getDimension(R.dimen.fretboardMargin));

        xN = (int) ((width  - 2 * margin + minNoteSpace) / (noteSize + minNoteSpace));
        yN = (int) ((height - 2 * margin + minNoteSpace) / (noteSize + minNoteSpace));
        if (xN == 1) { realNoteSpaceX = 0; } 
		else         { realNoteSpaceX = ((width - (2 * margin)) - (xN * noteSize)) / (xN - 1); }
        if (yN == 1) { realNoteSpaceY = 0; } 
		else         { realNoteSpaceY = ((height - (2 * margin)) - (yN * noteSize)) / (yN - 1); }
		logLayout_sizes(xN, yN, width, height);
        for (int ix = 0; ix < xN; ix++)
            for (int iy = 0; iy < yN; iy++) {
                int left = (int) (margin + ix * (noteSize + realNoteSpaceX));
                int top  = (int) (margin + iy * (noteSize + realNoteSpaceY));
                createControler(left, top, ix, yN-1-iy);
				logLayout_controlerCreation(ix, iy, left, top);
            }
    }
    private void createControler(int x, int y, int ix, int iy) {
		/* factory method to build controller */
		View viewToAdd;

		ControlerType ctype = specialControlersMap.get(new Coordinates(ix, iy));
		boolean isSpecialControler = (ctype != null);
		if (isSpecialControler) {
			/* here we should have switch on different subgroups of controllers, 
			 * for now we have only looper */
			LooperControlerView looperControler = 
				new LooperControlerView(this.getContext(), this.looper, ix, iy, ctype);
			switch (ctype) {
				case LOOPER_RECORD: 
					looper.setRecordControler(looperControler);
					break;
				case LOOPER_OVERDUB:
					looper.setOverdubControler(looperControler);
					break;
				case LOOPER_UNDO:
					looper.setUndoControler(looperControler);
					break;
			}
			viewToAdd = (View) looperControler;
		}
		else {
			viewToAdd = (View) new NoteView(
					this.getContext(), this.synthDelegate, this.looper, ix, iy);
		}
        RelativeLayout.LayoutParams params = 
			new RelativeLayout.LayoutParams(noteSize, noteSize);
        params.leftMargin = x;
        params.topMargin  = y;
        this.addView(viewToAdd, params);
    }
	private void logLayout(String s) {
		Log.i("FretboardLayout", s);
	}
	private void logLayout_sizes(int xN, int yN, int width, int height) {
        logLayout("(xN, yN) <- (" + xN + ", " + yN + ")");
        logLayout("(width, height) <- (" + width + ", " + height + ")");
	}
	private void logLayout_controlerCreation(int ix, int iy, int left, int top) {
		logLayout(String.format(
			"create controler with indexes: (x: %d, y: %d)" +
			"with coordinates (left: %d, top: %d)", 
			ix, yN-1-iy, left, top));

	}
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
		chooseSpecialControlersCoordinates();
        createNotes();
    }
	private float S=1;
	private int noteSize;
	private int minNoteSpace;
	private int margin;
	private int xN, yN;
    private int realNoteSpaceX, realNoteSpaceY;

	private Looper looper;
	private class Coordinates {
		public int x, y;
		public Coordinates(int x, int y) { this.x=x; this.y=y; }
		public boolean equals(Object other) {
			Coordinates other_ = (Coordinates) other;
			return this.x == other_.x && this.y == other_.y;
		}
		public int hashCode() {
			return this.x*31 + this.y;
		}
	}
	private void chooseSpecialControlersCoordinates() {
		this.specialControlersMap.clear(); // clear hashmap

		/* @TODO we should by code choose coordinates for special controlers */
		this.specialControlersMap.put( new Coordinates(0,8), ControlerType.LOOPER_RECORD);
		this.specialControlersMap.put( new Coordinates(1,8), ControlerType.LOOPER_OVERDUB);
		this.specialControlersMap.put( new Coordinates(2,8), ControlerType.LOOPER_UNDO);
	}
	private HashMap<Coordinates, ControlerType> specialControlersMap = new HashMap<Coordinates, ControlerType>();
}
