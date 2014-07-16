package org.kll.bigbrother;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements AsyncTaskCompleteListener{
	
	EditText mUsername, mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mUsername = (EditText) findViewById(R.id.editusr);
		mPassword = (EditText) findViewById(R.id.editpass);
		Button btnlogin = (Button) findViewById(R.id.btnlogin);
		
		btnlogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(LoginActivity.this);
				sharedPrefs.edit().putString("prefusername", mUsername.getText().toString()).commit();
				sharedPrefs.edit().putString("prefpassword", mPassword.getText().toString()).commit();
				if (isInternetAvailable()) {
					new AuthenticateUser(LoginActivity.this).execute();
				} else {
					Toast.makeText(
							getBaseContext(),
							"You do not have a internet connection, You need internet connection to Login",
							Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
	}
	
	
	
	@Override
	protected void onPause() {
		finish();
		super.onPause();
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
	public void onTaskComplete(String result) {
		System.out.println("calling....");
		System.out.println("result :: " + result);
		if(result != null){
		if (!result.equals("0") && !result.equals("")) {
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
		} else {
			Toast.makeText(getApplicationContext(),
					"Please provoide a valid username and password",
					Toast.LENGTH_SHORT).show();
		}
		} else {
			Toast.makeText(getApplicationContext(),
					"There might be a problem with your Internet Connection, Please Troubleshoot it",
					Toast.LENGTH_SHORT).show();
		}
		
		
	}
		
	
	

}
