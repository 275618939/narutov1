package com.movie.client.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.movie.app.Constant;
import com.movie.app.ErrorState;
import com.movie.app.InvokeException;
import com.movie.client.bean.Session;
import com.movie.client.dao.BaseDao;
import com.movie.client.dao.SessionDaoImple;
import com.movie.client.db.SQLHelper;
import com.movie.util.HttpUtils;

public abstract class BaseService  {

	public  String TAG="BaseService";
	protected final int SUCCESS_STATE=0;
	protected final int FAILE_STATE=1;
	protected final String SESSION_KEY="Session-Id";
	public final String URL_KEY="url-key";
	protected static final TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
	protected static final ObjectMapper objectMapper = new ObjectMapper();
	protected Map<String, String> headers = new HashMap<String, String>();
	protected Map<String, String> urls = new HashMap<String, String>();
	protected Map<String, Object> params = new HashMap<String, Object>();
	protected static final int MAXREQUEST=2;
	protected static final int REQUESTDEFUALCOUNT=1;
	protected int requestCount;
	protected CallBackService callBackService;
	protected Context context; 
    private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private static ThreadPoolExecutor executor =  new ThreadPoolExecutor(5, 8, 20,	TimeUnit.SECONDS,queue);
	public abstract void requestServer(CallBackService callbackService);

	public BaseService(){
		initService();
	}
	
	public void  execute(final CallBackService callbackService) {
		this.callBackService=callbackService;
		this.callBackService.OnRequest();
	    executor.execute(new Runnable() {
			@Override
			public void run() {
				requestServer(callBackService);
			}
		 });
	}

	
	protected void initService(){
		clearReqeustParams();
		requestCount=REQUESTDEFUALCOUNT;
	}
	
	public void clearReqeustParams() {
		headers.clear();
		params.clear();
	}
	/**
	 * 获取回话SID
	 * @return SID
	 * @throws InvokeException
	 */
	public String getSid() throws InvokeException {
		BaseDao sesssionDao = new SessionDaoImple();
		Map<String, String> map = sesssionDao.viewData(null, null);
		if (null != map&&map.size()>0) {
			return map.get(SQLHelper.SID);
		} else {
			//记录SID
			String sid = requestSid();
			Session session=new Session();
			session.setSid(sid);
			sesssionDao.setContentValues(session);
			sesssionDao.addData();
			sesssionDao.close();
			return sid;
		}
	}
	/**
	 * 重新获取回话SID
	 * @return SID
	 * @throws InvokeException
	 */
	public String updateSid() throws InvokeException {
		
		BaseDao sesssionDao = new SessionDaoImple();
		//记录SID
		String sid = requestSid();
		sesssionDao.deleteData(null, null);
		Session session=new Session();
		session.setSid(sid);
		sesssionDao.setContentValues(session);
		sesssionDao.addData();
		sesssionDao.close();
		return sid;
		
	}
	public String requestSid() throws InvokeException {
		Integer state = 0;
		String desc = null;
		String result = HttpUtils.requestGet(Constant.Session_API_URL,null);
		if (result != null) {
			Map<String, Object> map = null;
			try {
				map = objectMapper.readValue(result, typeReference);
			} catch (Exception e) {
				throw new InvokeException(ErrorState.ConvertJsonFasle);
			}
			state = (Integer) map.get("state");
			desc = map.get("desc").toString();
			Log.i("<BaseService RequestSid-state>", state.toString());
			Log.i("<BaseService RequestSid-desc>", map.get("desc").toString());
			if (state.intValue() == ErrorState.Success.getState()) {
				String sid = (String) map.get("value");
				return sid;
			} else {
				throw new InvokeException(state, desc);
			}
		}
		return null;

	}
	protected  Handler handler = new Handler(){
		public void handleMessage(Message message){
			HashMap<String,Object> data =(HashMap<String, Object>) message.getData().getSerializable(Constant.ReturnCode.RETURN_DATA);
			  switch(message.what) {
				 case   0:
					 callBackService.SuccessCallBack(data);
					 break;
				  case   1:
					 callBackService.ErrorCallBack(data);
					 break;
			  }
						
		}
	};
	public void addHeads(String key,String value){
		headers.put(key, value);
	}
	public  void addParams(String key,Object value){
		params.put(key, value);
	}
	public  void addUrls(String value){
		urls.put(URL_KEY, value);
	}

	
}