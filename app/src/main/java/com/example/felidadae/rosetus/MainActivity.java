package com.example.felidadae.rosetus;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity implements ISynth {
    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();
    public native void start(int sample_rate, int buf_size);
    public native void attackNote(int positionX, int positionY);
    public native void releaseNote(int positionX, int positionY);
    public native void bendNote(int positionX, int positionY, float bendingIndexX, float bendingIndexY);
    public native void unbendNote(int positionX, int positionY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // final View decorView = getWindow().getDecorView();
        // int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        //         | View.SYSTEM_UI_FLAG_FULLSCREEN;
        // decorView.setSystemUiVisibility(uiOptions);

        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        FretboardLayout fretboardLayout = (FretboardLayout) findViewById(R.id.activity_main);
        fretboardLayout.setSynthDelegate((ISynth) this);

        this.looper = new Looper(fretboardLayout.getContext(), this, fretboardLayout);
        fretboardLayout.looper = this.looper;

        start(441, 256);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private Looper looper;
}
