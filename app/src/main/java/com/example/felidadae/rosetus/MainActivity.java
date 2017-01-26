package com.example.felidadae.rosetus;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();

    //to set by library user; only setters
    private float S=1; //scale
    private int noteSize;     
    private int minNoteSpace;
    private int margin;
    //to set by library; only getters
    private int xN, yN;
    private int realNoteSpaceX, realNoteSpaceY;
    private View createNote(int x, int y) {
        View note = new View(this);
        note.setBackgroundColor(Color.BLACK);
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fretboard_onclick(v);
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(noteSize, noteSize);
        params.leftMargin = x;
        params.topMargin  = y;
        return note;
    }
    private void prepareFretboard(RelativeLayout fretboard) {
        int width  = fretboard.getWidth();
        int height = fretboard.getHeight();

        //case on width and height

		//Read from R.dimen
        noteSize     = (int) (S*R.dimen.noteRadius);
        minNoteSpace = (int) (S*R.dimen.minNoteSpace);
        margin       = (int) (S*R.dimen.fretboardMargin);

        xN = (int) ((width  - 2*margin + minNoteSpace) / (noteSize + minNoteSpace));
        yN = (int) ((height - 2*margin + minNoteSpace) / (noteSize + minNoteSpace));
        realNoteSpaceX = ((width - 2*margin)/xN);
        realNoteSpaceY = ((height- 2*margin)/yN);

        for (int ix = 0; ix < xN; ix++)
            for (int iy = 0; iy < yN; iy++) {
                int x =(int) (margin + ix*(1+realNoteSpaceX)*noteSize);
                int y =(int) (margin + iy*(1+realNoteSpaceY)*noteSize);
                fretboard.addView(createNote(x,y));
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        RelativeLayout fretboard = (RelativeLayout) findViewById(R.id.activity_main);
        prepareFretboard(fretboard);
    }

    public void fretboard_onclick(View view) {
        int activeColor = R.color.noteActive;
        int unactiveColor=R.color.noteUnactive;

        int currentColor, fromColor, toColor;
        currentColor = ((ColorDrawable)view.getBackground()).getColor();
        if (unactiveColor == currentColor) {
            fromColor = unactiveColor;
            toColor   = activeColor;
        }
        else {
            fromColor = activeColor;
            toColor   = unactiveColor;
        }

        ObjectAnimator colorAnimation = ObjectAnimator.ofInt(view, "backgroundColor",
            fromColor, toColor);
        colorAnimation.setRepeatCount(0);
        colorAnimation.setEvaluator(new ArgbEvaluator());
        colorAnimation.setInterpolator(new BounceInterpolator());
        colorAnimation.setTarget(view);
        colorAnimation.setDuration(3000);
        colorAnimation.start();
    }
}
