package org.ntu.rtsearch.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ntu.rtsearch.datacollected.Constants;
import org.ntu.rtsearch.datacollected.Resource;
import org.ntu.rtsearch.utils.CommonLogger;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class TopicCrawler {
	
	private static final int MAX_PAGE = 15; 	// 经试验，超过15页之后的返回的内容重复
	
	private static final int START_DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	private final static OAuthService service = Resource.getService();
	
	private List<String> topicList = new ArrayList<String>();
	
	private HashSet<String> topicSet = new HashSet<String>();
	
	private static Set<String> userIdSet = Collections.synchronizedSet(new HashSet<String>());
	
	private static int reqCount = 0;
	
	public void getHotTopics(String outFile) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 7; i++) {
			cal.set(2012, 1, START_DAY - i);		// 今天前一周
			String date = sdf.format(cal.getTime());
			System.out.println(date);
			getTopicByDay(date);
		}
		try {
			// save to file
			BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.BASE_PATH + outFile));
			for (String topic : topicSet) {
				bw.write(topic + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(topicSet.size());
	}
	
	private void getTopicByDay(String date) {
		Token token = Resource.getToken(0);
		OAuthRequest req = new OAuthRequest(Verb.GET, Constants.TRENDS_DAILY + "date=" + date);
		service.signRequest(token, req);
		Response resp = null;
		
		try {
			resp = req.send();
			if (resp.getCode() == 200) {
				String respText = resp.getBody();
				JSONObject obj = new JSONObject(respText).getJSONObject("trends");			
				Iterator<String> it = obj.keys();
				JSONArray trends = obj.getJSONArray(it.next());
				handleTrends(trends);
			} else if (resp.getCode() != 400) {
				CommonLogger.logger.info("error code:" + resp.getCode());
			}
		} catch (Exception e) {
			CommonLogger.logger.info(e);
		}
	}

	private void handleTrends(JSONArray trends) {
		try {
			for (int i = 0; i < trends.length(); i++) {
				JSONObject obj = trends.getJSONObject(i);
				String topic = obj.getString("name");
				topicSet.add(topic);
				System.out.println(topic);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * read keywords from file
	 */
	public void initKeywords() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Constants.POPULAR_TOPICS));
			String line = null;
			while((line = br.readLine()) != null) {
				topicList.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 * collecting the ids of the users involved in the popular topics of this week 
	 */
	public void collectUserId(int tokenIdx, int startIdx, int endIdx) {
		initKeywords();		// read keywords from file
		Token token = Resource.getToken(tokenIdx);
		Response resp = null;
		
		for (int ti = startIdx; ti < endIdx; ti++) {		// toplist.size() == 140
			String topic = topicList.get(ti);
			CommonLogger.logger.info("----------------search keyword: " + topic + "-------------------");
			try {
				String encTopic = URLEncoder.encode(topic, "utf8");
				
				for (int i = 1; i <= MAX_PAGE; i++) { 		
					OAuthRequest req = new OAuthRequest(Verb.GET, Constants.SEARCH_API + encTopic + "&page=" + i);
					service.signRequest(token, req);
					resp = req.send();
					reqCount++;
					if (resp.getCode() == 200) {
						//CommonLogger.logger.info(resp.getBody());
						handleSearchResult(resp.getBody());
						CommonLogger.logger.info("keyword " + ti + ", page " + i + ": " + userIdSet.size());
					} else if(resp.getCode() == 420) {
						token = Resource.getToken(++tokenIdx);
						CommonLogger.logger.info("------------------limitted, change token-----------------");
						CommonLogger.logger.info(reqCount);
						CommonLogger.logger.info(resp.getBody());
					} else if (resp.getCode() != 400) {
						CommonLogger.logger.info("error code:" + resp.getCode());
						CommonLogger.logger.info(resp.getBody());
						CommonLogger.logger.info("keyword=" + topic + ", page=" + i);
					} 
				}
			} catch (Exception e) {
				CommonLogger.logger.info(e);
				CommonLogger.logger.info("keyword=" + topic);
			}
			CommonLogger.logger.info("# of userId: " + userIdSet.size());
		}
		saveUserIds();
	}
	
	private void handleSearchResult(String text) {
		try {
			JSONObject obj = new JSONObject(text);
			
			JSONArray results = obj.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				String userId = result.getString("from_user_id");
				userIdSet.add(userId);
				//CommonLogger.logger.info("userId:" + userId);
			}
		} catch (Exception e) {	}
	}
	
	private void saveUserIds() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.USER_ID_FILE_PATH));
			for (String userId : userIdSet) {
				bw.write(userId + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				new TopicCrawler().collectUserId(0, 0, 134);
			}
		}).start();

	}

}
