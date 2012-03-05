package org.ntu.rtsearch.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.ntu.rtsearch.datacollected.Constants;
import org.ntu.rtsearch.datacollected.Resource;
import org.ntu.rtsearch.utils.CommonLogger;
import org.ntu.rtsearch.utils.EmailUtil;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class UserInfoCrawler {
	
	private static final int BUFFER_SIZE = 2097152;
	
	private OAuthService service = Resource.getService();
	
	private static List<String> userIdList = new ArrayList<String>();

	static {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Constants.USER_ID_FILE_PATH));
			String line = null;
			while((line = br.readLine()) != null) {
				userIdList.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * 找出抓取失败的userId
	 */
	public void findOmittedUserId() {
		try {
			Set<String> userIdSet = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(Constants.USER_INFO_FILE_PATH), BUFFER_SIZE);
			String line = null;
			while((line = br.readLine()) != null) {
				JSONObject obj = new JSONObject(line);
				userIdSet.add(obj.getString("id"));
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.USERID_OMITTED), BUFFER_SIZE);
			for(String userId : userIdList) {
				if(!userIdSet.contains(userId)) 
					bw.write(userId);
			}
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据userId抓取用户信息
	 * @param offset	token的offset
	 * @param postfix	不同线程保存的user_info文件的后缀
	 * @param startIdx	开始抓取的userId 的index
	 * @param endIdx	最后一个抓取的userId 的index
	 * @throws Exception
	 */
	public void getUserInfo(int offset, String postfix, int startIdx, int endIdx) {
		CommonLogger.logger.info("--------begin users' information collection--------");
		OAuthRequest req = null;
		
		BufferedWriter userInfoWriter = null, errorWriter = null;
		String tName = Thread.currentThread().getName();
		try {
			userInfoWriter = new BufferedWriter(new FileWriter(Constants.USER_INFO_FILE_PATH, true), 2097152);
			errorWriter = new BufferedWriter(new FileWriter(Constants.ERROR_WRITER, true));
		
			Token token = Resource.getToken(0);
			String userId = null;
			int cnt = 0, tokenIdx = 0;
			
			for(int i = startIdx; i < endIdx; i++) {
				userId = userIdList.get(i);
				req = new OAuthRequest(Verb.GET, Constants.USER_INFO_URL + userId);
				service.signRequest(token, req);
				Response resp = req.send();
				
				if(resp.getCode() == 200) {
					CommonLogger.logger.info(tName + " get info for useId: " + userId);
					userInfoWriter.write(resp.getBody() + "\n");
				} else if(resp.getCode() != 400) {
					CommonLogger.logger.info("error code:" + resp.getCode());
					CommonLogger.logger.info("error msg:" + resp.getBody());
					errorWriter.write(userId + "\n");
				}
				
				// limited, change access token
				if(++cnt > 350 || resp.getCode() == 400) {
					cnt = 0;
					tokenIdx = (++tokenIdx) % 10 + offset;
					token = Resource.getToken(tokenIdx);
					CommonLogger.logger.info("change access token: token " + tokenIdx);
				}
			}
		} catch (Exception e) {
			EmailUtil.sendEmail(e);
		} finally {
			try {
				if(userInfoWriter != null) {
					userInfoWriter.flush();
					userInfoWriter.close();
				}
			} catch(Exception e) {}
		}
		
		CommonLogger.logger.info("--------end of user infomation's collection--------");
	}

}
