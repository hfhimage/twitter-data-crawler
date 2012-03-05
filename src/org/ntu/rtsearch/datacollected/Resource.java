package org.ntu.rtsearch.datacollected;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ntu.rtsearch.datacollected.api.TwitterApi;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

public class Resource {
	
	private static Token[] tokens = new Token[20];
	
	private static OAuthService service = new ServiceBuilder().provider(
			TwitterApi.class).apiKey("9MHwSJk5Lo5V2726hCZj7A").apiSecret(
			"PPi9wBbhIK1bQJP4TiVftT0ZRiUktcw9a2N3iuWr4").build();
	
	static {
		int i = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(Constants.TOKEN_FILENAME));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line.indexOf("/") != -1 || "".equals(line.trim()))
					continue;
				String[] item = line.split(",");
				tokens[i++] = new Token(item[0].trim(), item[1].trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// 获得access token
	public static Token getToken(int i) {
		if(i >= 20 || i < 0) {
			return null;
		} else {
			return tokens[i];
		}
	}
	
	// 获得OAuthService实例对象
	public static OAuthService getService() {
		return service;
	}
	
	/*
	public static void main(String[] args) {
		int i = 0;
		for(Token t : tokens) {
			System.out.println("" + (i++) + t);
		}
	}
	*/
}
