package com.example.felidadae.rosetus;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;


public class NoteView extends View {
    public void readResourceValues() {
        this.alphaActive   = Float.parseFloat(getResources().getString(R.string.activeNoteAlpha));
        this.alphaInactive = Float.parseFloat(getResources().getString(R.string.unactiveNoteAlpha));
        this.colorInactive = getResources().getColor(R.color.noteUnactive);
        this.colorActive   = getResources().getColor(R.color.noteActive);
    }

    public NoteView(Context context, ISynth synthDelegate, Looper looper, int x, int y) {
        super(context);

        this.looper = looper;
        this.synthDelegate = synthDelegate;
        this.x__ = x;
        this.y__ = y;

        this.readResourceValues();

        initPaint();
        ifActive = false;
        this.setAlpha(this.alphaInactive);
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
        int deltaX = (-1) * (this.initialMoveX - newX) /7;
        int deltaY = (-1) * (this.initialMoveY - newY) /3;

        int lying_deltaX = deltaX - 10*deltaY;
        if (this.ifActive) {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.BEND, deltaX, 0);
            synthDelegate.bendNote(this.x__, this.y__, deltaX, 0);
            ArgbEvaluator evaluator = new ArgbEvaluator();
            int r = (int) evaluator.evaluate(Math.min((float)(Math.abs(deltaX)/135.0), (float)1.0), this.colorInactive, this.colorActive);
            this.setColor(r);
        }
        logNote(String.format("EVENT_MOVE with value (%d, %d)", deltaX, deltaY));
    }
    public void onClick_handler(MotionEvent event) {
        if (!this.ifActive) {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.ATTACK);
            synthDelegate.attackNote(this.x__, this.y__);
            this.animate_alpha();
            //this.animate_color();
            this.animate_size();
            this.ifActive = true;
            this.initialMoveX = (int) event.getX();
            this.initialMoveY = (int) event.getY();
        }
        else {
            looper.notifyEvent(this.x__, this.y__, LooperEventType.RELEASE);
            synthDelegate.releaseNote(this.x__, this.y__);
            this.animate_alpha();
            this.animate_color();
            this.animate_size();
            this.ifActive = false;
            synthDelegate.unbendNote(this.x__, this.y__);
            looper.notifyEvent(this.x__, this.y__, LooperEventType.UNBEND);
        }
    }


    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        // paint.setPathEffect(new DashPathEffect(new float[] {80,30}, 0));
        paint.setColor(colorInactive);
    }

    public void setColor(int color) {
        this.currentColor = color;
        paint.setColor(color);
        invalidate();
    }

    public void setDeltaRadius(int deltaRadius) {
        this.deltaRadius = deltaRadius;
        invalidate();
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        /* @TODO do not allocate here data */
        RectF rect = new RectF(
                2+deltaRadius,
                2+deltaRadius,
                this.getWidth()-4-deltaRadius,
                this.getWidth()-4-deltaRadius);
        canvas.drawRoundRect(rect, this.getWidth(), this.getWidth(), paint);
    }

    void animate_alpha(){
        if (!ifActive) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", alphaActive);
            anim.setDuration(500);
            anim.start();
        }
        else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", this.alphaInactive);
            anim.setDuration(900); // duration 3 seconds
            anim.start();
        }
    }

    void animate_color() {
        if (!ifActive) {
            ArgbEvaluator evaluator = new ArgbEvaluator();
            ObjectAnimator animator = ObjectAnimator.ofObject(
                    this, "color", evaluator,
                    currentColor, colorActive);
            animator.setDuration(500).start();
        }
        else {
            ArgbEvaluator evaluator = new ArgbEvaluator();
            ObjectAnimator animator = ObjectAnimator.ofObject(
                    this, "color", evaluator,
                    currentColor, colorInactive);
            animator.setDuration(900).start();
        }
    }

    void animate_size() {
        if (!ifActive) {
            ObjectAnimator anim = ObjectAnimator.ofInt(this, "deltaRadius", 0, 10);
            anim.setDuration(200);
            anim.start();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofInt(this, "deltaRadius", 10,0);
            anim.setDuration(900);
            anim.start();
        }
    }

    private int x__,y__;
    private int deltaRadius = 0;
    private boolean ifActive;
    private int initialMoveX, initialMoveY;
    private float alphaActive, alphaInactive;
    private int currentColor;
    private int colorActive, colorInactive;
    private ISynth synthDelegate;
    private Looper looper;

}
