package com.movie.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtils {


	
	public static byte[] requestGetByte(String url,Map<String,String>headers){
		return requestGetByte("GET",url,headers);
	}
	public static String requestGet(String url,Map<String,String>headers){
		return requestGetDelete("GET",url,headers);
	}

	public static String requestDelete(String url,Map<String,String>headers){
		return requestGetDelete("DELETE",url,headers);
	}
	public static String requestPost(String url,String params,Map<String,String>headers){
		return requestPostPut("POST",url,params,headers);
	}
	public static String requestPut(String url,String params,Map<String,String>headers){
		return requestPostPut("PUT",url,params,headers);
	}
	
	public static String requestPost(String url,Map<String,String>headers,Map<String,Object>params){
		return requestPostPut("POST",url,getParams(params),headers);
	}
	public static String requestPut(String url,Map<String,String>headers,Map<String,Object>params){
		return requestPostPut("PUT",url,getParams(params),headers);
	}
	private static byte[] requestGetByte(String method,String url,Map<String,String>headers){
		
		InputStream is=null;
		int respCode=0;
		ByteArrayOutputStream baos=null;
		HttpURLConnection conn = null;
		
		try{
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			if(headers!=null){
				for(Map.Entry<String, String> entry:headers.entrySet()){
					conn.setRequestProperty(entry.getKey(),entry.getValue());
				}
			}
			conn.setRequestMethod(method);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(180000);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			
			conn.connect();
			
			respCode=conn.getResponseCode();
		
			if (respCode>=200&&respCode<300) {
				String charset=KeyValueParser.parserCharset(conn.getHeaderField("Content-Type"), "UTF-8");
				is = conn.getInputStream();
			
				byte buffer[] = new byte[2048];
				int len;
				baos = new ByteArrayOutputStream();
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
				baos.close();
				
				return baos.toByteArray();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(is!=null)try{is.close();}catch(IOException e){};
			if(conn!=null)try{conn.disconnect();}catch(Exception e){};
		
		}
		return null;
	}

	private static String requestGetDelete(String method,String url,Map<String,String>headers){
	
		InputStream is=null;
		int respCode=0;
		ByteArrayOutputStream baos=null;
		HttpURLConnection conn = null;
		
		try{
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			if(headers!=null){
				for(Map.Entry<String, String> entry:headers.entrySet()){
					conn.setRequestProperty(entry.getKey(),entry.getValue());
				}
			}
			
			conn.setRequestMethod(method);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(180000);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			
			conn.connect();
			
			respCode=conn.getResponseCode();
		
			if (respCode>=200&&respCode<300) {
				String charset=KeyValueParser.parserCharset(conn.getHeaderField("Content-Type"), "UTF-8");
				is = conn.getInputStream();
			
				byte buffer[] = new byte[2048];
				int len;
				baos = new ByteArrayOutputStream();
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
				baos.close();
				
				return new String(baos.toByteArray(),charset);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(is!=null)try{is.close();}catch(IOException e){};
			if(conn!=null)try{conn.disconnect();}catch(Exception e){};
		
		}
		return null;
	}
	private static String requestPostPut(String method,String url,String params,Map<String,String>headers) {
		
		OutputStream os=null;
		InputStream is=null;
		int respCode=0;
		ByteArrayOutputStream baos=null;
		HttpURLConnection conn = null;
		
		try{
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			byte data[] = params.getBytes();
			conn.setDoOutput(true);	
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Content-Type", "text/plain;charset=utf8");
			conn.setRequestProperty("Content-Length",Integer.toString(data.length));
			if(headers!=null){
				for(Map.Entry<String, String> entry:headers.entrySet()){
					conn.setRequestProperty(entry.getKey(),entry.getValue());
				}
			}
			
			conn.setRequestMethod(method);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(180000);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			
			conn.connect();
			
			os = conn.getOutputStream();
			os.write(data);
			os.flush();
			os.close();
			
			respCode=conn.getResponseCode();
		
			if (respCode>=200&&respCode<300) {
				String charset=KeyValueParser.parserCharset(conn.getHeaderField("Content-Type"), "UTF-8");
				is = conn.getInputStream();
			
				byte buffer[] = new byte[4096];
				int len;
				baos = new ByteArrayOutputStream();
				while ((len = is.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
				baos.close();
				return new String(baos.toByteArray(),charset);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(is!=null)try{is.close();}catch(IOException e){};
			if(conn!=null)try{conn.disconnect();}catch(Exception e){};
		
		}
		return null;
	}
	
	private static String getParams(Map<String,Object>params){
		if(params!=null){
			boolean once=false;
			StringBuffer sb=new StringBuffer();
			for(Map.Entry<String, Object> entry:params.entrySet()){
				if(once){
					sb.append('&');
				}
				else{
					once=true;
				}
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue());
			}
			return sb.toString();
		}
		return null;
	}
	public boolean ping(String ip) {
		Runtime run = Runtime.getRuntime();
		Process proc = null;
		try {
			String str = "ping -c 1 -i 0.2 -W 1 " + ip;
			System.out.println(str);
			proc = run.exec(str);
			int result = proc.waitFor();
			if (result == 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			proc.destroy();
		}
		return false;

	}
}
