package com.example.felidadae.rosetus;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class NoteView extends View {
	public int x__,y__;
    public boolean ifActive;
	public int initial_move_x, initial_move_y;
    public float alpha_active, alhpa_inactive;

    public NoteView(Context context) {
        super(context);
        setAlpha(0.0f);
        this.alpha_active  = 1.0f;
        this.alhpa_inactive= 0.4f;
        Log.i("NoteView", "(alpha_active, alpha_inactive) <- (" + alpha_active + ", " + alhpa_inactive + ")");
        initPaint();
        ifActive = false;
        this.setAlpha(0.4f);
    }
    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlpha(0.0f);
        initPaint();
        ifActive = false;
    }

    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        int unactiveColor=getResources().getColor(R.color.noteUnactive);
        paint.setColor(unactiveColor);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        RectF rect = new RectF(0, 0, this.getWidth(), this.getWidth());
        canvas.drawRoundRect(rect, this.getWidth(), this.getWidth(), paint);
    }

    void animate_alpha(){
        if (!ifActive) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", alpha_active);
            anim.setDuration(500);
            anim.start();
        }
        else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", this.alhpa_inactive);
            anim.setDuration(900); // duration 3 seconds
            anim.start();
        }
    }
}
