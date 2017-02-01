package com.example.felidadae.rosetus;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI();
	public native void start(int sample_rate, int buf_size);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
		start(441, 256);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        FretboardLayout fretboard = (FretboardLayout) findViewById(R.id.activity_main);
//        fretboard.prepareNotes();
    }
}
