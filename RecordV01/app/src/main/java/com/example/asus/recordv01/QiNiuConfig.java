package com.example.asus.recordv01;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.PutPolicy;

import org.json.JSONException;

/**
 * Created by lixiaodaoaaa on 14/10/12.
 */
public final class QiNiuConfig {
	public static final String token = getToken();
	public static final String QINIU_AK = "DDu_BviK9cl4lvRmCRD7sEKNKdYTiNi684VIl84u";
	public static final String QINIU_SK = "BRJkj8DWUwP3ks-t-xbPckOEX4lrBOn7HyL6ajJZ";
	public static final String QINIU_BUCKNAME = "recordings";

	public static String getToken() {

		Mac mac = new Mac(QiNiuConfig.QINIU_AK, QiNiuConfig.QINIU_SK);
		PutPolicy putPolicy = new PutPolicy(QiNiuConfig.QINIU_BUCKNAME);
		putPolicy.returnBody = "{\"name\": $(fname),\"size\": \"$(fsize)\",\"w\": \"$(imageInfo.width)\",\"h\": \"$(imageInfo.height)\",\"key\":$(etag)}";
		try {
			String uptoken = putPolicy.token(mac);
			System.out.println("debug:uptoken = " + uptoken);
			return uptoken;
		} catch (AuthException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
