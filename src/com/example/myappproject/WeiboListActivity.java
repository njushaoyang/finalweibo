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

import com.example.sbean.ActivityStack;
import com.example.sbean.CacheManage;
import com.example.sbean.FriendListCache;
import com.example.sbean.Status;
import com.example.sbean.StatusList;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

import adapter.ViewAdapter;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * @author 韶阳
 *
 */
public class WeiboListActivity extends FragmentActivity implements
		RefreshListener, OnItemClickListener,OnShowcaseEventListener {

	protected static final String DIG_TAG = "dialog";
	private Oauth2AccessToken accessToken;
	private WeiboListView lv;
	private ArrayList<Status> alist;
	private ViewAdapter vadapter2;
	private StatusesAPI sapi;
	private int totalWeibo;
	private final int MOREWEIBO = 30;
	private CacheManage cacheManage;
	private FriendListCache cache;
	private long maxId = 0;
	private final int REQUEST = 0;
	private final int REPLY = 1;
	ActivityStack stack;
	private ShowcaseView sv=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		stack = ActivityStack.getInstance();
		stack.push(this);
		setContentView(R.layout.home_layout);
		lv = (WeiboListView) findViewById(R.id.listView);
		alist = new ArrayList<Status>();
		accessToken = AccessTokenKeeper.readAccessToken(this);
		final String uid = accessToken.getUid();
		sapi = new StatusesAPI(this, Constants.APP_KEY, accessToken);

		ActionBar ab =  getActionBar();
		if(ab!=null){
		ab.setDisplayShowHomeEnabled(false);
		}
		cacheManage = CacheManage.getInstance(this);

		setAdapter();

		initData();
		lv.setInterface(this);
		lv.header2.findViewById(R.id.user_head).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(WeiboListActivity.this,
								UserHomeActivity.class);
						intent.putExtra("id", uid);
						startActivity(intent);
					}
				});
		lv.setOnItemClickListener(this);
		handleFirst();
	}

	private void initData() {
		// 从缓存文件中提取数据
		cache = cacheManage.readFriend(accessToken.getUid());
		if (cache != null && cache.getMyList() != null
				&& cache.getMyList().size() > 0) {
			alist.clear();
			alist.addAll(cache.getMyList());
			maxId = Long.parseLong(alist.get(alist.size() - 1).id) - 1;
		} else {
			new getData().execute("");
		}

		File file = getFilesDir();
		File f = new File(file, GetImageActivity.BACKGROUND_IMAGE);
		if (!f.exists())
			return;
		BufferedInputStream bs;
		try {
			bs = new BufferedInputStream(new FileInputStream(f));
			Bitmap btp = BitmapFactory.decodeStream(bs);
			ImageView iv = (ImageView) lv.header2.findViewById(R.id.background);
			iv.setImageBitmap(btp);
			bs.close();
			btp = null;
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	public void setAdapter() {
		if (vadapter2 == null) {
			vadapter2 = new ViewAdapter(this, alist);
			lv.setAdapter(vadapter2);
			vadapter2.notifyDataSetChanged();
		} else {
			vadapter2.notifyDataSetChanged();
			lv.refreshComplete();
		}
	}

	@Override
	public void onRefresh() {
		// 获取最新数据，通知界面显示，通知界面显示
		sapi.friendsTimeline(0, 0, MOREWEIBO, 1, false, 0, false,
				new RListenner2());
	}

	@Override
	public void loadMore() {
		// 获取更多数据，通知界面显示
		if (totalWeibo >= 100) {
			Toast.makeText(this, "已经没有更多的微博了", Toast.LENGTH_SHORT).show();
			lv.refreshComplete();
			return;
		}
		sapi.friendsTimeline(0, maxId, MOREWEIBO, 1, false, 0, false,
				new RListenner());
	}

	public class getData extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			sapi.friendsTimeline(0, 0, MOREWEIBO, 1, false, 0, false,
					new RListenner2());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	// 全部刷新的监听类
	class RListenner2 implements RequestListener {

		@Override
		public void onComplete(String response) {
			Log.i("weibo", "Friend 执行一次网络访问");
			if (response.startsWith("{\"statuses\"")) {
				alist.clear();
				StatusList res = StatusList.parse(response);

				if (res != null && res.total_number > 0) {

					ArrayList<Status> list = res.statusList;
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
						alist.add(sta);
					}
					totalWeibo = list.size();
					handler.sendEmptyMessage(0);

					if (cache == null) {
						cache = new FriendListCache();
					}
					cache.setMyList(alist, accessToken.getUid());
					cacheManage.writeFriend(cache);
				} else
					lv.refreshComplete();
				if (alist.size() > 1)
					maxId = Long.parseLong(alist.get(alist.size() - 1).id) - 1;
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}

	// 加载更多的监听类
	class RListenner implements RequestListener {

		@Override
		public void onComplete(String response) {
			Log.i("weibo", "Friend append 执行一次网络访问");
			if (response.startsWith("{\"statuses\"")) {
				StatusList res = StatusList.parse(response);

				if (res != null && res.total_number > 0) {

					ArrayList<Status> list = res.statusList;
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
						alist.add(sta);
					}
					totalWeibo = alist.size();
					handler.sendEmptyMessage(0);

					if (cache == null) {
						cache = new FriendListCache();
					}
					cache.setMyList(alist, accessToken.getUid());
					cacheManage.writeFriend(cache);
				} else{
					Toast.makeText(WeiboListActivity.this, "已经没有更多的微博了", Toast.LENGTH_SHORT).show();
					lv.refreshComplete();
				}
				if (alist.size() > 1)
					maxId = Long.parseLong(alist.get(alist.size() - 1).id) - 1;
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO 自动生成的方法存根

		}
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater mf = getMenuInflater();
		mf.inflate(R.menu.weibo_menu, menu);
		return super.onCreateOptionsMenu(menu);
	};

	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Log.i("weibo", "新微博");
			Intent intent = new Intent(this, NewWeiboActivity.class);
			startActivityForResult(intent, REQUEST);
			break;
		case R.id.setting:
			Log.i("weibo", "设置");
			Intent intent2 = new Intent(this, UserInfoActivity.class);
			startActivity(intent2);
		}
		return false;
	};

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		stack.remove(this);
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == REPLY) {
			// 通知缓存失效并且刷新界面
			CacheManage.notifyInvalid();
			Log.i("weibo", "开始刷新");
			new getData().execute("");
		}
		if (requestCode == 11 && resultCode == 12) {
			File file = getFilesDir();
			File f = new File(file, GetImageActivity.BACKGROUND_IMAGE);
			if (!f.exists())
				return;
			BufferedInputStream bs;
			try {
				Log.i("weibo", "success");
				bs = new BufferedInputStream(new FileInputStream(f));
				Bitmap btp = BitmapFactory.decodeStream(bs);
				ImageView iv = (ImageView) lv.header2
						.findViewById(R.id.background);
				iv.setImageBitmap(btp);
				bs.close();
				btp = null;
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0)
				setAdapter();
		};
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (id < 0)
			return;
		Intent intent = new Intent(this, DetialWeiboActivity.class);
		intent.putExtra("status", alist.get((int) id));
		startActivity(intent);
	}
	
	class ChangeImageListenner implements OnClickListener {
		@Override
		public void onClick(View v) {
			final RelativeLayout co = (RelativeLayout) findViewById(R.id.cover);
			co.setVisibility(View.VISIBLE);
			co.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 自动生成的方法存根
					if (co.getVisibility() == View.VISIBLE)
						co.setVisibility(View.GONE);
				}
			});
			Button bt = (Button) findViewById(R.id.chang_image);
			bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 自动生成的方法存根
					Log.i("weibo", "button");
					// 跳转 并且startActivityForResult
					Intent intent = new Intent(WeiboListActivity.this,
							GetImageActivity.class);
					startActivityForResult(intent, 11);
					RelativeLayout co = (RelativeLayout) findViewById(R.id.cover);
					co.setVisibility(View.GONE);
				}
			});
		}
	}
	private void handleFirst() {
		if (checkFirst()) {
			showCaseOne();
		}
	}
	private void showCaseOne() {
		//使用初次提示啊.
		ViewTarget target=new ViewTarget(R.id.user_head,this);
		sv=new ShowcaseView.Builder(this)
			.setTarget(target)
			.setContentTitle("Fake微博")
			.setContentText("点击跳转到我的微博界面")
			.setShowcaseEventListener(this)
			.build();
//        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
//        																ViewGroup.LayoutParams.WRAP_CONTENT);
//        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
//        lps.setMargins(margin, margin, margin, margin);
//		sv.setButtonPosition(lps);
	}
	private void showCaseTwo() {
		new ShowcaseView.Builder(this)
			.setContentTitle("Fake微博")
			.setContentText("编辑微博和我的信息界面")
			.setTarget(new ViewTarget(R.id.action_new, this))
			.setShowcaseEventListener(this)
			.build();
	}
	private void showCaseThree() {
		new ShowcaseView.Builder(this)
		.setContentTitle("Fake微博")
		.setContentText("点击进入微博正文")
		.setTarget(new ViewTarget(R.id.listView, this))
		.setShowcaseEventListener(this)
		.build();
	}
	private boolean checkFirst() {
		SharedPreferences preferences=getSharedPreferences("fakeweibo",Context.MODE_PRIVATE);
		if (preferences.getBoolean("firststart",true)) {
			SharedPreferences.Editor edit=preferences.edit();
			edit.putBoolean("firststart", false);
			edit.commit();
			return true;
		} else {
			return false;
		}
	}
	private int currentItem=1;
	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub
		
	}
	
	private int currentStatus=1;
	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub
		switch(currentItem) {
		case 1:
			showCaseTwo();
			break;
		case 2:
			showCaseThree();
			break;
		default:
			//doNothing
		}
		currentItem++;
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub
		
	}
}
