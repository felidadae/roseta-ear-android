package com.example.felidadae.rosetus;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;


public class LooperControlerView extends View implements LooperControlerUIInterface {
	/* contructors */
    public LooperControlerView(Context context, Looper looper, int x, int y, ControlerType controlerType) {
        super(context);
		this.controlerType = controlerType;
		this.looper = looper;
		this.x__ = x;
		this.y__ = y;
        setAlpha(0.4f);
        initPaint();

		switch (controlerType) {
			case LOOPER_RECORD: 
				this.enable(false);
				break;
			case LOOPER_OVERDUB:
				this.disable(false);
				break;
			case LOOPER_UNDO:
				this.disable(false);
				break;
		}
	}
    public LooperControlerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlpha(0.0f);
        initPaint();
    }

	/* on touch event */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
			looper.toggle_state(this.controlerType);
		}
		return true;
	}

	/* LooperControlerUIInterface */
	public int defaultAnimationLength = 500;
	public void enable(boolean ifAnimate) {
	    this.animateColor(Color.WHITE, defaultAnimationLength);
	}
	public void disable(boolean ifAnimate) {
        this.animateColor(Color.DKGRAY, defaultAnimationLength);
	}
	public void makeActive(boolean ifAnimate) {
        this.animateColor(Color.GREEN, defaultAnimationLength);
	}
	public void indicateLast(boolean ifAnimate) {
        this.animateColor(Color.YELLOW, defaultAnimationLength);
	}
    public void indicateWaiting(boolean ifAnimate) {;}

    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        int unactiveColor = getResources().getColor(R.color.defaultValue);
		switch (this.controlerType) {
            case LOOPER_RECORD:
				unactiveColor = getResources().getColor(R.color.looperOverdube);
				break;
			case LOOPER_OVERDUB:
				unactiveColor = getResources().getColor(R.color.looperOverdube);
				break;
			case LOOPER_UNDO:
				unactiveColor = getResources().getColor(R.color.looperOverdube);
				break;
		}
        paint.setColor(unactiveColor);
    }

	/* drawing; animations */
    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }
    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        RectF rect = new RectF(0, 0, this.getWidth(), this.getWidth());
        canvas.drawRoundRect(rect, this.getWidth(), this.getWidth(), paint);
    }


    /* animations */
    public void animateColor(int newColor, int length) {
        final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(
                this,
                "color",
                new ArgbEvaluator(),
                paint.getColor(),
                newColor);
        backgroundColorAnimator.setDuration(length);
        backgroundColorAnimator.start();
    }
    public void animateAlpha(float newAlpha, int length) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", newAlpha);
        anim.setDuration(length);
        anim.start();
    }

	/* properties */
	private ControlerType controlerType;
	public ControlerType getControlerType() { return this.controlerType; }
	private int x__, y__;
	private Looper looper;

	/* Logging */
	private void logLooperControler(String event_type) {
		String s = String.format("%s of looper touch type %s (%d, %d)", 
				event_type, this.controlerType, this.x__, this.y__);
		Log.d("LooperTouchEvent", s);
	}
	private void logLooperControler_alpha() {
		String s = String.format("Alpha of looper controler type %s is %f", 
				this.controlerType, this.getAlpha());
		Log.d("LooperTouchEvent", s);
	}
}
