package com.yulinghb.watermonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * 位置信息服务
 */
public class LocationService {

	public static final String TAG = "LocationService";
	public static final int LOCATION_COMMAND = 0;
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int GOOD_COUNT = 3;
	private Location currentLocation;
	private int mCount = 0;
	LocationManager locationManager;
	private boolean isStart = false;
	private GuardThread guarder;
	// Define a listener that responds to location updates
	LocationListener gpsListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			if (null == currentLocation){
				currentLocation = location;
				mCount ++;
			}else{
				if (isBetterLocation(location, currentLocation)){
					currentLocation = location;
					mCount ++;
				}
			}
			
			if (GOOD_COUNT < mCount){
				isStart = false;
				locationManager.removeUpdates(gpsListener);
				client.sendLocation(currentLocation);
			}
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	};
	
	LocationListener networkListener = new LocationListener(){

		public void onLocationChanged(Location location) {
			currentLocation = location;
			
			locationManager.removeUpdates(networkListener);
			client.sendLocation(currentLocation);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
	};
	
	LocationClient client;
	
	public LocationService(DataServer server){
		client = new LocationClient();
		client.bindServer(server);
	}
	
	public boolean isStart(){
		return isStart;
	}
	
	public void setActivity(Activity mActivity){
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
		if ((!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
				|| (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){
			 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			 mActivity.startActivity(intent);
		 }
	}
	
	public void startLocation(){
		 if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
			 Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			 if (null != location){
				 currentLocation = location;
				 client.sendLocation(currentLocation);
			 }
			 
		 }
		 
		 if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
			 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			 if (null != location){
				 currentLocation = location;
				 client.sendLocation(currentLocation);
			 }
		 }
		
		 isStart = true;
		 
		 if (null != guarder){
			 guarder = null;
		 }
		 guarder = new GuardThread();
		 guarder.start();
	}
	
	public void stopLocation(){
		isStart = false;
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationManager.removeUpdates(networkListener);
			 
		 }
		 
		 if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			 locationManager.removeUpdates(gpsListener);
		 }
		 
		 client.sendExpired();
	}

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	class LocationClient extends DataClient implements WaterMonitorClientContract{
		LocationClient(){
			super(TYPE_LOCATION_INFO, ORITATION_OUT);
		}

		@Override
		protected void doJob(Message msg) {
			if (isStart){
				client.sendLocation(currentLocation);
			}else{
				startLocation();
			}
			
		}
		
		void sendLocation(Location location){
			send(TYPE_LOCATION_INFO, ORITATION_IN, 0, location);
		}

		public void sendExpired() {
			send(TYPE_LOCATION_INFO, ORITATION_IN, -1);
		}
	}
	
	class GuardThread extends Thread{

		public void run() {
			try {
				sleep(60000);
				stopLocation();
			} catch (InterruptedException e) {
				Log.e(TAG, "guard thread sleep", e);
			}
		}
		
	}
}
