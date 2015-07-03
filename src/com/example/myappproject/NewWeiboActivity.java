/**
 * 
 */
package com.example.myappproject;

import com.example.sbean.ActivityStack;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 韶阳
 *
 */
public class NewWeiboActivity extends Activity {

	private StatusesAPI api;
	private Oauth2AccessToken accessToken;
	private String uid;
	private EditText et;
	private final int REPLY = 1;
	private ActivityStack stack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO 自动生成的方法存根

		stack = ActivityStack.getInstance();
		stack.push(this);
		setContentView(R.layout.new_weibo_layout);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);

		accessToken = AccessTokenKeeper.readAccessToken(this);
		uid = accessToken.getUid();

		api = new StatusesAPI(this, Constants.APP_KEY, accessToken);

		et = (EditText) findViewById(R.id.weibo_content);
		et.addTextChangedListener(new EditListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mf = getMenuInflater();
		mf.inflate(R.menu.send_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO 自动生成的方法存根
		switch (item.getItemId()) {
		case R.id.send:
			Log.i("weibo", "send");
			String content = et.getText().toString();
			if (content.length() > 140) {
				Toast.makeText(this, "请不要超过140个字符", Toast.LENGTH_SHORT).show();
				return super.onMenuItemSelected(featureId, item);
			} else if (content.length() == 0){
				Toast.makeText(this, "您还未输入内容", Toast.LENGTH_SHORT).show();
				return super.onMenuItemSelected(featureId, item);
			}
			createNotify();
			api.update(content, "0", "0", new SendWeiboListener());
			break;
		case android.R.id.home:
			Log.i("weibo", "home");
			stack.remove(this);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public class SendWeiboListener implements RequestListener {

		@Override
		public void onComplete(String str) {
			Log.i("weibo", "send complete");
			cancelNotify();
			setResult(REPLY);
			stack.remove(NewWeiboActivity.this);
		}

		@Override
		public void onWeiboException(WeiboException arg0) {

		}
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		stack.remove(this);
		super.onDestroy();
	}

	// 取消掉通知
	private void cancelNotify() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
	}

	// 产生通知
	private void createNotify() {
		Notification not = new NotificationCompat.Builder(this)
				.setTicker("正在发送").setSmallIcon(R.drawable.my_icon)
				.setContentTitle("fakeweibo").setContentText("发送中")
				.setAutoCancel(true).build();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(0, not);
	}
	
	class EditListener implements TextWatcher{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			TextView tv=(TextView) findViewById(R.id.tcount);
			if(140-s.length()<=10){
				tv.setVisibility(View.VISIBLE);
				tv.setText("您还可以输入"+(140-s.length())+"个字");
			}else{
				tv.setVisibility(View.GONE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO 自动生成的方法存根
			
		}
		
	}

}
