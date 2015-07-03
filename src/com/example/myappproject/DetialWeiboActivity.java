/**
 * 
 */
package com.example.myappproject;

import java.util.ArrayList;

import myview.CommentListView;
import myview.CommentListView.RefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.myappproject.WeiboListActivity.getData;
import com.example.sbean.ActivityStack;
import com.example.sbean.CacheManage;
import com.example.sbean.Status;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.net.RequestListener;

import adapter.CommentAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author 韶阳
 *
 */
public class DetialWeiboActivity extends Activity implements RefreshListener {
	private static final int REPLY = 1;
	ActivityStack stack;
	Long weiboId;
	CommentAdapter adapter;
	CommentListView listview;
	private long maxId = 0;

	Status sta;
	CommentsAPI api;
	StatusesAPI sapi;
	Oauth2AccessToken accessToken;
	ArrayList<Comment> list;
	TextView repost;
	TextView comment;
	int total = 0;

	Button confirmBt;
	Button cancelBt;
	RelativeLayout coverLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_detial);
		stack = ActivityStack.getInstance();
		stack.push(this);
		listview = (CommentListView) findViewById(R.id.comment_list);
		accessToken = AccessTokenKeeper.readAccessToken(this);
		api = new CommentsAPI(this, Constants.APP_KEY, accessToken);
		sapi = new StatusesAPI(this, Constants.APP_KEY, accessToken);
		list = new ArrayList<Comment>();

		repost = (TextView) findViewById(R.id.click_repost);
		comment = (TextView) findViewById(R.id.click_comment);
		repost.setOnClickListener(new Click());
		comment.setOnClickListener(new Click());

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);

		Intent intent = getIntent();
		try{
			sta = (Status) intent.getSerializableExtra("status");
		}catch (Exception e){
			Log.i("weibo", "异常");
		}

		weiboId = Long.parseLong(sta.id);
		listview.setStatus(sta);
		listview.setInterface(this);
		new CommentTask().execute("");
		list = new ArrayList<Comment>();
		total = sta.comments_count;
		setAdapter();

		confirmBt = (Button) findViewById(R.id.confirm);
		cancelBt = (Button) findViewById(R.id.cancel);
		coverLayout = (RelativeLayout) findViewById(R.id.cover);
		
		confirmBt.setOnClickListener(new DeleteListenner());
		cancelBt.setOnClickListener(new DeleteListenner());
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

	class CommentTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			onRefresh();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO 自动生成的方法存根
			super.onPostExecute(result);
		}
	}

	class RListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				list.clear();
				CommentList comments = CommentList.parse(response);
				if (comments != null && comments.total_number > 0
						&& comments.commentList != null) {
					list.addAll(comments.commentList);
					handler.sendEmptyMessage(0);
				} else {
					listview.refreshComplete();
				}
				if (list.size() > 1)
					maxId = Long.parseLong(list.get(list.size() - 1).id) - 1;
			}

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}

	// 加载更多监听类
	class RListener2 implements RequestListener {

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				CommentList comments = CommentList.parse(response);
				if (comments != null && comments.total_number > 0
						&& comments.commentList != null) {
					list.addAll(comments.commentList);
					handler.sendEmptyMessage(0);
				} else {
					listview.refreshComplete();
				}
				if (list.size() > 1)
					maxId = Long.parseLong(list.get(list.size() - 1).id) - 1;
			}

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				setAdapter();
			} else if (msg.what == 1) {
				Bundle b = msg.getData();
				int cc = b.getInt("comments");
				int rc = b.getInt("reposts");
				listview.updateCount(cc, rc);
			}
		}

	};

	private void setAdapter() {
		if (adapter == null) {
			adapter = new CommentAdapter(this, list);
			listview.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
		listview.refreshComplete();
	}

	@Override
	public void onRefresh() {
		sapi.count(new String[] { weiboId + "" }, new RequestListener() {
			@Override
			public void onComplete(String response) {
				try {
					JSONArray ja = new JSONArray(response);
					JSONObject jsonObject = ja.getJSONObject(0);
					int count = jsonObject.optInt("comments");
					int repost = jsonObject.optInt("reposts");

					Bundle b = new Bundle();
					b.putInt("comments", count);
					b.putInt("reposts", repost);
					Message m = new Message();
					m.what = 1;
					m.setData(b);
					handler.sendMessage(m);

					total = count;
				} catch (JSONException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
			}

		});
		api.show(weiboId, 0, 0, 50, 1, 0, new RListener());
	}

	@Override
	public void loadMore() {
		if (list.size() < total)
			api.show(weiboId, 0, maxId, 50, 1, 0, new RListener2());
		else {
			listview.refreshComplete();
			return;
		}
	};

	class Click implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.click_comment) {
				Intent intent = new Intent(DetialWeiboActivity.this,
						NewCommentActivity.class);
				intent.putExtra("weiboId", sta.id);
				DetialWeiboActivity.this.startActivityForResult(intent, 10);
			} else if (v.getId() == R.id.click_repost) {
				Status status = sta;
				Intent intent = new Intent(DetialWeiboActivity.this,
						NewRepostActivity.class);
				intent.putExtra("weiboId", status.id);
				intent.putExtra("rep_url", status.user.avatar_large);
				intent.putExtra("rep_name", status.user.name);
				intent.putExtra("rep_content", status.text);
				DetialWeiboActivity.this.startActivity(intent);
			}
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == REPLY) {
			onRefresh();
		}
	};

	@Override
	public void onBackPressed() {
		if (coverLayout.getVisibility() == View.VISIBLE)
			coverLayout.setVisibility(View.GONE);
		else
			super.onBackPressed();
	}

	class DeleteListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			switch (v.getId()) {
			case R.id.confirm:
				createNotify();
				sapi.destroy(weiboId, new RequestListener() {
					
					@Override
					public void onWeiboException(WeiboException arg0) {
						// TODO 自动生成的方法存根						
					}
					
					@Override
					public void onComplete(String arg0) {
						setResult(1);
						cancelNotify();
						stack.remove(DetialWeiboActivity.this);
					}
				});
				break;
			case R.id.cancel:
				coverLayout.setVisibility(View.GONE);
				break;
			}
		}

	}
	
	// 取消掉通知
	private void cancelNotify() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
	}

	// 产生通知
	private void createNotify() {
		Notification not = new NotificationCompat.Builder(this)
				.setTicker("正在删除").setSmallIcon(R.drawable.my_icon)
				.setContentTitle("fakeweibo").setContentText("删除中")
				.setAutoCancel(true).build();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(1, not);
	}
}
