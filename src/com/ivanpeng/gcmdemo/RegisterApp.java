package com.ivanpeng.gcmdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterApp extends AsyncTask<Void, Void, String> {

	private static final String TAG = "GCMRelated";
	Context ctx;
	GoogleCloudMessaging gcm;
	String SENDER_ID = "343594554298";
	String regid = null; 
	private int appVersion;
	public RegisterApp(Context ctx, GoogleCloudMessaging gcm, int appVersion){
		this.ctx = ctx;
		this.gcm = gcm;
		this.appVersion = appVersion;
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void... arg0) {
		String msg = "";
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(ctx);
			}
			regid = gcm.register(SENDER_ID);
			msg = "Device registered, registration ID=" + regid;

			// You should send the registration ID to your server over HTTP,
			// so it can use GCM/HTTP or CCS to send messages to your app.
			// The request to your server should be authenticated if your app
			// is using accounts.
			sendRegistrationIdToBackend();

			// For this demo: we don't need to send it because the device
			// will send upstream messages to a server that echo back the
			// message using the 'from' address in the message.

			// Persist the regID - no need to register again.
			storeRegistrationId(ctx, regid);
		} catch (IOException ex) {
			msg = "Error :" + ex.getMessage();
			// If there is an error, don't just keep trying to register.
			// Require the user to click a button again, or perform
			// exponential back-off.
		}
		return msg;
	}
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
	private void storeRegistrationId(Context ctx, String regid) {
		final SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("registration_id", regid);
		editor.putInt("appVersion", appVersion);
		editor.commit();

	}


	private void sendRegistrationIdToBackend() {
		String urlStr;
		urlStr = "http://localhost/gcm2/register.php";
		Log.d(TAG, "Generating URL");

		HttpClient httpClient = new DefaultHttpClient();
		BufferedReader br = null;
		try	{
			HttpGet httpget =  new HttpGet(urlStr);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)	{
				InputStream instream = entity.getContent();
				StringBuilder sb= new StringBuilder();
				String line;
				br = new BufferedReader(new InputStreamReader(instream, "utf-8"));
				while((line = br.readLine()) != null)	{
					sb.append(line);
				}
				Log.d(TAG,sb.toString());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally	{
			if (br != null)
				try	{
					br.close();
				} catch (IOException e)	{
					e.printStackTrace();
				}
		}
	}


	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Toast.makeText(ctx, "Registration Completed. Now you can see the notifications", Toast.LENGTH_SHORT).show();
		Log.v(TAG, result);
	}
}