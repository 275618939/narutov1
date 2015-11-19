package com.movie.network;

import java.util.HashMap;

import android.content.Context;
import android.os.Message;

import com.movie.app.Constant;
import com.movie.app.ErrorState;
import com.movie.app.InvokeException;
import com.movie.client.service.BaseService;
import com.movie.client.service.CallBackService;
import com.movie.util.HttpUtils;

public class HttpPlayMovieService  extends  BaseService{

	public HttpPlayMovieService() {
		TAG="HttpPlayMovieService";
		
	}
	public HttpPlayMovieService(Context context) {
		TAG="HttpPlayMovieService";
		this.context=context;
	}
	
	@Override
	public void requestServer(CallBackService callbackService) {
		HashMap<String, Object> map=new HashMap<String, Object>();
		Message message = handler.obtainMessage();
		try {
			requestCount++;
			StringBuilder path=new StringBuilder(Constant.Playing_API_URL);		
			String result  = HttpUtils.requestGet(path.toString(),headers);
			if (result != null) {		
				try {
					map = objectMapper.readValue(result, typeReference);
				} catch (Exception e) {
					throw new InvokeException(ErrorState.ConvertJsonFasle.getState(),ErrorState.ConvertJsonFasle.getMessage());
			    }
				Integer state = (Integer) map.get(Constant.ReturnCode.RETURN_STATE);
				if (state==ErrorState.Success.getState()) {
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
