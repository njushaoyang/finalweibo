/**
 * 
 */
package com.example.myappproject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import myview.WeiboListView;
import myview.WeiboListView.RefreshListener;

import com.example.myappproject.WeiboListActivity.getData;
import com.example.sbean.ActivityStack;
import com.example.sbean.CacheManage;
import com.example.sbean.FriendListCache;
import com.example.sbean.Status;
import com.example.sbean.StatusList;
import com.example.sbean.MyListCache;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

import adapter.UserListItemAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * @author 韶阳
 *
 */
public class UserHomeActivity extends Activity implements RefreshListener,
		OnItemClickListener {

	private Oauth2AccessToken accessToken;
	private WeiboListView wlv;
	private ArrayList<Status> slist;
	private StatusesAPI sapi;
	private int totalWeibo;
	private final int MOREWEIBO = 20;
	private UserListItemAdapter adapter;
	private Long uid;
	private CacheManage cacheManage;
	private MyListCache cache;
	private long maxId = 0;
	ActivityStack stack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);

		stack = ActivityStack.getInstance();
		stack.push(this);
		ActionBar ab = getActionBar();
		if(ab!=null){
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);
		}

		wlv = (WeiboListView) findViewById(R.id.listView);
		slist = new ArrayList<Status>();
		cacheManage = CacheManage.getInstance(this);

		accessToken = AccessTokenKeeper.readAccessToken(this);
		sapi = new StatusesAPI(this, Constants.APP_KEY, accessToken);
		Intent intent = getIntent();
		String id = intent.getStringExtra("id");
		uid = Long.parseLong(id);

		wlv.setInterface(this);
		wlv.setOnItemClickListener(this);
		setAdapter();

		wlv.header2.findViewById(R.id.user_head).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						Intent intent2 = new Intent(UserHomeActivity.this,
								UserInfoActivity.class);
						startActivity(intent2);
					}
				});
		initData();
	}

	private void initData() {
		// 从缓存文件中提取数据
		cache = cacheManage.readMine(uid + "");

		if (cache != null && cache.getMyList() != null
				&& cache.getMyList().size() > 0) {
			slist.clear();
			slist.addAll(cache.getMyList());
			maxId = Long.parseLong(slist.get(slist.size() - 1).id) - 1;
		} else {
			new getData().execute("");
		}
		
		File file = getFilesDir();
		File f = new File(file, GetImageActivity.BACKGROUND_IMAGE);
		if (!f.exists())
			return;
		BufferedInputStream bs;
		try {
			Log.i("weibo","success");
			bs = new BufferedInputStream(new FileInputStream(f));
			Bitmap btp = BitmapFactory.decodeStream(bs);
			ImageView iv = (ImageView) wlv.header2
					.findViewById(R.id.background);
			iv.setImageBitmap(btp);
			bs.close();
			btp = null;
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	public class RListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			Log.i("weibo", "在UserHome中执行一次网络访问");
			if (response.startsWith("{\"statuses\"")) {
				slist.clear();
				// 调用 StatusList#parse 解析字符串成微博列表对象
				StatusList res = StatusList.parse(response);

				if (res != null && res.total_number > 0) {
					ArrayList<Status> list = res.statusList;
					for (int i = 0; i < list.size(); i++) {
						Status sta = list.get(i);
						ArrayList<String> picurls = null;

						if (sta.pic_urls != null && sta.pic_urls.size() > 0) {
							picurls = sta.pic_urls;
							for (int j = 0; j < picurls.size(); j++) {
								picurls.set(
										j,
										picurls.get(j).replace("thumbnail",
												"bmiddle"));
							}
						} else {
							sta.pic_urls = new ArrayList<String>();
						}
						slist.add(sta);
					}
					totalWeibo = list.size();
					handler.sendEmptyMessage(0);

					if (cache == null) {
						cache = new MyListCache();
					}
					cache.setMyList(slist, uid + "");
					cacheManage.writeMine(cache);

					maxId = Long.parseLong(slist.get(slist.size() - 1).id) - 1;
				} else
					wlv.refreshComplete();
			} else
				wlv.refreshComplete();

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}

	private void setAdapter() {
		if (adapter == null) {
			adapter = new UserListItemAdapter(this, slist);
			wlv.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
			wlv.refreshComplete();
		}
	}

	@Override
	public void onRefresh() {
		sapi.userTimeline(uid, 0, 0, MOREWEIBO, 1, false, 0, false,
				new RListener());
	}

	@Override
	public void loadMore() {
		if (totalWeibo >= 100) {
			Toast.makeText(this, "已经没有更多的微博了", Toast.LENGTH_SHORT).show();
			wlv.refreshComplete();
			return;
		}
		sapi.userTimeline(uid, 0, maxId, MOREWEIBO, 1, false, 0, false,
				new RListenner2());

	}

	public class getData extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			sapi.userTimeline(uid, 0, 0, MOREWEIBO, 1, false, 0, false,
					new RListener());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			stack.remove(this);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0)
				setAdapter();
		};
	};

	// 加载更多的监听类
	class RListenner2 implements RequestListener {

		@Override
		public void onComplete(String response) {
			Log.i("weibo", "user append 执行一次网络访问");
			if (response.startsWith("{\"statuses\"")) {
				StatusList res = StatusList.parse(response);
				if (res != null && res.total_number > 0) {
					ArrayList<Status> list = res.statusList;
					Log.i("weibo", "" + list.size());
					for (int i = 0; i < list.size(); i++) {
						Status sta = list.get(i);
						ArrayList<String> picurls = null;
						if (sta.pic_urls != null) {
							picurls = sta.pic_urls;
							for (int j = 0; j < picurls.size(); j++) {
								picurls.set(
										j,
										picurls.get(j).replace("thumbnail",
												"bmiddle"));
							}
						} else
							sta.pic_urls = new ArrayList<String>();
						slist.add(sta);
					}
					totalWeibo = slist.size();
					handler.sendEmptyMessage(0);
					if (cache == null) {
						cache = new MyListCache();
					}
					cache.setMyList(slist, uid + "");
					cacheManage.writeMine(cache);
				} else {
					Toast.makeText(UserHomeActivity.this, "已经没有更多的微博了", Toast.LENGTH_SHORT).show();
					wlv.refreshComplete();
				}
				maxId = Long.parseLong(slist.get(slist.size() - 1).id) - 1;
				return;
			}
			wlv.refreshComplete();
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(id<0)
			return;
		Intent intent = new Intent(this, DetialWeiboActivity.class);
		intent.putExtra("status", slist.get((int) id));
		startActivity(intent);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			// 通知缓存失效并且刷新界面
			CacheManage.notifyInvalid();
			Log.i("weibo", "开始刷新");
			new getData().execute("");
		}
	}
}