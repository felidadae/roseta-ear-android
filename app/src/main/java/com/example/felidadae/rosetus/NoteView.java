package com.example.felidadae.rosetus;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by felidadae on 28.01.17.
 */

public class NoteView extends View {
    public NoteView(Context context) {
        super(context);
        initPaint();
    }
    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    /* State of a note:
     * active or unactive
      * */
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    private int state;

    private Paint paint;
    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAlpha(255);
        paint.setAntiAlias(true);
        int unactiveColor=getResources().getColor(R.color.noteUnactive);
        paint.setColor(unactiveColor);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
//        Path mPath = new Path();
//        mPath.addRoundRect(
//            new RectF(0, 0, this.getWidth(),this.getWidth()),
//            50,50,
//            Path.Direction.CCW);
//        canvas.clipPath(mPath, Region.Op.INTERSECT);
//        canvas.drawRect(0, 0, this.getWidth(),this.getWidth(),paint);
        RectF rect = new RectF(0, 0, this.getWidth(), this.getWidth());
        canvas.drawRoundRect(rect, this.getWidth(), this.getWidth(), paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }
    public int getColor() {
        return paint.getColor();
    }

    public void setPaintAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidate();
    }
    public int getPaintAlpha() {
        return paint.getAlpha();
    }

    /*
        Generic method to animate between active and
         unactive state of NoteView object;
    * */
    public interface CompareI<T> {
        boolean compare(T val1, T val2);
    }
    public <T, E extends TypeEvaluator> void animate__generic(
            T activeValue, T unactiveValue,
            Callable<T> fun_getCurrentVal,
            CompareI<Float> fun_compareVal,
            String property, E evaluator) throws Exception
    {
        T currentValue, fromValue, toValue;
        currentValue = fun_getCurrentVal.call();
        boolean ifUnactiveState=fun_compareVal.compare((Float)unactiveValue, (Float)currentValue);
        if (ifUnactiveState) {
            fromValue = unactiveValue;
            toValue   = activeValue;
        }
        else {
            fromValue = activeValue;
            toValue   = unactiveValue;
        }
        Log.i("DUPA", "currentValue" + currentValue);
        Log.i("DUPA", "fromValue" + fromValue);
        Log.i("DUPA", "toValue" + toValue);
        Log.i("DUPA", "activeValue" + activeValue);
        Log.i("DUPA", "unactiveValue" + unactiveValue);
        Log.i("DUPA", "compare" + (ifUnactiveState));

        ObjectAnimator colorAnimation = ObjectAnimator.ofObject(
                this, property, evaluator, fromValue, toValue);
        colorAnimation.setRepeatCount(0);
        colorAnimation.setTarget(this);
        colorAnimation.setDuration(1000);
        colorAnimation.start();
    }

    public void animate__color() {
        int activeColor = getResources().getColor(R.color.noteActive);
        int unactiveColor=getResources().getColor(R.color.noteUnactive);
    }

    public void animate__scale() {

    }

    public void animate__alpha() {
        final NoteView noteView = (NoteView) this;
        Callable<Float> fun_curr_val = new Callable<Float>() {
            public Float call() {
                return noteView.getAlpha();
            }
        };
        NoteView.CompareI<Float> fun_ifValuesEqual = new NoteView.CompareI<Float>() {
            public boolean compare(Float a1, Float a2) {
                return (a1-a2)<0.000001;
            }
        };
        try {
            noteView.animate__generic(
                    0.1f, (float)1,
                    fun_curr_val, fun_ifValuesEqual,
                    "alpha",
                    new FloatEvaluator());
        } catch  (java.lang.Exception e){
            Log.d("Dupa", "dupa");
            System.exit(1);
        }
    }
}
