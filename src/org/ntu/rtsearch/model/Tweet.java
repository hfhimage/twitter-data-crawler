package org.ntu.rtsearch.model;

import org.json.JSONObject;
import org.ntu.rtsearch.utils.DateUtil;

public class Tweet {
	private long tId;
	private String userId;
	private String text;
	private int timeStamp;
	private long rtId;
	private String rtCount;
	private int isFav;

	public Tweet(JSONObject obj) {
		try {
			tId = obj.getLong("id");
			userId = obj.getJSONObject("user").getString("id_str");
			text = obj.getString("text").replace("\n", "\\n");
			timeStamp = DateUtil.getTime(obj.getString("created_at"));
			String str = obj.getString("in_reply_to_status_id");
			if(!"null".equals(str)) {
				rtId = Long.parseLong(str);
			}
			setRtCount(obj.getString("retweet_count"));
			setIsFav(obj.getBoolean("favorited") ? 1 : 0);
		} catch (Exception e) {
			System.out.println("parse json to tweet error");
		}		
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	

	public void setIsFav(int isFav) {
		this.isFav = isFav;
	}

	public int getIsFav() {
		return isFav;
	}

	public void setRtCount(String rtCount) {
		this.rtCount = rtCount;
	}

	public String getRtCount() {
		return rtCount;
	}

	public void settId(long tId) {
		this.tId = tId;
	}

	public long gettId() {
		return tId;
	}

	public void setRtId(long rtId) {
		this.rtId = rtId;
	}

	public long getRtId() {
		return rtId;
	}

}
