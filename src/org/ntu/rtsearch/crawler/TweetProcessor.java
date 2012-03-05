package org.ntu.rtsearch.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ntu.rtsearch.datacollected.Constants;

public class TweetProcessor {
	
	public static void main(String[] args) throws IOException, JSONException, ParseException {
		BufferedReader br = new BufferedReader(new FileReader(Constants.BASE_PATH + "tweet_t1"));
		String line = null;
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 1, 8);
		Date aWeekAgo = cal.getTime();
		List<String> tIdList = new ArrayList<String>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		while((line = br.readLine()) != null) {
			JSONArray tweets = new JSONArray(line);
			for (int i = 0; i < tweets.length(); i++) {
				JSONObject obj = tweets.getJSONObject(i);
				Date date = sdf.parse(obj.getString("created_at"));
				if(date.after(aWeekAgo)) {
					tIdList.add(obj.getString("id_str"));
				}
				if(obj.getString("id_str").equals("167145335628644353")) {
					System.out.println(date);
				}
			}
		}
		
		// sort
		Collections.sort(tIdList);
		for (int i = 0; i < 10; i++) {
			System.out.println(tIdList.get(i));
		}
	}
}
