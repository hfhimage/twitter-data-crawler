package org.ntu.rtsearch.datacollected;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.JSONException;
import org.json.JSONObject;
import org.ntu.rtsearch.utils.CommonLogger;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class TweetCollected {

	private OAuthService service = Resource.getService();

	HashMap<String, String> map = new HashMap<String, String>();

	/**
	 * collect tweet data
	 * 
	 * @throws Exception
	 */
	private void collectTweets(int tokenIdx, int tokenOffset, int tweetFileIdx,
			long skip, String filePrefix, String endId) throws Exception {
		BufferedReader userIdReader = new BufferedReader(new FileReader(
				Constants.USER_ID_FILE_PATH));
		userIdReader.skip(skip);
		// set the writer's buffer to 2MB to improve performance
		BufferedWriter tweetWriter = new BufferedWriter(new FileWriter(
				Constants.TWEET_FILE_PATH + filePrefix + tweetFileIdx++, true),
				2097152);

		String userId = null;
		int cnt = 0, total = 0;
		Token token = Resource.getToken(tokenIdx);

		long start = System.currentTimeMillis();
		// traversing the social network
		String curThreadName = Thread.currentThread().getName();
		Response resp = null;
		try {
			while ((userId = userIdReader.readLine()) != null
					&& !endId.equals(userId)) {
				OAuthRequest req = new OAuthRequest(Verb.GET,
						Constants.USER_TIMELINE_URL + userId);
				service.signRequest(token, req);

				try {
					resp = req.send();
					if (resp.getCode() == 200) {
						tweetWriter.write(resp.getBody() + "\n");
						CommonLogger.logger.info(curThreadName
								+ " get tweet of user: " + userId);
					} else if (resp.getCode() != 400) {
						CommonLogger.logger
								.info("error code:" + resp.getCode());
						CommonLogger.logger.debug("tweets for user id: "
								+ userId + " response error");
					}
				} catch (Exception e) {
					CommonLogger.logger.info(e);
				}

				// limited, change access token
				if (++cnt >= 350 || resp.getCode() == 400) {
					cnt = 0;
					tokenIdx = (tokenIdx + 1) % 10;
					token = Resource.getToken(tokenIdx + tokenOffset);
					CommonLogger.logger.info("change access token, token: "
							+ tokenIdx + ", response code: " + resp.getCode());
					long end = System.currentTimeMillis();
					CommonLogger.logger.info(end - start);
					start = System.currentTimeMillis();
				}

				// divide the tweets into 50 files
				if (++total >= 10000) {
					total = 0;
					tweetWriter.flush();
					tweetWriter.close();

					// write to a new file
					tweetWriter = new BufferedWriter(new FileWriter(
							Constants.TWEET_FILE_PATH + filePrefix
									+ tweetFileIdx++), 2097152);
					CommonLogger.logger.info("write to a new file: tweet"
							+ (tweetFileIdx - 1));
				}
				// 7000 times per hour
				// try {
				// TimeUnit.MILLISECONDS.sleep(1);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }

			}

		} catch (Exception e) {
			SimpleEmail email = new SimpleEmail();
			email.setHostName("smtp.gmail.com");
			email.setAuthentication("hfhimage@gmail.com", "lovehui1314");
			email.setSSL(true);
			email.setSslSmtpPort("465");
			email.setFrom("137045001@qq.com");
			email.addTo("hfhimage@gmail.com");
			email.setSubject("data collection error");
			email.setMsg(e.toString());
			email.send();
			throw e;
		} finally {
			// release resourse
			userIdReader.close();
			tweetWriter.flush();
			tweetWriter.close();
		}
	}

	// show the limit message
	public void showLimitStatus() throws JSONException {
		OAuthRequest req4limit = new OAuthRequest(Verb.GET,
				Constants.LIMIT_STATUS_URL);
		service.signRequest(Resource.getToken(0), req4limit);
		Response resp = req4limit.send();

		if (resp.getCode() == 200) {
			JSONObject obj = new JSONObject(resp.getBody());
			String seconds = obj.getString("reset_time_in_seconds");
			String remain = obj.getString("remaining_hits");

			map.put(seconds, remain);

			for (Map.Entry<String, String> entry : map.entrySet()) {
				System.out.println(entry.getKey() + " : " + entry.getValue());
			}

			System.out.println("-------------------------");
		} else if (resp.getCode() == 400) {
			System.out.println("limited");
		}

	}

	public static void main(String[] args) throws EmailException {

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				TweetCollected col = new TweetCollected();
				CommonLogger.logger.info("start");
				try {
					// col.showLimitStatus();
					col.collectTweets(0, 0, 28, 4592220, "\\thread1\\tweetA",
							"16852611");
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		Thread t2 = new Thread(new Runnable() {
			public void run() {
				TweetCollected col = new TweetCollected();
				CommonLogger.logger.info("start2");
				try {
					// col.showLimitStatus();
					// begin from 30wth userId
					col.collectTweets(10, 10, 24, 4504124, "\\thread2\\tweetB",
							"306289325");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		t1.start();
		t2.start();

	}
}
