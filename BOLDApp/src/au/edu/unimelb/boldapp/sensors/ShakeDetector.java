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
 * A simple ShakeDetector.
 *
 * Optional parameter: threshold, a float which indicates
 * when to report shake events. 
 *
 * Override the shaken method to react to shaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ShakeDetector {
  
  protected SensorManager sensorManager;
  protected float acceleration;        // acceleration relative to gravity
  protected float currentAcceleration; // current acceleration including gravity
  protected float lastAcceleration;    // last acceleration including gravity
  
  // the threshold which needs to be crossed for the shaken method to be called.
  //
  protected float threshold;
  protected boolean goneOver;

  protected final SensorEventListener sensorListener = new SensorEventListener() {
    
    // 
    //
    public void onSensorChanged(SensorEvent sensorEvent) {
      float x = sensorEvent.values[0];
      float y = sensorEvent.values[1];
      float z = sensorEvent.values[2];
      lastAcceleration = currentAcceleration;
      currentAcceleration = FloatMath.sqrt(x*x + y*y + z*z);
      float delta = currentAcceleration - lastAcceleration;
      acceleration = acceleration * 0.7f + delta * 0.3f; // Slight filtering.
      if (!goneOver && acceleration >= threshold) {
        shaken(acceleration);
        goneOver = true; // report only once
      }
      if (goneOver && acceleration < threshold) {
        goneOver = false; // reset
      }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // We ignore a changing accuracy for now.
    }
  };
  
  public ShakeDetector(Activity activity) {
    // 4 equals a quick vigorous shake that does not trigger on picking it up.
    //
    this(activity, 4.0f);
  }
  
  public ShakeDetector(Activity activity, float threshold) {
    this.sensorManager       = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    this.acceleration        = 0.0f;
    this.currentAcceleration = SensorManager.GRAVITY_EARTH;
    this.lastAcceleration    = SensorManager.GRAVITY_EARTH;
    
    this.threshold = threshold;
  }
  
  // Override this method to detect shake events.
  //
  public void shaken(float acceleration) {
    
  }
  
  // Start listening to shaking at the beginning
  // of an activity.
  //
  public void start() {
    this.goneOver = false;
    sensorManager.registerListener(
      sensorListener,
      sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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