package com.example.felidadae.rosetus;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;


public class NoteView extends View {
    public NoteView(Context context, ISynth synthDelegate, Looper looper, int x, int y) {
        super(context);

        this.looper = looper;
        this.synthDelegate = synthDelegate;
        this.x__ = x;
        this.y__ = y;

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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            onClick_handler(event);
            logNote("EVENT_DOWN");
        }
        else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            onClick_handler(event);
            logNote("EVENT_UP");
        }
        else if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
            onMove_handler(event);
        }
        return true;
    }
    private void logNote(String event_type) {
        Log.d("NoteTouchEvent", String.format("%s of note (%d, %d)", event_type, this.x__, this.y__));
    }
    public void onMove_handler(MotionEvent event ) {
        int newX = Math.round(event.getX());
        int newY = Math.round(event.getY());
        int deltaX = (-1) * (this.initial_move_x - newX) /7;
        int deltaY = (-1) * (this.initial_move_y - newY) /3;

        int lying_deltaX = deltaX - 10*deltaY;
        if (this.ifActive) {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.BEND, deltaX, 0);
            synthDelegate.bendNote(this.x__, this.y__, deltaX, 0);
        }
        logNote(String.format("EVENT_MOVE with value (%d, %d)", deltaX, deltaY));
    }
    public void onClick_handler(MotionEvent event) {
        if (!this.ifActive) {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.ATTACK);
            synthDelegate.attackNote(this.x__, this.y__);
            this.animate_alpha();
            this.ifActive = true;
            this.initial_move_x = (int) event.getX();
            this.initial_move_y = (int) event.getY();
        }
        else {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.RELEASE);
            synthDelegate.releaseNote(this.x__, this.y__);
            this.animate_alpha();
            this.ifActive = false;
            synthDelegate.unbendNote(this.x__, this.y__);
            looper.notifyEvent(this.x__, this.y__, LooperEventType.UNBEND);
        }
    }


    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        int unactiveColor = getResources().getColor(R.color.noteUnactive);
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

    private int x__,y__;
    private boolean ifActive;
    private int initial_move_x, initial_move_y;
    private float alpha_active, alhpa_inactive;
    private ISynth synthDelegate;
    private Looper looper;

}
