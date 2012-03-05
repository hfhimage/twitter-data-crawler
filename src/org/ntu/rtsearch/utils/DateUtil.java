package org.ntu.rtsearch.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DateUtil {
	
	private static Map<String, SimpleDateFormat> map = new HashMap<String, SimpleDateFormat>();
	
	public static int getTime(String str) {
		return getTime(str, "EEE MMM dd HH:mm:ss z yyyy");
	}
	
	public static int getTime(String str, String patern) {
		if(str == null || "".equals(str.trim()))
			return 0;
		
		SimpleDateFormat sdf = map.get(patern);
		if(sdf == null) {
			sdf = new SimpleDateFormat(patern, Locale.ENGLISH);
			map.put(patern, sdf);
		}
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			synchronized (sdf) {
				return (int) (sdf.parse(str).getTime() / 1000);
			}
		} catch (ParseException pe) {
			return 0;
		}
	}
		
}	
