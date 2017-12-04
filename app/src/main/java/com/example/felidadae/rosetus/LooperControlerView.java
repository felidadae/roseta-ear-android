package com.example.felidadae.rosetus;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;


public class LooperControlerView extends View implements LooperControlerUIInterface {
    public LooperControlerView(Context context, Looper looper, int x, int y, ControlerType controlerType) {
        super(context);

		this.controlerType = controlerType;
		this.looper = looper;
		this.x__ = x;
		this.y__ = y;

        setAlpha(0.0f);
        this.alpha_active   = 1.0f;
        this.alhpa_inactive = 0.4f;
        initPaint();
        ifActive = false;
        this.setAlpha(0.4f);
    }
    public LooperControlerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlpha(0.0f);
        initPaint();
        ifActive = false;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
			looper.toggle_state(this.controlerType);
		}
		return true;
	}
	private void logNote(String event_type) {
		String s = String.format("%s of looper touch type %s (%d, %d)", 
				event_type, this.controlerType, this.x__, this.y__);
		Log.d("LooperTouchEvent", s);
	}

    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        int unactiveColor = getResources().getColor(R.color.defaultValue);
		switch (this.controlerType) {
            case LOOPER_RECORD:
				unactiveColor = getResources().getColor(R.color.looperRecord);
				break;
			case LOOPER_OVERDUB:
				unactiveColor = getResources().getColor(R.color.looperOverdube);
				break;
			case LOOPER_UNDO:
				unactiveColor = getResources().getColor(R.color.looperUndo);
				break;
		}
        paint.setColor(unactiveColor);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        RectF rect = new RectF(0, 0, this.getWidth(), this.getWidth());
        canvas.drawRoundRect(rect, this.getWidth(), this.getWidth(), paint);
    }

    public void animate_alpha() {
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

	/* LooperControlerUIInterface */
	public void setActive()   {;}
	public void setUnactive() {;}
	public void setBeeping()  {;}

	private ControlerType controlerType;
	public ControlerType getControlerType() {
		return this.controlerType;
	}
	private int x__, y__;
    private boolean ifActive;
	public void negateIfActive() {
		if (!ifActive) { ifActive = true; } else { ifActive = false; }
	}
    private float alpha_active, alhpa_inactive;
	private Looper looper;
}
