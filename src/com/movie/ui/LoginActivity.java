package com.movie.ui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.movie.R;
import com.movie.app.BaseActivity;
import com.movie.app.Constant;
import com.movie.client.bean.Login;
import com.movie.client.bean.User;
import com.movie.client.service.BaseService;
import com.movie.client.service.CallBackService;
import com.movie.client.service.LoginService;
import com.movie.client.service.UserService;
import com.movie.network.HttpCaptchaService;
import com.movie.network.HttpLoginService;
import com.movie.util.BytesUtils;
import com.movie.view.CustomDialog;


public class LoginActivity extends BaseActivity implements OnClickListener, CallBackService {

	EditText accountEdit;
	EditText passwordEdit;
	Button loginButton;
	Button forgetButton;
	TextView title;
	TextView right_text;
	ImageView captchaView;
	LoginService loginService;
	UserService userService;
	BaseService httpLoginService;
	BaseService httpCaptchaService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		loginService = new LoginService();
		httpLoginService=new HttpLoginService(this);
		httpCaptchaService=new HttpCaptchaService(this);
		userService = new UserService();
		initViews();
		initEvents();
		initData();
	}
	@Override
	protected void initViews() {		
		accountEdit = (EditText) this.findViewById(R.id.account);
		passwordEdit= (EditText) this.findViewById(R.id.password);
		loginButton = (Button) this.findViewById(R.id.login);
		forgetButton = (Button) this.findViewById(R.id.forget);
		accountEdit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		title = (TextView) findViewById(R.id.title);
		right_text = (TextView) findViewById(R.id.right_text);
		right_text.setVisibility(View.VISIBLE);	
	}

	@Override
	protected void initEvents() {
		loginButton.setOnClickListener(this);
		forgetButton.setOnClickListener(this);
		right_text.setOnClickListener(this);	
		right_text.setClickable(true);
	}

	@Override
	protected void initData() {
		title.setText("登陆");
		right_text.setText("注册");
	}


	private void doLogin(String captcha) {

		String account=accountEdit.getText().toString();
		String password=passwordEdit.getText().toString().trim();
		if (account==null||account.isEmpty()) {
		   showToask("账号不能为空");
		   return;
		}
		if (password==null||password.isEmpty()) {
		   showToask("密码不能为空");
		   return;
	    }
		try {
			String pwd = BytesUtils.toHexString(MessageDigest.getInstance("MD5").digest((account +":"+ password).getBytes()), false);
			httpLoginService.addParams("login", account);
			httpLoginService.addParams("password", pwd);
			if(null!=captcha){
				httpLoginService.addParams("captcha", captcha);
			}
			httpLoginService.execute(this);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	
	}
	private void doCaptcha(String account){
		if(null!=account&&!account.isEmpty()){
			httpCaptchaService.addParams("login", account);
			httpCaptchaService.execute(this);
		}
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.right_text:
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.login:
			doLogin(null);
			break;
		case R.id.forget:
			Intent forgetIntent = new Intent(this, ForgetActivity.class);
			startActivity(forgetIntent);
			this.finish();
			break;
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		Intent intent = new Intent(this, MainActivity.class);
		this.startActivity(intent);
		this.finish();
		
	}

	
    
	@Override
	public void SuccessCallBack(Map<String, Object> map) {
		hideProgressDialog();
		String code=map.get(Constant.ReturnCode.RETURN_STATE).toString();
		if (Constant.ReturnCode.STATE_1.equals(code)) {
			String tag=map.get(Constant.ReturnCode.RETURN_TAG).toString();
			if(tag.equals(httpLoginService.TAG)){
				overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
				Login login=new Login();
	
				/*记录登入信息，下次自动登陆*/
				String account=accountEdit.getText().toString();
				String password=passwordEdit.getText().toString().trim();
				String pwd=null;
				try {
					pwd = BytesUtils.toHexString(MessageDigest.getInstance("MD5").digest((account +":"+ password).getBytes()), false);
				} catch (NoSuchAlgorithmException e) {
					
					e.printStackTrace();
				}	
				String memberId=map.get("value").toString();
				login.setAccount(account);
				login.setPass(pwd);
				loginService.addLogin(login);
				User user=new User();
				user.setMemberId(memberId);
				userService.addUser(user);
				
				onBackPressed();
			}else if(tag.equals(httpCaptchaService.TAG)){
				Object object= map.get(Constant.ReturnCode.RETURN_VALUE);
				if(null!=object){
					byte[] content=(byte[])object;
					final CustomDialog.Builder builder = new CustomDialog.Builder(this);  
					builder.setTitle(R.string.captcha_hint);
					builder.setCode(BitmapFactory.decodeByteArray(content, 0, content.length));
			        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialog, int which) {  
			                dialog.dismiss();  
			            }  
			        });  
			        
			        builder.setNegativeButton("确定",  
			                new android.content.DialogInterface.OnClickListener() {  
			                    public void onClick(DialogInterface dialog, int which) {  
			                        String realCode=builder.getRealCode();
			                        //设置操作
			                        doLogin(realCode);
			                        dialog.dismiss();
			                    }  
			                });  
			        builder.setCaptchaChange(new android.content.DialogInterface.OnClickListener() {  
			                    public void onClick(DialogInterface dialog, int which) {  
			                        //dialog.dismiss();  
			                        doCaptcha(accountEdit.getText().toString());
			                    }  
			                });
			  
			        builder.create().show();  
				}
			}
			
		}else if(Constant.ReturnCode.STATE_2.equals(code)){	
			doCaptcha(accountEdit.getText().toString());
		}else{
			String message=map.get(Constant.ReturnCode.RETURN_MESSAGE).toString();
			showToask(message);
		}
		
	}
	@Override
	public void ErrorCallBack(Map<String, Object> map) {
		hideProgressDialog();
		String message=map.get(Constant.ReturnCode.RETURN_MESSAGE).toString();
		showToask(message);
	}

	@Override
	public void OnRequest() {
		showProgressDialog("提示", "正在登陆，请稍后......");
	}

	

	

}
