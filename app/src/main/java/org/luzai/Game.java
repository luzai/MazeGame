package org.luzai;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Display;
import android.view.WindowManager;

public class Game extends Activity {
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private PowerManager.WakeLock mWakeLock;

    public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Maze maze = (Maze)extras.get("maze");
		GameView view = new GameView(this,maze);
		setContentView(view);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();



    }
}
