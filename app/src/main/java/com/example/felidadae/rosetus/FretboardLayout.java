package com.example.felidadae.rosetus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.HashMap;


public class FretboardLayout extends RelativeLayout implements LooperUIDelegate {
    public ISynth synthDelegate;
    public void setSynthDelegate(ISynth synthDelegate) {
        this.synthDelegate = synthDelegate;
    }
    public Looper looper;
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

        chooseSpecialControlersCoordinates();

        for (int ix = 0; ix < xN; ix++)
            for (int iy = 0; iy < yN; iy++) {
                int left = (int) (margin + ix * (noteSize + realNoteSpaceX));
                int top  = (int) (margin + iy * (noteSize + realNoteSpaceY));
                View controler = createControler(left, top, ix, yN-1-iy);

                this.notesMap.put( new Coordinates(ix,yN-1-iy), controler);
                logLayout_controlerCreation(ix, iy, left, top);
            }
    }
    private View createControler(int x, int y, int ix, int iy) {
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
                    looper.setRecordControler((LooperControlerUIInterface) looperControler);
                    break;
                case LOOPER_OVERDUB:
                    looper.setOverdubControler((LooperControlerUIInterface) looperControler);
                    break;
                case LOOPER_UNDO:
                    looper.setUndoControler((LooperControlerUIInterface) looperControler);
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
        return viewToAdd;
    }
    public View getViewAtCoordinate(Coordinates coordinates) {
        return notesMap.get(coordinates);
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
        if (this.getChildCount() > 0) { return; }
        createNotes();
    }

    private float S=1;
    private int noteSize;
    private int minNoteSpace;
    private int margin;
    private int xN, yN;
    private int realNoteSpaceX, realNoteSpaceY;


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
        this.specialControlersMap.put( new Coordinates(0,this.yN-1), ControlerType.LOOPER_RECORD);
        this.specialControlersMap.put( new Coordinates(1,this.yN-1), ControlerType.LOOPER_OVERDUB);
        /* @TODO add undo button */ // this.specialControlersMap.put( new Coordinates(2,8), ControlerType.LOOPER_UNDO);
    }
    private HashMap<Coordinates, ControlerType> specialControlersMap = new HashMap<Coordinates, ControlerType>();
    private HashMap<Coordinates, View> notesMap = new HashMap<Coordinates, View>();

    @Override
    public void attackNote(int positionX, int positionY) {
        NoteView note = (NoteView) this.getViewAtCoordinate(new Coordinates(positionX, positionY));
        note.onClick_handler(0,0); /* probably shoudn't be 0,0 */
    }
    @Override
    public void releaseNote(int positionX, int positionY) {
        NoteView note = (NoteView) this.getViewAtCoordinate(new Coordinates(positionX, positionY));
        note.onClick_handler(0,0); /* probably shoudn't be 0,0 */
    }
}
