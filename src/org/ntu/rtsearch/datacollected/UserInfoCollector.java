package org.ntu.rtsearch.datacollected;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ntu.rtsearch.utils.CommonLogger;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class UserInfoCollector {
		
	private static final int TAGET_USER_CNT = 500000;

	private static final int MAX_FRIENDS_CNT = 100;
	
	private List<String> userNetwork = new ArrayList<String>();
	
	private HashSet<String> userIdSet = new HashSet<String>();
	
	private OAuthService service = Resource.getService();

	public static void main(String[] args) throws Exception {
		UserInfoCollector collector = null;
		try {
			collector = new UserInfoCollector();
			//collector.initSeeds();
			//collector.buildUserNetwork();
			//collector.getUserInfo();
			collector.getSkipLength();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			//collector.saveUserId();
		}
	}
	
	/**
	 * build user network, 
	 * generate files: user_id, relation_ship
	 * @throws IOException
	 * @throws EmailException 
	 */
	private void buildUserNetwork() throws IOException, EmailException {
		CommonLogger.logger.info("--------begin social network data collection--------");
		// buffer: 2MB
		BufferedWriter relationShipWriter = null;
		try {
			relationShipWriter = new BufferedWriter(new FileWriter(Constants.RELATION_SHIP_FILE_PATH, true));
			
			Token token = Resource.getToken(0);
			int userIdx = 10249, tokenIdx = 0, cnt = 0;
			
			while(userIdSet.size() < TAGET_USER_CNT) {
				// request for the seed's friends
				StringBuilder sb = new StringBuilder();
				String userId = userNetwork.get(userIdx++);
				sb.append(userId);
				CommonLogger.logger.info("get friends of user: " + userId);
				
				OAuthRequest req = new OAuthRequest(Verb.GET, Constants.GET_FRIENDSID_URL + userId);
				service.signRequest(token, req);
				Response resp = req.send();
				
				if(resp.getCode() == 200) {
					try {
						JSONObject jsonObj = new JSONObject(resp.getBody());
						JSONArray idArr = (JSONArray) jsonObj.get("ids");
						
						String newId = null;
						// add no more than MAX_FRIENDS_CNT friends to the user network
						for (int i = 0; i < idArr.length() && i < MAX_FRIENDS_CNT; i++) {
							newId = idArr.getString(i);
							
							if(!userIdSet.contains(newId)) {
								userIdSet.add(newId);
								userNetwork.add(newId);
								sb.append("," + newId);
							}
						}
						
						// save relationship
						sb.append("\n");
						relationShipWriter.write(sb.toString());
						CommonLogger.logger.info("user amount: " + userNetwork.size());
					} catch (JSONException e) {
						CommonLogger.logger.info("json parse error");
					}
				} else if(resp.getCode() != 400) {
					CommonLogger.logger.info("error code:" + resp.getCode());
					CommonLogger.logger.info("request relationship for user id: " 
							+ userId + " response error");
				}
				
				// limited, change access token
				if(++cnt > 350 || resp.getCode() == 400) {
					cnt = 0;
					tokenIdx = (++tokenIdx) % 20;
					token = Resource.getToken(tokenIdx);
					CommonLogger.logger.info("change access token: token " + tokenIdx);
				}
				
				//sleep(10);
			}
		} catch (IOException e) {
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
			relationShipWriter.flush();
			relationShipWriter.close();
			Logger.shutdown();
		}

		CommonLogger.logger.info("--------end social network data collection--------");
	}

	/**
	 * sleep for some seconds
	 * @param seconds
	 */
	private void sleep(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);		// max 350request per hour
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * save users'id to file user_id
	 * @throws IOException
	 */
	private void saveUserId() throws IOException {
		BufferedWriter userIdWriter = new BufferedWriter(new FileWriter(Constants.USER_ID_FILE_PATH, true));
		for(String userId : userNetwork) {
			userIdWriter.write(userId + "\n");
		}
		userIdWriter.flush();
		userIdWriter.close();
	}

	
	public void finalize() {
		
	}
	
	/**
	 * get user information, 
	 * generate file: user_info
	 * @throws Exception 
	 */
	private void getUserInfo() throws Exception {
		CommonLogger.logger.info("--------begin users' information collection--------");
		OAuthRequest req4UserInfo = null;
		
		BufferedReader userIdReader = new BufferedReader(new FileReader(Constants.USER_ID_FILE_PATH));
		BufferedWriter userInfoWriter = null;
	
		try {
			userInfoWriter = new BufferedWriter(new FileWriter(Constants.USER_INFO_FILE_PATH, true), 2097152);
			
			Token token = Resource.getToken(0);
			String userId = null;
			int cnt = 0, tokenIdx = 0;
			userIdReader.skip(4488980);
			while((userId = userIdReader.readLine()) != null) {
				req4UserInfo = new OAuthRequest(Verb.GET, Constants.USER_INFO_URL + userId);
				service.signRequest(token, req4UserInfo);
				Response resp = req4UserInfo.send();
				
				if(resp.getCode() == 200) {
					CommonLogger.logger.info("get info for useId: " + userId);
					userInfoWriter.write(resp.getBody() + "\n");
				} else if(resp.getCode() != 400) {
					CommonLogger.logger.info("error code:" + resp.getCode());
					CommonLogger.logger.info("userInfo for user id: " 
							+ userId + " response error");
				}
				
				// limited, change access token
				if(++cnt > 350 || resp.getCode() == 400) {
					cnt = 0;
					tokenIdx = (++tokenIdx) % 20;
					token = Resource.getToken(tokenIdx);
					CommonLogger.logger.info("change access token: token " + tokenIdx);
				}
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
			userIdReader.close();
			userInfoWriter.flush();
			userInfoWriter.close();
		}
		
		CommonLogger.logger.info("--------end of user infomation's collection--------");
	}
	
	/**
	 * get userId for seeds
	 * @throws IOException
	 */
	private void initSeeds() throws IOException {
		CommonLogger.logger.info("--------start generating 90 users's id as seeds...--------");
		File userIdSeeds = new File(Constants.USER_ID_SEEDS_FILE_PATH);
		File userIdFile = new File(Constants.USER_ID_FILE_PATH);
		
		// exist, read from the user id seeds file
		if(userIdFile.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(userIdFile));
			String userId = null;
			while((userId = br.readLine()) != null) {
				userNetwork.add(userId);
				userIdSet.add(userId);
			}
			br.close();
			return;
		}
		
		// exist, read from the user id seeds file
		if(userIdSeeds.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(userIdSeeds));
			String userId = null;
			while((userId = br.readLine()) != null) {
				userNetwork.add(userId);
				userIdSet.add(userId);
			}
			br.close();
			return;
		}
		
		BufferedReader br = new BufferedReader(new FileReader(Constants.SEED_FILE_PATH));
		BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.USER_ID_SEEDS_FILE_PATH));
		
		String userName = null;
		Token token = Resource.getToken(0);
		int cnt = 0, tokenIdx = 0;
		while ((userName = br.readLine()) != null) {
			if (userName.indexOf("#") == -1 && !"".equals(userName)) {
								
				// requst for the seed's info(id)
				OAuthRequest req = new OAuthRequest(Verb.GET, Constants.USER_INFO_URL2 + userName);
				service.signRequest(token, req);
				Response resp = req.send();
				if(resp.getCode() == 200) {
					try {
						String content = resp.getBody();
						JSONObject jsonObj = new JSONObject(content);
						String userId = String.valueOf(jsonObj.get("id_str"));
						userNetwork.add(userId);
						userIdSet.add(userId);
						bw.write(userId + "\n");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if(resp.getCode() != 400) {
					CommonLogger.logger.info("error code:" + resp.getCode());
					CommonLogger.logger.info("get userId for user name: " 
							+ userName + " response error");
				}
				
				// limited, change access token
				if(++cnt > 350 || resp.getCode() == 400) {
					cnt = 0;
					token = Resource.getToken(++tokenIdx);
					CommonLogger.logger.info("change access token: token " + tokenIdx);
				}
			}
		}
		
		br.close();
		bw.flush();
		bw.close();
		CommonLogger.logger.info("--------got " + userNetwork.size() + " user id as seeds to build a social network!--------");
	}
		
	public void getSkipLength() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(Constants.USER_ID_FILE_PATH));
		String line;
		long skip = 0;
		//br.skip(366748);
		//System.out.println(br.readLine());
		
		while((line = br.readLine()) != null) {
			skip += line.length() + 1;
			if("306289325".equals(line)) {
				System.out.println(skip);
				break;
			}
		}
	}
}
