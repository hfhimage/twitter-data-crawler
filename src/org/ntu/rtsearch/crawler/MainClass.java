package org.ntu.rtsearch.crawler;

import java.io.IOException;

import org.ntu.rtsearch.utils.CommonLogger;

public class MainClass {
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Usage: java -jar twitter-data-crawler.jar [-option] [outFilePath]");
			return;
		}
		
		String opt = args[0];
		if(opt.equals("userInfo")) {
			int userIdSize = Integer.parseInt(args[1]);
			getUserInfo(userIdSize);
		} else if(opt.equals("topic")) {
			String outFileName = args[1];
			getTopic(outFileName);
		} else if(opt.equals("tweet")) {
			int userIdSize = Integer.parseInt(args[1]);
			int startIdx1 = Integer.parseInt(args[2]);
			int startIdx2 = Integer.parseInt(args[3]); 
			getTweet(userIdSize, startIdx1, startIdx2);
		} else if(opt.equals("userId")) {
			getUserId();
		} else {
			System.out.println("Usage: java -jar twitter-data-crawler.jar [-option] [outFilePath]");
			return;
		}
	}

	/**
	 * 获得参与热门话题的user的 id 
	 */
	private static void getUserId() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				new TopicCrawler().collectUserId(0, 0, 134);
			}
		}).start();
	}

	/**
	 * 根据userId，多线程抓取最近的50条tweet
	 */
	private static void getTweet(final int size, final int startIdx1, final int startIdx2) {
		new Thread(new Runnable() {
			public void run() {
				try {
					new TweetCrawler().collectTweets(0, "_t1", startIdx1, size / 2);
				} catch (IOException e) {
					CommonLogger.logger.info("collect tweet error");
					e.printStackTrace();
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run() {
				try {
					new TweetCrawler().collectTweets(10, "_t2", startIdx2, size);
				} catch (IOException e) {
					CommonLogger.logger.info("collect tweet error");
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 抓取最近一周的热门话题
	 * @param outFile	热门话题保存路径
	 */
	private static void getTopic(String outFile) {
		TopicCrawler tc = new TopicCrawler();
		tc.getHotTopics(outFile);
	}

	/**
	 * 多线程获取User info
	 */
	private static void getUserInfo(final int size) {
		// thread-1
		new Thread(new Runnable() {
			public void run() {
				try {
					UserInfoCrawler uic = new UserInfoCrawler();
					uic.getUserInfo(0, "_t1", 0, size / 2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		// thread-2
		new Thread(new Runnable() {
			public void run() {
				try {
					UserInfoCrawler uic = new UserInfoCrawler();
					uic.getUserInfo(0, "_t2", size / 2, size);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
