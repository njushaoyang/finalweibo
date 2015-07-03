package com.example.myappproject;

import com.example.sbean.ActivityStack;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WBAuthActivity extends Activity {
	private static final String TAG = "weibosdk";

	private AuthInfo mAuthInfo;

	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessToken mAccessToken;

	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
	private SsoHandler mSsoHandler;

	private ActivityStack stack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_layout);
		ActionBar ab = getActionBar();
		ab.setDisplayShowHomeEnabled(false);
		stack = ActivityStack.getInstance();

		mAuthInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		mSsoHandler = new SsoHandler(WBAuthActivity.this, mAuthInfo);

		stack.push(this);
		judgeLogin();
	}

	private void judgeLogin() {
		// TODO 自动生成的方法存根
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(this);
		if (token.isSessionValid()) {
			Intent intent = new Intent(this, WeiboListActivity.class);
			startActivity(intent);
			stack.remove(this);
		}
	}

	public void auth(View v) {
		mSsoHandler.authorize(new AuthListener());
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	class AuthListener implements WeiboAuthListener {
		@Override
		public void onComplete(Bundle values) {
			mAccessToken = Oauth2AccessToken.parseAccessToken(values); // 从Bundle
																		// 中解析Token
			if (mAccessToken.isSessionValid()) {
				AccessTokenKeeper.writeAccessToken(WBAuthActivity.this,
						mAccessToken); // 保存Token
			
				Intent intent = new Intent(WBAuthActivity.this,
						WeiboListActivity.class);
				startActivity(intent);
				finish();
			} else {
				// 当您注册的应用程序签名不正确时，就会收到错误Code，请确保签名正确
				String code = values.getString("code");
			}
		}

		@Override
		public void onCancel() {
			// TODO 自动生成的方法存根

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		stack.remove(this);
		super.onDestroy();
	}
}
