/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui.sensors;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.util.FloatMath;

/**
 * A simple proximity detector.
 *
 * Override the near method to react to shaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ProximityDetector {

	/**
	 * The sensor manager that provides the readings.
	 */
	protected SensorManager sensorManager;

	/**
	 * The threshold distance in centimeters that determines whether a users
	 * face is near or far from the phone.
	 */
	protected float threshold;
	/**
	 * True if users face is within the threshold; false otherwise.
	 */
	protected boolean close;

	/**
	 * The listener that provides the functionality when sensor readings change.
	 */
	protected final SensorEventListener sensorListener =
			new SensorEventListener() {

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

	/**
	 * Constructor that defaults to a threshold distance of 2 centimeters.
	 *
	 * @param	activity	The activity the proximity sensor belongs to.
	 */
	public ProximityDetector(Activity activity) {
		this(activity, 2.0f);
	}

	/**
	 * Constructor that allows for specification of the threshold.
	 *
	 * @param	activity	The activity the proximity sensor belongs to.
	 * @param	threshold	The distance in centimeters that is the threshold
	 * that determines whether the user's face is near or far.
	 */
	public ProximityDetector(Activity activity, float threshold) {
		this.sensorManager = (SensorManager)
				activity.getSystemService(Context.SENSOR_SERVICE);
		this.threshold = threshold;
	}

	/**
	 * Returns true if the users face is close to the phone.
	 *
	 * @return	true if the user is close; false otherwise.
	 */
	public boolean isNear() {
		return close;
	}

	/**
	 * Override this method to describe what should happen when the users face
	 * becomes close.
	 *
	 * @param	distance	The distance in centimeters between the users face
	 * and the proximity detector.
	 */
	public void near(float distance) {};

	/**
	 * Override this method to describe what should happen when the users face
	 * becomes far.
	 *
	 * @param	distance	The distance in centimeters between the users face
	 * and the proximity detector.
	 */
	public void far(float distance) {};

	/**
	 * Start the proximity detection.
	 */
	public void start() {
		this.close = false;
		sensorManager.registerListener(
				sensorListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
				SensorManager.SENSOR_DELAY_NORMAL
		);
	}

	/**
	 * Stop the proximity detection; do not forget to call this.
	 */
	public void stop() {
		sensorManager.unregisterListener(sensorListener);
	}

}
