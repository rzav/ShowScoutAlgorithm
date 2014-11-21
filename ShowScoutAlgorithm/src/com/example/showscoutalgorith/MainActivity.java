package com.example.showscoutalgorith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
	HashMap<String, ArrayList<ArrayList<String>>> concerts = new HashMap<String, ArrayList<ArrayList<String>>>();

	String spotiUser = [a_spotify_user];
	String authToken = [a_spotify_authentication_token];
	String songkickKey = [a_songkick_apikey];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Btngetdata = (Button) findViewById(R.id.getdata);
		Btngetdata.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				new ConcertDataGet().execute();

			}
		});

	}

	private class ConcertDataGet extends AsyncTask<String, String, String> {
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
				return e.toString();
			}

			jsponse = concerts.toString();

			return jsponse;
		}

		private HttpResponse GetResponse(String getRequest,
				HashMap<String, String> headers) throws Exception {
			try {
				HttpParams httpParameters;
				httpParameters = new BasicHttpParams();
				HttpGet request = new HttpGet(getRequest);

				if (!headers.isEmpty()) {
					for (Map.Entry<String, String> r : headers.entrySet()) {
						request.addHeader(r.getKey(), r.getValue());
					}
				}

				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				return client.execute(request);
			}

			catch (Exception e) {
				throw e;
			}
		}

		private JSONObject GetJSONResponse(String getRequest,
				HashMap<String, String> headers) throws ParseException,
				JSONException, IOException, Exception {

			return new JSONObject(EntityUtils.toString(GetResponse(getRequest,
					headers).getEntity()));
		}

		private void GetPlaylists() throws Exception {
			try {
				String getRequest = "https://api.spotify.com/v1/users/" +
				spotiUser +
				"/playlists";
				HashMap<String, String> headers = new HashMap<String,
				String>();
				headers.put("Accept", "application/json");
				headers.put("Authorization", "Bearer " + authToken);

				JSONObject jObject = GetJSONResponse(getRequest, headers);
				JSONArray items = jObject.getJSONArray(TAG_GET_PLS[0]);

				for (int r = 0; r < items.length(); ++r) {
					playlists.put(
							items.getJSONObject(r).getString(TAG_GET_PLS[1]),
							new ArrayList<String>());
				}
			}

			catch (Exception e) {
				throw e;
			}
		}

		private void GetArtists() throws Exception {
			for (Map.Entry<String, ArrayList<String>> i : playlists.entrySet()) {
				try {
					String getRequest = "https://api.spotify.com/v1/users/"
					+ spotiUser + "/playlists/" + i.getKey()
					+ "/tracks";
					HashMap<String, String> headers = new HashMap<String,
					String>();
					headers.put("Accept", "application/json");
					headers.put("Authorization", "Bearer " + authToken);

					JSONObject jObject = GetJSONResponse(getRequest, headers);
					JSONArray items = jObject.getJSONArray(TAG_ARTISTS[0]);

					for (int r = 0; r < items.length(); ++r) {
						JSONArray artists = items.getJSONObject(r)
								.getJSONObject(TAG_ARTISTS[1])
								.getJSONArray(TAG_ARTISTS[2]);

						for (int s = 0; s < artists.length(); ++s) {
							i.getValue().add(
									artists.getJSONObject(s).getString(
											TAG_ARTISTS[3]));
						}
					}
				}

				catch (Exception e) {
					throw e;
				}
			}
		}

		private HashMap<String, String> GetConcerts() throws Exception {
			HashMap<String, String> artists = getArtistIDs();
			for (Map.Entry<String, String> r : artists.entrySet()) {
				try {
					concerts.put(r.getKey(), getArtistConcerts(r.getValue()));

				}

				catch (Exception e) {
				}
			}
			return artists;
		}

		private HashMap<String, String> getArtistIDs() throws Exception {
			HashMap<String, String> artistIDs = new HashMap<String, String>();

			for (Map.Entry<String, ArrayList<String>> i : playlists.entrySet()) {
				for (String j : i.getValue()) {
					try {
						String getRequest = "http://api.songkick.com/api/3.0/search/artists.json?query="
								+ j.replaceAll("\\s", "%20")
								+ "&apikey="
								+ songkickKey;
						HashMap<String, String> headers = new HashMap<>();

						JSONObject jObject = GetJSONResponse(getRequest,
								headers);
						artistIDs.put(
								j,
								jObject.getJSONObject(TAG_GET_ARSID[0])
										.getJSONObject(TAG_GET_ARSID[1])
										.getJSONArray(TAG_GET_ARSID[2])
										.getJSONObject(0)
										.getString(TAG_GET_ARSID[3]));
					}

					catch (Exception e) {
					}

				}
			}

			return artistIDs;
		}

		private ArrayList<ArrayList<String>> getArtistConcerts(String value)
				throws ParseException, IOException, Exception {
			ArrayList<ArrayList<String>> concertDetails = new ArrayList<>();

			String getRequest = "http://api.songkick.com/api/3.0/artists/"
					+ value + "/calendar.json?apikey=" + songkickKey;
			HashMap<String, String> headers = new HashMap<>();

			JSONObject jObject = GetJSONResponse(getRequest, headers);
			JSONArray events = jObject.getJSONObject(TAG_CONCERT[0])
					.getJSONObject(TAG_CONCERT[1]).getJSONArray(TAG_CONCERT[2]);
			for (int r = 0; r < events.length(); ++r) {
				JSONObject event = events.getJSONObject(r);

				ArrayList<String> concert = new ArrayList<String>();
				concert.add(event.getJSONObject(TAG_DETAILS[1]).getString(
						TAG_DETAILS[2]));
				concert.add(event.getJSONObject(TAG_DETAILS[3]).getString(
						TAG_DETAILS[4]));
				concert.add(event.getJSONObject(TAG_DETAILS[3]).getString(
						TAG_DETAILS[5]));
				concert.add(event.getString(TAG_DETAILS[6]));

				concertDetails.add(concert);
			}
			return concertDetails;
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
