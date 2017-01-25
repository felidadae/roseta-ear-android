package com.example.felidadae.rosetus;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        RelativeLayout fretboard = (RelativeLayout) findViewById(R.id.activity_main);

        View fretboard = (View) findViewById(R.id.fretboard);
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private int passiveColor=-1;
    private int activeColor=Color.RED;
    public void fretboard_onclick(View view) {
        int currentColor = ((ColorDrawable)view.getBackground()).getColor();
        if (passiveColor == -1) {
            passiveColor = currentColor;
        }
        int fromColor;
        int toColor;
        if (passiveColor == currentColor) {
            fromColor = passiveColor;
            toColor   = activeColor;
        }
        else {
            fromColor = activeColor;
            toColor   = passiveColor;
        }
        ObjectAnimator colorAnimation = ObjectAnimator.ofInt(view, "backgroundColor",
            fromColor, toColor);
        colorAnimation.setRepeatCount(0);
        colorAnimation.setEvaluator(new ArgbEvaluator());
        colorAnimation.setInterpolator(new BounceInterpolator());
        colorAnimation.setTarget(view);
        colorAnimation.setDuration(3000);
        colorAnimation.start();

//        final View imageView = view;
//        ViewPropertyAnimator viewPropertyAnimator = imageView.animate()
//                .scaleX(0.5f)
//                .scaleY(0.5f)
//                .setDuration(1000)
//                .setInterpolator(new LinearInterpolator())
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        imageView.setScaleX(1);
//                        imageView.setScaleY(1);
//                    }
//                });
    }
}
