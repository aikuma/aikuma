/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui.sensors;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * A simple location detector using GPS and NETWORK
 *
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class LocationDetector implements LocationListener {
	/**
	* Constants used in the class
	* MIN_TIME_INTERVAL, MIN_DISTANCE_INTERVAL 
	* for location updates
	*/
	public static final double FALSE_LOCATION = -1000;
	private final long MIN_TIME_INTERVAL = 1000 * 60;
	private final long MIN_DISTANCE_INTERVAL = 10;
	/**
	 * locationMagaer which provides location data
	 */
	protected LocationManager locationManager;
	/**
	 * Current best provider
	 */
	protected String provider;
	/**
	 * True if GPS is enabled
	 */
	protected boolean isGPS;
	/**
	 * True if Network is enabled
	 */
	protected boolean isNetwork;
	/**
	 * True if new location data is available
	 */
	protected boolean isLocation;
	/**
	 * Location data (latitude, longitude)
	 */
	protected Location bestLocation;
	
	/**
	 * Constructor for the class
	 * @param context context of the locationManager
	 */
	public LocationDetector(Context context) {
		this.locationManager = (LocationManager) 
				context.getSystemService(Context.LOCATION_SERVICE);
		this.isGPS = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		this.isNetwork = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		this.isLocation = false;
		
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		this.provider = locationManager
				.getBestProvider(c, true);
	}
	
	/**
	 * Start the locationListener
	 * 
	 * @return	boolean value indicating the availability of provider
	 */
	public boolean start() {
		if(provider != null) {
			locationManager.requestLocationUpdates(
					provider, 
					MIN_TIME_INTERVAL,
					MIN_DISTANCE_INTERVAL,
					this);
			return true;
		}
		return false;
	}
	
	/**
	 * Stop the locatioinListener
	 */
	public void stop() {
		locationManager.removeUpdates(this);
	}
	/**
	 * 
	 * @return the latitude
	 */
	public Double getLatitude() { 
		if(isLocation) return bestLocation.getLatitude();
		else if(provider != null){
			bestLocation = locationManager.getLastKnownLocation(provider);
			if(bestLocation != null) return bestLocation.getLatitude();
			else return null;
		} else {
			return null;
		}
	}
	/**
	 * 
	 * @return the longitude
	 */
	public Double getLongitude() {
		if(isLocation) return bestLocation.getLongitude();
		else if(provider != null){
			bestLocation = locationManager.getLastKnownLocation(provider);
			if(bestLocation != null) return bestLocation.getLongitude();
			else return null;
		} else {
			return null;
		}
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (null == bestLocation
				|| location.getAccuracy() < bestLocation.getAccuracy()) {

			// Update best estimate
			bestLocation = location;
			if(!isLocation) {
				isLocation = true;
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	
}
