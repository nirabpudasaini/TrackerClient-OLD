package org.kll.bigbrother;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class AuthenticateUser extends AsyncTask<String, Integer, String> {

	private Activity activity;
	private ProgressDialog dialog;
	private AsyncTaskCompleteListener callback;
	private String userid;

	public AuthenticateUser(Activity a) {
		this.activity = a;
		this.callback = (AsyncTaskCompleteListener) a;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		dialog = new ProgressDialog(activity);
		dialog.setMessage("Verifying Username and Password...");
		dialog.show();
	}

	@Override
	protected String doInBackground(String... params) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(
				"http://www.kathmandulivinglabs.org/tracker/id_response.php");

		// get username and password from sharedprefs
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this.activity);
		String username = sharedPrefs.getString("prefusername", "");
		String password = sharedPrefs.getString("prefpassword", "");

		// Building post parameters, key and value pair
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("name", username));
		nameValuePair.add(new BasicNameValuePair("password", password));

		// Url Encoding the POST parameters
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		} catch (UnsupportedEncodingException e) {
			// writing error to Log
			e.printStackTrace();
		}

		// Making HTTP Request
		try {
			HttpResponse response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				System.out.println("Entity:" + entity);
				if (entity != null) {
					String responseBody = EntityUtils.toString(entity);
					userid = responseBody.toString();
				}
			}

		} catch (ClientProtocolException e) {
			// writing exception to log
			e.printStackTrace();

		} catch (IOException e) {
			// writing exception to log
			e.printStackTrace();
		}

		return userid;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (null != dialog && dialog.isShowing()) {
			dialog.dismiss();
		}
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this.activity);
		sharedPrefs.edit().putString("prefusrid", userid).commit();
		callback.onTaskComplete(result);
	}

}
