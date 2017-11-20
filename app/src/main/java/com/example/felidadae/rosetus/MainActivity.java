package com.example.felidadae.rosetus;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;


public class MainActivity extends AppCompatActivity implements ISynth {
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        FretboardLayout fretboardLayout = (FretboardLayout) findViewById(R.id.activity_main);
        fretboardLayout.synthDelegate = (ISynth) this;

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
}
