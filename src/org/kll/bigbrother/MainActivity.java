package org.kll.bigbrother;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements AsyncTaskCompleteListener {

	Button toggleTracking;
	Boolean tracking;
	SharedPreferences sharedPrefs;
	String network;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toggleTracking = (Button) findViewById(R.id.toggle_tracking);

		toggleTracking.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Triggred when the button tracking button is clicked
				if (!tracking) {
					// Start the tracking here
					// check if GPS is on
					LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					if (!locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						buildAlertMessageNoGps();
						return;
					}
					if (isInternetAvailable()) {
						new AuthenticateUser(MainActivity.this).execute();
					} else {
						Toast.makeText(
								getBaseContext(),
								"You do not have a internet connection, You need internet connection for starting the tracking.",
								Toast.LENGTH_LONG).show();
					}

				} else if (tracking) {
					// Stop the tracking here
					toggleTracking.setText(R.string.start);
					toggleTracking.setBackgroundColor(Color.GREEN);
					tracking = false;
					stopService(new Intent(MainActivity.this,
							TrackingService.class));
				}

			}
		});

	}

	// Alert dialog to propmt user to enable gps
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, Please enable it to start tracking")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						startActivity(new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean isInternetAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		network = sharedPrefs.getString("prefnetwork", "wifi");
		tracking = sharedPrefs.getBoolean("preftracking", false);
		if (tracking) {
			// If current status is tracking set the Button to allow stopping
			// the Service
			toggleTracking.setText(R.string.stop);
			toggleTracking.setBackgroundColor(Color.RED);
		} else if (!tracking) {
			// If current status is not tracking set button to allow starting
			// the service
			toggleTracking.setText(R.string.start);
			toggleTracking.setBackgroundColor(Color.GREEN);
		}

		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(getBaseContext(), UserSettings.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTaskComplete(String result) {
		System.out.println("calling....");
		System.out.println("result :: " + result);
		if (!result.equals("0") || !result.equals("")) {
			toggleTracking.setText(R.string.stop);
			toggleTracking.setBackgroundColor(Color.RED);
			tracking = true;
			startService(new Intent(MainActivity.this, TrackingService.class));
			Log.i(network, network);
		} else {
			Toast.makeText(getApplicationContext(),
					"Please provoide a valid username and password",
					Toast.LENGTH_SHORT).show();
		}
	}

}
