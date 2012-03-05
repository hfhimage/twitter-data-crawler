package org.ntu.rtsearch.datacollected;

public class Constants {
	/*------------------------------------------file path----------------------------------------------*/
	public static final String BASE_PATH = "d:\\rtsearch_data\\new\\";
	
	public static final String SEED_FILE_PATH = BASE_PATH + "seeds";
	
	public static final String USER_ID_FILE_PATH = BASE_PATH + "user_id";
	
	public static final String RELATION_SHIP_FILE_PATH = BASE_PATH + "realtionship";
	
	public static final String USER_INFO_FILE_PATH = BASE_PATH + "user_info";
	
	public static final String TWEET_FILE_PATH = BASE_PATH + "tweet";
	
	public static final String USER_ID_SEEDS_FILE_PATH = BASE_PATH + "user_id_seeds";
	
	public static final String TOKEN_FILENAME = BASE_PATH + "access_token";
	
	public static final String POPULAR_TOPICS = BASE_PATH + "popular_topics";
	
	public static final String DEBUG_FILE = BASE_PATH + "userId_debug";
	
	public static final String ERROR_WRITER = BASE_PATH + "userInfo_debug";

	public static final String USERID_OMITTED = BASE_PATH + "userId_omitted";;
	
	/*------------------------------------API---------------------------------------------*/
	// recent 50 tweet, no with the user info
	public static final String USER_TIMELINE_URL = "http://api.twitter.com/1/statuses/user_timeline.json?count=50&trim_user=t&user_id=";
	
	public static final String USER_TIMELINE_URL_WEEKLY = "http://api.twitter.com/1/statuses/user_timeline.json?trim_user=t" +
			"&since_id=167145335628644353&user_id=";
	

	public static final String LIMIT_STATUS_URL = "http://api.twitter.com/1/account/rate_limit_status.json";

	public static final String GET_FRIENDSID_URL = "http://api.twitter.com/1/friends/ids.json?cursor=-1&user_id=";

	public static final String USER_INFO_URL = "http://api.twitter.com/1/users/show.json?skip_status=1&user_id=";
	
	public static final String USER_INFO_URL2 = "http://api.twitter.com/1/users/show.json?skip_status=1&screen_name=";
	
	public static final String TRENDS_DAILY = "https://api.twitter.com/1/trends/daily.json?exclude=hashtags&";
	
	public static final String SEARCH_API = "http://search.twitter.com/search.json?rpp=100&q=";

	

	

	

	


	
}
