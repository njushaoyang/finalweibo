/**
 * 
 */
package com.example.myappproject;

import com.example.sbean.ActivityStack;
import com.example.sbean.CacheManage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author 韶阳
 *
 */
public class UserInfoActivity extends Activity {

	private ImageView head;
	private TextView name;
	private TextView introduction;
	private TextView weiboCount;
	private TextView careCount;
	private TextView fansCount;
	private UsersAPI api;
	private Oauth2AccessToken accessToken;
	private Long userid;
	ActivityStack stack;
	private Button confirm;
	private Button cancel;
	private RelativeLayout cover;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_setting);
		head = (ImageView) findViewById(R.id.user_head);
		name = (TextView) findViewById(R.id.user_name);
		introduction = (TextView) findViewById(R.id.user_introduction);
		weiboCount = (TextView) findViewById(R.id.weibo_count);
		careCount = (TextView) findViewById(R.id.care_count);
		fansCount = (TextView) findViewById(R.id.fans_count);

		stack = ActivityStack.getInstance();
		stack.push(this);

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);

		if (MyApplication.getInstance().user != null) {
			setView(MyApplication.getInstance().user);
		} else {
			accessToken = AccessTokenKeeper.readAccessToken(this);
			api = new UsersAPI(this, Constants.APP_KEY, accessToken);
			userid = Long.parseLong(accessToken.getUid());
			api.show(userid, new RListener());
		}
		findViewById(R.id.logout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("weibo", "logout");
				cover = (RelativeLayout) findViewById(R.id.cover);
				cover.setVisibility(View.VISIBLE);
			}
		});
		findViewById(R.id.chang_image).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(UserInfoActivity.this,
								GetImageActivity.class);
						startActivityForResult(intent, 11);
					}
				});
		findViewById(R.id.help).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(UserInfoActivity.this,
								HelpActivity.class);
						startActivity(intent);
					}
				});

		confirm = (Button) findViewById(R.id.confirm);
		cancel = (Button) findViewById(R.id.cancel);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cover.setVisibility(View.GONE);
				AccessTokenKeeper.clear(UserInfoActivity.this);
				((MyApplication) getApplication()).user = null;
				CacheManage.notifyInvalid();

				Intent intent = new Intent(UserInfoActivity.this,
						WBAuthActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				stack.clearOther(WBAuthActivity.class);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				cover.setVisibility(View.GONE);
			}
		});
	}

	private void setView(User user) {
		name.setText(user.name);
		introduction.setText(user.description);
		weiboCount.setText(user.statuses_count + "");
		careCount.setText(user.friends_count + "");
		fansCount.setText(user.followers_count + "");

		ImageLoader.getInstance().displayImage(user.avatar_large, head,
				new SimpleImageLoadingListener() {
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						super.onLoadingComplete(imageUri, view, loadedImage);
					}
				});
	}

	public class RListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				User user = User.parse(response);
				MyApplication.getInstance().setUsr(user);
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("name", user.name);
				bundle.putString("url", user.avatar_large);
				bundle.putString("introduction", user.description);
				bundle.putInt("weibocount", user.statuses_count);
				bundle.putInt("carecount", user.friends_count);
				bundle.putInt("fanscount", user.followers_count);

				message.setData(bundle);
				handler.sendMessage(message);
			}

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

	public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			stack.remove(this);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 11 && resultCode == 12) {
			/*
			 * File file = getFilesDir(); File f = new File(file,
			 * GetImage.BACKGROUND_IMAGE); if (!f.exists()) return;
			 * BufferedInputStream bs; try { Log.i("weibo","success"); bs = new
			 * BufferedInputStream(new FileInputStream(f)); Bitmap btp =
			 * BitmapFactory.decodeStream(bs); ImageView iv = (ImageView)
			 * lv.header2 .findViewById(R.id.background);
			 * iv.setImageBitmap(btp); bs.close(); btp = null; } catch
			 * (IOException e) { // TODO 自动生成的 catch 块 e.printStackTrace(); }
			 */
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String uname = bundle.getString("name");
			String uurl = bundle.getString("url");
			String intro = bundle.getString("introduction");
			int wc = bundle.getInt("weibocount");
			int cc = bundle.getInt("carecount");
			int fc = bundle.getInt("fanscount");

			name.setText(uname);
			introduction.setText(intro);
			weiboCount.setText(wc + "");
			careCount.setText(cc + "");
			fansCount.setText(fc + "");

			ImageLoader.getInstance().displayImage(uurl, head,
					new SimpleImageLoadingListener() {
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							super.onLoadingComplete(imageUri, view, loadedImage);
						}
					});
		}
	};

}
