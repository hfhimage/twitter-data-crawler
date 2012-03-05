package org.ntu.rtsearch.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ntu.rtsearch.datacollected.Constants;
import org.ntu.rtsearch.model.Tweet;
import org.ntu.rtsearch.utils.CommonLogger;

public class TweetProcess {
	
	public static void main(String[] args) throws IOException, JSONException {
		processTweetFiles();
		//verify();
	}
	
	public static void verify() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Constants.BASE_PATH + "\\tweet\\" + "tweet0.processed"));
		String line = null;
		int time = 0;
		while((line = reader.readLine()) != null) {
			String[] strs = line.split("\\:\\^");
			if(strs.length != 5)
				continue;
			int tmp = Integer.parseInt(strs[0]);
			if(time > tmp) {
				System.out.println("error");
				System.out.println(time);
			} else {
				time = tmp;
			}
		}
	}

	/**
	 * process the tweets, order by timestamp and only english tweet save
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void processTweetFiles() throws FileNotFoundException,
			IOException, JSONException {
		BufferedReader br = null;
		File dir = 	new File("D:/wulingkun/rtsearch_data");
		JSONArray jsonArr;
		JSONObject obj = null;
		for(String fileName : dir.list()) {
			if(fileName.startsWith("tweet") && !fileName.endsWith(".processed")) {
				File file = new File(dir, fileName);
				if(file.isDirectory()) 
					continue;
				CommonLogger.logger.info("processing " + fileName);
				
				br = new BufferedReader(new FileReader(file));

				// process the raw json format tweets
				List<Tweet> tweets = new ArrayList<Tweet>();
				String line = null;
				int cnt = 0;
				while((line = br.readLine()) != null) {
					try {
						jsonArr = new JSONArray(line);
					} catch (JSONException e) {
						System.out.println("line " + cnt++ + " parse error");
						continue;
					}
					
					for (int i = 0; i < jsonArr.length(); i++) {
						try{
							obj = jsonArr.getJSONObject(i);
						} catch (Exception e) {
							System.out.println("line " + cnt + ", tweet " + i + " parse error");
							continue;
						}
						
						if(isAllEnglish(obj.getString("text"))) {
							tweets.add(new Tweet(obj));
						}
					}
					cnt++;
				}
				br.close();
				
				// quitsort by timestamp
				Collections.sort(tweets, new Comparator<Tweet>() {
					public int compare(Tweet t1, Tweet t2) {
						long diff = t1.gettId() - t2.gettId();
						return (diff > 0) ? 1 : ((diff < 0) ? -1 : 0);
					}
				});
				
				// write out to the file
				String outFileName = fileName + ".processed";
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, outFileName)));
				for(Tweet tweet : tweets) {
					bw.write(+ tweet.getTimeStamp() + ":^" 
							+ tweet.gettId() + ":^" 
							+ tweet.getUserId() + ":^" 
							+ tweet.getText() + ":^"
							+ tweet.getRtId() + ":^"
							+ tweet.getRtCount() + ":^"
							+ tweet.getIsFav() + "\n");
				}
								
				bw.flush();
				bw.close();
			}
			System.gc();
		}
	}

	private static boolean isAllEnglish(String text) {
		char ch;
		for(int i = 0; i < text.length(); i++) {
			ch = text.charAt(i);
			if(ch > 0x7f || ch < 0)
				return false;
		}
		return true;
	}
	
}
