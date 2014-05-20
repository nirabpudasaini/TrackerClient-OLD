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

import android.os.AsyncTask;
import android.util.Log;

public class SendData extends AsyncTask<String, Integer, Boolean> {

	@Override
	protected Boolean doInBackground(String... params) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(
				"http://www.kathmandulivinglabs.org/tracker/insertion_from_mob.php");

		// get data to make post
		String id = params[0];
		String name = params[1];
		String latitude = params[2];
		String longitude = params[3];
		String accuracy = params[4];
		String timestamp = params[5];

		// Building post parameters, key and value pair
		// for name
		// 1. id of the user 'id'
		// 2. name of the user 'name'
		// 3.latitude 'X'
		// 4.longitude 'Y'
		// 5.accuracy of gps 'accuracy'
		// 6.timestamp 'timestamp'

		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("id", id));
		nameValuePair.add(new BasicNameValuePair("name", name));
		nameValuePair.add(new BasicNameValuePair("X", latitude));
		nameValuePair.add(new BasicNameValuePair("Y", longitude));
		nameValuePair.add(new BasicNameValuePair("accuracy", accuracy));
		nameValuePair.add(new BasicNameValuePair("timestamp", timestamp));

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
					String responsemessage = responseBody.toString();
					Log.i("DATA RESPONSE", responsemessage);
				}

			}

		} catch (ClientProtocolException e) {
			// writing exception to log
			e.printStackTrace();

		} catch (IOException e) {
			// writing exception to log
			e.printStackTrace();
		}

		return false;
	}

}
