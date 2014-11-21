package com.example.showscoutalgorith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	TextView output;
	Button Btngetdata;

	// JSON Node Names
	private static final String[] TAG_GET_PLS = { "items", "id" };
	private static final String[] TAG_ARTISTS = { "items", "track", "artists",
			"name" };
	private static final String[] TAG_GET_ARSID = { "resultsPage", "results",
			"artist", "id" };
	private static final String[] TAG_CONCERT = { "resultsPage", "results",
			"event" };
	private static final String[] TAG_DETAILS = { "type", "start", "datetime",
			"location", "lat", "lng", "displayName" };

	HashMap<String, ArrayList<String>> playlists = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> concerts = new HashMap<String, ArrayList<String>>();
	HashMap<String, Integer> arts = new HashMap<String, Integer>();

	String spotiUser = "kkhat111";
	String authToken = "BQCnSe9sv4WQWmwmsSeXz1ppJamiI-EAwqDt0Z5WA24pRb3oCzmQCUdKXJe_aOkyjroO_pzq_XYXlkJEZoHCyAeqyrQ7TSj3iK3Xhj46AXJxth4Py_dkL20WdRTZfyV9DjN3QEQPHSxYwVZjcgaC9dkUGPs";
	String songkickKey = "QlQALOTCwC35QozG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Btngetdata = (Button) findViewById(R.id.getdata);
		Btngetdata.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				new SpotifyDataGet().execute();

			}
		});

	}

	private class SpotifyDataGet extends AsyncTask<String, String, String> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			output = (TextView) findViewById(R.id.output);
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Getting Data ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			String jsponse = "";

			try {
				GetPlaylists();
				GetArtists();
				GetConcerts();
			}

			catch (Exception e) {
				e.printStackTrace();
			}

			ArrayList<String> debgg = new ArrayList<String>();
			jsponse = arts.toString();

			return jsponse;
		}

		private void GetPlaylists() throws Exception {
			try {
				HttpParams httpParameters;
				httpParameters = new BasicHttpParams();

				HttpGet request = new HttpGet(
						"https://api.spotify.com/v1/users/" + spotiUser
								+ "/playlists");
				request.addHeader("Accept", "application/json");
				request.addHeader("Authorization", "Bearer " + authToken);

				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				HttpResponse response = client.execute(request);
				JSONObject jObject = new JSONObject(
						EntityUtils.toString(response.getEntity()));
				JSONArray items = jObject.getJSONArray(TAG_GET_PLS[0]);
				for (int r = 0; r < items.length(); ++r) {
					JSONObject item = new JSONObject(items.getString(r));
					playlists.put(item.getString(TAG_GET_PLS[1]),
							new ArrayList<String>());
				}
			}

			catch (Exception e) {
				throw e;
			}
		}

		private void GetArtists() throws Exception {
			try {
				HttpParams httpParameters;
				httpParameters = new BasicHttpParams();

				for (Map.Entry<String, ArrayList<String>> i : playlists
						.entrySet()) {
					HttpGet request = new HttpGet(
							"https://api.spotify.com/v1/users/" + spotiUser
									+ "/playlists/" + i.getKey() + "/tracks");
					request.addHeader("Accept", "application/json");
					request.addHeader("Authorization", "Bearer " + authToken);

					HttpConnectionParams.setSoTimeout(httpParameters, 5000);
					DefaultHttpClient client = new DefaultHttpClient(
							httpParameters);
					HttpResponse response = client.execute(request);
					JSONObject jObject = new JSONObject(
							EntityUtils.toString(response.getEntity()));
					JSONArray items = jObject.getJSONArray(TAG_ARTISTS[0]);
					for (int r = 0; r < items.length(); ++r) {
						JSONObject item = new JSONObject(items.getString(r));
						JSONObject track = new JSONObject(
								item.getString(TAG_ARTISTS[1]));
						JSONArray artists = track.getJSONArray(TAG_ARTISTS[2]);
						for (int s = 0; s < artists.length(); ++s) {
							JSONObject artist = new JSONObject(
									artists.getString(s));
							i.getValue().add(artist.getString(TAG_ARTISTS[3]));
						}
					}
				}
			}

			catch (Exception e) {
				throw e;
			}
		}

		private HashMap<String, Integer> GetConcerts() throws Exception {
			HashMap<String, Integer> artists = getArtistIDs();
			return artists;
		}

		private HashMap<String, Integer> getArtistIDs() throws Exception {
			HashMap<String, Integer> toReturn = new HashMap<String, Integer>();

			for (Map.Entry<String, ArrayList<String>> i : playlists.entrySet()) {
				for (String j : i.getValue()) {
					Toast.makeText(getApplicationContext(), j,
							Toast.LENGTH_SHORT).show();
					try {
						HttpParams httpParameters;
						httpParameters = new BasicHttpParams();
						HttpGet request = new HttpGet(
								"http://http://api.songkick.com/api/3.0/search/artists.json?query="
										+ j + "&apikey=" + songkickKey);
						HttpConnectionParams.setSoTimeout(httpParameters, 5000);
						DefaultHttpClient client = new DefaultHttpClient(
								httpParameters);
						HttpResponse response = client.execute(request);
						JSONObject jObject = new JSONObject(
								EntityUtils.toString(response.getEntity()));
						JSONObject resultsPage = new JSONObject(
								jObject.getString(TAG_GET_ARSID[0]));
						JSONObject results = new JSONObject(
								resultsPage.getString(TAG_GET_ARSID[1]));
						JSONArray artists = results
								.getJSONArray(TAG_GET_ARSID[2]);
						JSONObject artist0 = new JSONObject(
								artists.getString(0));
						toReturn.put(j, artist0.getInt(TAG_GET_ARSID[3]));
					}

					catch (Exception e) {
					}

				}
			}

			return toReturn;
		}

		protected void onPostExecute(String json) {
			pDialog.dismiss();
			try {
				String str = json.toString();
				output.setText(str);
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
