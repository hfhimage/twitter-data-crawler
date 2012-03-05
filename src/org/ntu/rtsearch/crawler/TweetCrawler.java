package org.ntu.rtsearch.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.JSONArray;
import org.json.JSONException;
import org.ntu.rtsearch.datacollected.Constants;
import org.ntu.rtsearch.datacollected.Resource;
import org.ntu.rtsearch.utils.CommonLogger;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class TweetCrawler {
	
	private final static OAuthService service = Resource.getService();
	
	private static List<String> userIdList = Collections.synchronizedList(new ArrayList<String>(141791));
	
	static {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Constants.USER_ID_FILE_PATH));
			String line = null;	
			while((line = br.readLine()) != null) {
				userIdList.add(line);
			}
			br.close();
		} catch (Exception e) {
			CommonLogger.logger.error("init userIdList error!");
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据userId抓取其最近的50条tweet
	 * @param offset		不同线程使用的token的offset不同
	 * @param postfix		保存文件的后缀
	 * @param startIdx		开始的userId
	 * @param endIdx		结束的userId
	 * @throws IOException	
	 */
	public void collectTweets(int offset, String postfix, int startIdx, int endIdx) throws IOException {
		int tokenIdx = offset;
		Token token = Resource.getToken(tokenIdx);
		Response resp = null;
		int cnt = 0;
		String curThreadName = Thread.currentThread().getName();
		BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.TWEET_FILE_PATH + postfix, true));
		BufferedWriter debugWriter = new BufferedWriter(new FileWriter(Constants.DEBUG_FILE + postfix, true));
		
		for (int i = startIdx; i < endIdx; i++) {
			CommonLogger.logger.info(curThreadName + ": get tweet for " + userIdList.get(i));
			try {
				OAuthRequest req = new OAuthRequest(Verb.GET, Constants.USER_TIMELINE_URL + userIdList.get(i));
				service.signRequest(token, req);
				resp = req.send();

				if (resp.getCode() == 200) {
					String respText = resp.getBody();
					bw.write(respText + "\n");
				} else if (resp.getCode() != 400) {
					CommonLogger.logger.info(resp.getCode() + ", "
							+ resp.getBody());
					debugWriter.write(i + ", " + userIdList.get(i) + "\n");
					CommonLogger.logger.info("error: " + i + ", "
							+ userIdList.get(i) + "\n");
				}

				// limited, change access token
				if (++cnt >= 350 || resp.getCode() == 400) {
					cnt = 0;
					tokenIdx = (tokenIdx + 1) % 10 + offset;
					token = Resource.getToken(tokenIdx);
					CommonLogger.logger.info("change access token, token: "
							+ tokenIdx + ", response code: " + resp.getCode());
				}
			} catch (Exception e) {
				sendEmail(e);
			}
		}
		bw.flush();
		bw.close();
		debugWriter.flush();
	}

	private void sendEmail(Exception e) {
		SimpleEmail email = new SimpleEmail();
		email.setHostName("smtp.gmail.com");
		email.setAuthentication("hfhimage@gmail.com", "Rider@206.cloud");
		email.setSSL(true);
		email.setSslSmtpPort("465");
		try {
			email.setFrom("137045001@qq.com");
			email.addTo("hfhimage@gmail.com");
			email.setSubject("data collection error");
			email.setMsg(e.toString());
			email.send();
		} catch (EmailException e1) {
			CommonLogger.logger.info("send email error!");
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final int size = 141791;
		final int t1StartIdx = Integer.parseInt(args[0]);
		final int t2StartIdx = Integer.parseInt(args[1]);
		new Thread(new Runnable() {
			public void run() {
				TweetCrawler tc = new TweetCrawler();
				try {
					tc.collectTweets(0, "_t1", t1StartIdx, size / 2);
				} catch (IOException e) {
					CommonLogger.logger.info("collect tweet error");
					e.printStackTrace();
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run() {
				TweetCrawler tc = new TweetCrawler();
				try {
					tc.collectTweets(10, "_t2", t2StartIdx, size);
				} catch (IOException e) {
					CommonLogger.logger.info("collect tweet error");
					e.printStackTrace();
				}
			}
		}).start();
	}

}
