package au.edu.unimelb.boldapp.sensors;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.util.FloatMath;

/**
 * A simple ProximityDetector.
 *
 * Override the near method to react to shaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ProximityDetector {
  
  protected SensorManager sensorManager;
  
  // the threshold which needs to be crossed for the near method to be called.
  //
  protected float threshold;
  protected boolean close;

  protected final SensorEventListener sensorListener = new SensorEventListener() {
    
    public void onSensorChanged(SensorEvent sensorEvent) {
      float distance = sensorEvent.values[0];
      
      if (!close && distance <= threshold) {
        near(distance);
        close = true; // report only once
      }
      if (close && distance > threshold) {
        far(distance);
        close = false; // report only once
      }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // We ignore a changing accuracy for now.
    }
  };
  
  public ProximityDetector(Activity activity) {
    this(activity, 2.0f);
  }
  
  public ProximityDetector(
    Activity activity,
    float threshold
  ) {
    this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    this.threshold = threshold;
  }
  
  // Returns true if the phone is close.
  //
  public boolean isNear() {
    return close;
  }
  
  // Override this method to detect near events.
  //
  public void near(float distance) {};
  
  // Override this method to detect far events.
  //
  public void far(float distance) {};
  
  // Start listening to shaking at the beginning
  // of an activity.
  //
  public void start() {
    this.close = false;
    sensorManager.registerListener(
      sensorListener,
      sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
      SensorManager.SENSOR_DELAY_NORMAL
    );
  }
  
  // Stop listening to shaking at the end of an activity.
  //
  // Note: Do not forget to call this.
  //
  public void stop() {
    sensorManager.unregisterListener(sensorListener);
  }
  
}