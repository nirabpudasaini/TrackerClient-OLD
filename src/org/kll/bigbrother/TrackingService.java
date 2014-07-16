package org.kll.bigbrother;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class TrackingService extends Service {

	private NotificationManager mNM;
	private SharedPreferences sharedPrefs;
	private LocationManager lm;
	private LocationListener locationListener;
	private Location currentLocation;
	private Boolean tracking;
	private long interval;

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		interval = (long) (Float.valueOf(sharedPrefs.getString("prefinterval", "5")) * 60 * 1000) ;
		tracking = sharedPrefs.getBoolean("preftracking", false);
		sharedPrefs.edit().putBoolean("preftracking", true).commit();

		startTracker();

		showNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		stopTracker();

		mNM.cancel(R.string.tracking_in_progress);
		sharedPrefs.edit().putBoolean("preftracking", false).commit();

		Toast.makeText(this, R.string.tracking_stopped, Toast.LENGTH_SHORT)
				.show();
	}

	@SuppressWarnings("deprecation")
	private void showNotification() {

		CharSequence text = getText(R.string.tracking_in_progress);

		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "Big Brother", text,
				contentIntent);

		mNM.notify(R.string.tracking_in_progress, notification);
	}

	private void startTracker() {

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationListener = new MyLocationListener();

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2,
				locationListener);

		final Handler handler = new Handler();
		Runnable runable = new Runnable() {

			@Override
			public void run() {
				try {
					sendData();

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					tracking = sharedPrefs.getBoolean("preftracking", false);
					interval = (long) (Float.valueOf(sharedPrefs.getString("prefinterval", "5")) * 60 * 1000) ;
					if (tracking) {
						handler.postDelayed(this, interval);
					}
				}
			}
		};
		handler.postDelayed(runable, interval);

	}

	private void stopTracker() {
		lm.removeUpdates(locationListener);
	}

	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

	}

	public boolean isInternetAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean isWifiOn() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean isMobileDataOn() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean sendData() {
		String networkType = sharedPrefs.getString("prefnetwork", "wifi");
		if (networkType.equals("wifi")) {
			if (isWifiOn()) {
				Log.i("WifiOn", "Data will be sent from Wifi");
				startSendAsync();
				return true;
			} else {
				Toast.makeText(getBaseContext(),
						"Make Sure Wifi is on or change your settings",
						Toast.LENGTH_LONG).show();
				return false;

			}
		} else if (networkType.equals("3g")) {
			if (isWifiOn()) {
				startSendAsync();
				Log.i("WifiOn", "Data will be sent from Wifi");
				return true;
			} else if (isMobileDataOn()) {
				startSendAsync();
				Log.i("Mobile On", "Data will be sent from Mobile Data ");
				return true;
			} else {
				Toast.makeText(
						getBaseContext(),
						"Make Sure you have a working internet connection or change your settings",
						Toast.LENGTH_LONG).show();
				return false;

			}
		} else {
			if (isWifiOn()) {
				startSendAsync();
				Log.i("WifiOn", "Data will be sent from Wifi");
				return true;
			} else if (isMobileDataOn()) {
				startSendAsync();
				Log.i("Mobile On", "Data will be sent from Mobile Data ");
				return true;
			} else {
				Log.i("SMS On", "Data will be sent from SMS ");
				if (currentLocation != null) {

					String phoneNumber = sharedPrefs
							.getString("prefnumber", "+9779803193917");
					String id = sharedPrefs.getString("prefusrid", "");
					Log.i("ID", id);
					String name = sharedPrefs.getString("prefusername", "");
					Log.i("NAME", name);
					String latitude = String.valueOf(currentLocation
							.getLatitude());
					Log.i("LATITUDE", latitude);
					String longitude = String.valueOf(currentLocation
							.getLongitude());
					Log.i("LONGITUDE", longitude);
					String accuracy = String.valueOf(currentLocation
							.getAccuracy());
					Log.i("ACCURACY", accuracy);
					String timestamp = getCurrentTimeStamp();
					Log.i("TIMESTAMP", timestamp);
					String message = "trackerapp,data," + id + "," + name + ","
							+ latitude + "," + longitude + "," + accuracy + ","
							+ timestamp;
					sendSMS(phoneNumber, message);

				} else {
					Toast.makeText(getBaseContext(),
							"Location fix not available", Toast.LENGTH_SHORT)
							.show();
				}
				return true;
			}
		}
	}

	private void sendSMS(String phoneNumber, String message) {

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "Traker has sent your location via SMS",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(
							getBaseContext(),
							"Tracker could not send your data vai SMS, make sure you are able to send SMS",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(
							getBaseContext(),
							"Tracker could not send your data vai SMS because no service is available",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(
							getBaseContext(),
							"Tracker could not send your data vai SMS, make sure airplane mode is not enabled",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	public void startSendAsync() {
		// 1. id of the user 'id'
		// 2. name of the user 'name'
		// 3.latitude 'X'
		// 4.longitude 'Y'
		// 5.accuracy of gps 'accuracy'
		// 6.timestamp 'timestamp'
		if (currentLocation != null) {

			String id = sharedPrefs.getString("prefusrid", "");
			Log.i("ID", id);
			String name = sharedPrefs.getString("prefusername", "");
			Log.i("NAME", name);
			String latitude = String.valueOf(currentLocation.getLatitude());
			Log.i("LATITUDE", latitude);
			String longitude = String.valueOf(currentLocation.getLongitude());
			Log.i("LONGITUDE", longitude);
			String accuracy = String.valueOf(currentLocation.getAccuracy());
			Log.i("ACCURACY", accuracy);
			String timestamp = getCurrentTimeStamp();
			Log.i("TIMESTAMP", timestamp);
			new SendData().execute(id, name, latitude, longitude, accuracy,
					timestamp);
		} else {
			Toast.makeText(getBaseContext(), "Location fix not available",
					Toast.LENGTH_SHORT).show();
		}

	}

	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
