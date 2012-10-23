package au.edu.unimelb.boldapp.sensors;

import android.util;

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
  
  private SensorManager sensorManager;
  private float acceleration;        // acceleration relative to gravity
  private float currentAcceleration; // current acceleration including gravity
  private float lastAcceleration;    // last acceleration including gravity
  
  // the threshold which needs to be crossed for the shaken method to be called.
  //
  private float threshold;

  private final SensorEventListener sensorListener = new SensorEventListener() {
    
    // 
    //
    public void onSensorChanged(SensorEvent sensorEvent) {
      float x = sensorEvent.values[0];
      float y = sensorEvent.values[1];
      float z = sensorEvent.values[2];
      lastAcceleration = currentAcceleration;
      currentAcceleration = FloatMath.sqrt(x*x + y*y + z*z);
      float delta = currentAcceleration - lastAcceleration;
      acceleration = acceleration * 0.9f + delta * 0.1f; // Filtering.
      if (acceleration > threshold) {
        shaken(acceleration);
      }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // We ignore a changing accuracy for now.
    }
  };
  
  public void ShakeDetector() {
    this(2.0f);
  }
  
  public void ShakeDetector(float threshold) {
    this.sensorManager       = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    this.acceleration        = 0.0f;
    this.currentAcceleration = SensorManager.GRAVITY_EARTH;
    this.lastAcceleration    = SensorManager.GRAVITY_EARTH;
    this.threshold           = threshold;
  }
  
  // Override this method to detect shake events.
  //
  protected void shaken(float acceleration) {
    
  }
  
  // Start listening to shaking at the beginning
  // of an activity.
  //
  protected void start() {
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
  protected void stop() {
    sensorManager.unregisterListener(sensorListener);
  }
  
}