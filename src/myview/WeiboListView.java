package myview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.example.myappproject.AccessTokenKeeper;
import com.example.myappproject.Constants;
import com.example.myappproject.MyApplication;
import com.example.myappproject.R;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.TextView;

;
public class WeiboListView extends ListView implements OnScrollListener {

	View header;
	public View header2;
	View footer;
	int headerHeight;
	RefreshListener refreshListener;
	int firstVisibleItem;// 当前第一个可见的item
	int lastVisibleItem;// 最后一个可见的item
	int totalItemCount;
	boolean isLoading;

	int scrollState; // listview的滚动状态
	boolean isRemark;// 是否在listview最顶端按下的
	int startY;// 按下的y坐标值

	int state;
	final int NONE = 0;
	final int PULL = 1;
	final int RELEASE = 2;
	final int REFRESHING = 3;
	boolean userhome = false;

	public WeiboListView(Context context) {
		super(context);
		init(context);
	}

	public WeiboListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WeiboListView(Context context, AttributeSet attrs, int defstyle) {
		super(context, attrs, defstyle);
		init(context);
	}

	public void init(final Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.header_layout, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		topPadding(-headerHeight);
		this.addHeaderView(header);

		LayoutInflater inflater2 = LayoutInflater.from(context);
		header2 = inflater2.inflate(R.layout.header2_layout, null);
		this.addHeaderView(header2);
		// 加载用户头像
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(context);
		long uid = Long.parseLong(token.getUid());

		// 使用已经保存的信息
		if (MyApplication.getInstance().user != null) {
			setUserHeader(MyApplication.getInstance().user);
		} else {

			// 如果没有 ，那么使用新信息
			UsersAPI uapi = new UsersAPI(context, Constants.APP_KEY, token);
			uapi.show(uid, new RequestListener() {

				@Override
				public void onWeiboException(WeiboException e) {
					Toast.makeText(context, "无网络连接，请检查", Toast.LENGTH_LONG)
							.show();
				}

				@Override
				public void onComplete(String response) {
					if (!TextUtils.isEmpty(response)) {
						User user = User.parse(response);
						MyApplication.getInstance().setUsr(user);
						setUserHeader(user);
					}
				}
			});
		}

		LayoutInflater inflater3 = LayoutInflater.from(context);
		footer = inflater3.inflate(R.layout.footer_layout, null);
		footer.setVisibility(View.GONE);
		this.addFooterView(footer);

		setOnScrollListener(this);
	}

	// 设置用户的头像
	private void setUserHeader(User user) {
		String url = user.avatar_large;
		ImageView iv = (ImageView) header2.findViewById(R.id.user_head);
		TextView tv = (TextView) header2.findViewById(R.id.user_name);
		tv.setText(user.name);

		com.nostra13.universalimageloader.core.ImageLoader.getInstance()
				.displayImage(url, iv, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						// TODO 自动生成的方法存根
						super.onLoadingComplete(imageUri, view, loadedImage);
					}
				});

	}

	private void topPadding(int topPadding) {
		header.setPadding(header.getPaddingLeft(), topPadding,
				header.getPaddingRight(), header.getPaddingBottom());
		header.invalidate();
	}

	// 通知父布局大小
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
		if (totalItemCount == lastVisibleItem
				&& scrollState == SCROLL_STATE_IDLE) {
			if (!isLoading) {
				isLoading = true;
				footer.setVisibility(View.VISIBLE);
				// 加载数据
				refreshListener.loadMore();
				Timer timer = new Timer();
				timer.schedule(new cancelTask(), 10000);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO 自动生成的方法存根
		this.firstVisibleItem = firstVisibleItem;
		this.lastVisibleItem = firstVisibleItem + visibleItemCount;
		this.totalItemCount = totalItemCount;
	}


	// dispatch阶段尝试处理
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRemark = true;
				startY = (int) ev.getY();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			if (state == RELEASE) {
				state = REFRESHING;
				// 加载最新数据
				refreshViewByState();
				refreshListener.onRefresh();
			} else if (state == PULL) {
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	// 判断移动过程操作
	private void onMove(MotionEvent ev) {
		if (!isRemark)
			return;
		int tempY = (int) ev.getY();
		int space = tempY - startY;
		int topPadding = space - headerHeight;

		switch (state) {
		case NONE:
			if (space > 0) {
				state = PULL;
				refreshViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding);
			if (space > headerHeight + 30
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELEASE;
				refreshViewByState();
			}
			break;
		case RELEASE:
			topPadding(topPadding);
			if (space < headerHeight + 30) {
				state = PULL;
				refreshViewByState();
			} else if (space <= 0) {
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;
		}

	}

	private void refreshViewByState() {
		TextView tip = (TextView) header.findViewById(R.id.tip);
		ImageView arrow = (ImageView) header.findViewById(R.id.arrow);
		ProgressBar progress = (ProgressBar) header.findViewById(R.id.progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);
		switch (state) {
		case NONE:
			arrow.clearAnimation();
			topPadding(-headerHeight);
			break;

		case PULL:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("下拉可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELEASE:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("松开可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFRESHING:
			topPadding(50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			Timer timer = new Timer();
			timer.schedule(new cancelTask(), 10000);
			break;
		}
	}

	// 通知listview加载完毕
	public void refreshComplete() {
		if (isLoading) {
			isLoading = false;
			footer.setVisibility(View.GONE);
			//return;
		}
		state = NONE;
		isRemark = false;
		refreshViewByState();
		TextView lastupdatetime = (TextView) header
				.findViewById(R.id.lastupdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月 HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastupdatetime.setText(time);
	}

	public void setInterface(RefreshListener iReflashListener) {
		this.refreshListener = iReflashListener;
	}

	public interface RefreshListener {
		public void onRefresh();

		public void loadMore();
	}

	class cancelTask extends TimerTask {

		@Override
		public void run() {
			handler.sendEmptyMessage(101);
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what==101){
				Log.i("weibo","refresh");
				refreshComplete();
			}
		}
	};
}
