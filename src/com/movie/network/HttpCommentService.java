package com.movie.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.os.Message;

import com.movie.app.Constant;
import com.movie.app.ErrorState;
import com.movie.app.InvokeException;
import com.movie.client.bean.Dictionary;
import com.movie.client.dao.BaseDao;
import com.movie.client.dao.CommentDaoImple;
import com.movie.client.service.BaseService;
import com.movie.client.service.CallBackService;
import com.movie.util.HttpUtils;

public class HttpCommentService  extends  BaseService{

	BaseDao commentDao;
	public HttpCommentService() {
		TAG="HttpCommentService";
		commentDao = new CommentDaoImple();
	}
	public HttpCommentService(Context context) {
		TAG="HttpCommentService";
		this.context=context;
		commentDao = new CommentDaoImple();
	}
	
	@Override
	public void requestServer(CallBackService callbackService) {
		HashMap<String, Object> map=new HashMap<String, Object>();
		Message message = handler.obtainMessage();
		try {
			requestCount++;
			String sid= getSid();
			headers.put(SESSION_KEY, sid);
			int count = commentDao.countData(null);
			if(count>0){
				throw new InvokeException(ErrorState.Success.getState(),ErrorState.Success.getMessage());
			}
			String result  = HttpUtils.requestGet(Constant.Dic_Comment_API_URL,headers);
			if (result != null) {		
				
				try {
					map = objectMapper.readValue(result, typeReference);
				} catch (Exception e) {
					throw new InvokeException(ErrorState.ConvertJsonFasle.getState(),ErrorState.ConvertJsonFasle.getMessage());
			    }
				Integer state = (Integer) map.get(Constant.ReturnCode.RETURN_STATE);
				if (state==ErrorState.Success.getState()) {
					Map<String, String> value = (HashMap<String, String>) map.get(Constant.ReturnCode.RETURN_VALUE);
					Iterator<String> keys= value.keySet().iterator();
					String key=null;
					String data= null;
					Dictionary comment = null;
					while(keys.hasNext()){
						 key=keys.next();
						 data=value.get(key);
						 comment = new Dictionary();
						 comment.setId(Integer.parseInt(key));
						 comment.setName(data);
						 commentDao.setContentValues(comment);
						 commentDao.addData();
				    }
					message.what = SUCCESS_STATE;
				}else if(state==ErrorState.SessionInvalid.getState()){
					if(requestCount<MAXREQUEST){
						updateSid();
						requestServer(callbackService);
					}
				}else{
					message.what = FAILE_STATE;
				}
			}else{
				throw new InvokeException(ErrorState.InvalidResource.getState(),ErrorState.InvalidResource.getMessage());
			}
		} catch (InvokeException e) {
			 message.what = FAILE_STATE;
			 map.put(Constant.ReturnCode.RETURN_STATE, e.getState());
			 map.put(Constant.ReturnCode.RETURN_MESSAGE, e.getMessage());
			
		}finally{
			map.put(Constant.ReturnCode.RETURN_TAG, TAG);
		    message.getData().putSerializable(Constant.ReturnCode.RETURN_DATA, map);
			handler.sendMessage(message);
		}
		
	}
	

	
	

}