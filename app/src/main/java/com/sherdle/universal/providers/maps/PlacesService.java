package com.sherdle.universal.providers.maps;

import com.sherdle.universal.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlacesService {

	 private String API_KEY;

	 public PlacesService(String apikey) {
	  this.API_KEY = apikey;
	 }

	 public void setApiKey(String apikey) {
	  this.API_KEY = apikey;
	 }

	 public ArrayList<Place> findPlaces(double latitude, double longitude,
	   String placeSpacification) {

	  String urlString = makeUrl(latitude, longitude, placeSpacification);

	  try {
	   String json = getJSON(urlString);

	   JSONObject object = new JSONObject(json);
	   JSONArray array = object.getJSONArray("results");

	   ArrayList<Place> arrayList = new ArrayList<Place>();
	   for (int i = 0; i < array.length(); i++) {
	    try {
	     Place place = Place
	       .jsonToPontoReferencia((JSONObject) array.get(i));
	     Log.v("Places Services ", "" + place);
	     arrayList.add(place);
	    } catch (Exception e) {
	    }
	   }
	   return arrayList;
	  } catch (JSONException ex) {
	   Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE,
	     null, ex);
	  }
	  return null;
	 }

	 // https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	 private String makeUrl(double latitude, double longitude, String place) {
	  StringBuilder urlString = new StringBuilder(
	    "https://maps.googleapis.com/maps/api/place/search/json?");

	  try {
		place = URLEncoder.encode(place, "UTF-8");
	  } catch (UnsupportedEncodingException e) {
		place = "";
		Log.printStackTrace(e);
	  }
	  
	  if (place.equals("")) {
	   urlString.append("&location=");
	   urlString.append(latitude);
	   urlString.append(",");
	   urlString.append(longitude);
	   urlString.append("&radius=1000");
	   // urlString.append("&types="+place);
	   urlString.append("&sensor=false&key=" + API_KEY);
	  } else {
	   urlString.append("&location=");
	   urlString.append(latitude);
	   urlString.append(",");
	   urlString.append(longitude);
	   urlString.append("&radius=1000");
	   urlString.append("&keyword=" + place);
	   urlString.append("&sensor=false&key=" + API_KEY);
	  }
	  return urlString.toString();
	 }

	 protected String getJSON(String url) {
	  return getUrlContents(url);
	 }

	 private String getUrlContents(String theUrl) {
	  StringBuilder content = new StringBuilder();
	  try {
	   URL url = new URL(theUrl);
	   URLConnection urlConnection = url.openConnection();
	   try (BufferedReader bufferedReader = new BufferedReader(
	     new InputStreamReader(urlConnection.getInputStream()), 8)) {
		   String line;
		   while ((line = bufferedReader.readLine()) != null) {
			   content.append(line + "\n");
		   }
	   }
	  }catch (Exception e) {
	   Log.printStackTrace(e);
	  }
	  return content.toString();
	 }
	}
