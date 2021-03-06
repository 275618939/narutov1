package com.movie.network;

import java.util.HashMap;

import android.content.Context;
import android.os.Message;

import com.movie.app.Constant;
import com.movie.app.InvokeException;
import com.movie.client.service.BaseService;
import com.movie.client.service.CallBackService;
import com.movie.state.ErrorState;
import com.movie.util.HttpUtils;

public class HttpCaptchaService extends BaseService {



	public HttpCaptchaService() {
		TAG="HttpCaptchaService";
	}
	public HttpCaptchaService(Context context) {
		this.context=context;
		TAG="HttpCaptchaService";
	}

	@Override
	public void requestServer(CallBackService callbackService) {
		HashMap<String, Object> map=new HashMap<String, Object>();
		Message message = handler.obtainMessage();
		try {
			Object login=params.get("login");
			StringBuilder url=new StringBuilder(Constant.Captcha_API_URL);
			if(null!=login){
				url.append("/").append(login);
			}
			byte [] values = HttpUtils.requestGetByte(url.toString(), headers);
			if (values != null) {
				map.put(Constant.ReturnCode.RETURN_STATE, ErrorState.Success.getState());
				map.put(Constant.ReturnCode.RETURN_VALUE, values);
				message.what = SUCCESS_STATE;
			}else{
				throw new InvokeException(ErrorState.InvalidResource.getState(),ErrorState.InvalidResource.getMessage());
			}
		}catch (Exception e) {
			 message.what = FAILE_STATE;
			 map.put(Constant.ReturnCode.RETURN_STATE, ErrorState.RuntimeException.getState());
			 map.put(Constant.ReturnCode.RETURN_MESSAGE, e.getMessage());
			
		}finally{
			map.put(Constant.ReturnCode.RETURN_TAG, TAG);
		    message.getData().putSerializable(Constant.ReturnCode.RETURN_DATA, map);
			handler.sendMessage(message);
		}

	}

}
