package myview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import myview.WeiboListView.cancelTask;

import com.example.myappproject.DetialWeiboActivity;
import com.example.myappproject.ImagePagerActivity;
import com.example.myappproject.MyApplication;
import com.example.myappproject.R;
import com.example.myappproject.R.id;
import com.example.myappproject.R.layout;
import com.example.sbean.Span;
import com.example.sbean.Status;
import com.example.sbean.WeiboTime;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import adapter.GridViewAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class CommentListView extends ListView implements OnScrollListener {
	View header;
	View header2;
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
	Context context;
	Status sta;

	public CommentListView(Context context) {
		super(context);
		init(context);
	}

	public CommentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CommentListView(Context context, AttributeSet attrs, int defstyle) {
		super(context, attrs, defstyle);
		init(context);
	}

	public void setStatus(Status sta) {
		this.sta = sta;
		setUserHeader();
	}

	public void init(final Context context) {
		this.context = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.header_layout, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		topPadding(-headerHeight);
		this.addHeaderView(header);

		LayoutInflater inflater2 = LayoutInflater.from(context);
		header2 = inflater2.inflate(R.layout.weibo_detial_header, null);
		this.addHeaderView(header2);
		LayoutInflater inflater3 = LayoutInflater.from(context);
		footer = inflater3.inflate(R.layout.footer_layout, null);
		footer.setVisibility(View.GONE);
		this.addFooterView(footer);

		setOnScrollListener(this);
	}

	private void setUserHeader() {
		if (sta == null) {
			Log.i("weibo", "null");
			return;
		}
		TextView username = (TextView) header2.findViewById(R.id.user_name);
		TextView time = (TextView) header2.findViewById(R.id.create_time);
		TextView content = (TextView) header2.findViewById(R.id.weibo_content);
		TextView rep_name = (TextView) header2.findViewById(R.id.forward_name);
		TextView rep_content = (TextView) header2
				.findViewById(R.id.forward_content);
		TextView rep_count = (TextView) header2
				.findViewById(R.id.forward_count);
		TextView comment_count = (TextView) header2
				.findViewById(R.id.comment_count);

		ImageView head = (ImageView) findViewById(R.id.user_head);
		MyGridView gridview = (MyGridView) findViewById(R.id.gridview);
		MyGridView rep_gridview = (MyGridView) findViewById(R.id.forward_gridview);
		LinearLayout rep_layout = (LinearLayout) findViewById(R.id.forward_area);

		ImageLoader.getInstance().displayImage(sta.user.avatar_large, head,
				new SimpleImageLoadingListener());
		username.setText(sta.user.name);
		time.setText(WeiboTime.getDetialTime(sta.created_at));
		content.setText(Span.changeName(sta.text));
		rep_count.setText(sta.reposts_count + "");
		comment_count.setText(sta.comments_count + "");
		final ArrayList<String> picurls = sta.pic_urls;
		if (picurls != null && picurls.size() > 0) {
			for (int j = 0; j < picurls.size(); j++) {
				picurls.set(j, picurls.get(j).replace("thumbnail", "bmiddle"));
			}
			gridview.setAdapter(new GridViewAdapter(context, picurls));
			gridview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					imageBrower(position, picurls);
				}
			});
		}
		// 删除按钮
		if (MyApplication.getInstance().user != null
				&& MyApplication.getInstance().user.id.equals(sta.user.id)) {
			TextView tv = (TextView) header2.findViewById(R.id.delete);
			tv.setVisibility(View.VISIBLE);
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 自动生成的方法存根
					((DetialWeiboActivity) context).findViewById(R.id.cover)
							.setVisibility(View.VISIBLE);
				}
			});
		} else {
			TextView tv = (TextView) header2.findViewById(R.id.delete);
			tv.setVisibility(View.GONE);
		}
		// 转发部分
		if (sta.retweeted_status == null) {
			rep_layout.setVisibility(View.GONE);
			return;
		}
		Status rep = sta.retweeted_status;
		if (rep.user == null) {
			rep_name.setText("抱歉");
			rep_content.setText(Span.changeName("您查看的微博不存在"));
			return;
		}
		rep_name.setText(rep.user.name);
		rep_content.setText(Span.changeName(rep.text));
		rep_content.setOnClickListener(new ClickListener(rep));
		final ArrayList<String> rpicurls = rep.pic_urls;
		if (rpicurls != null && rpicurls.size() > 0) {
			for (int j = 0; j < rpicurls.size(); j++) {
				rpicurls.set(j, rpicurls.get(j).replace("thumbnail", "bmiddle"));
			}
			rep_gridview.setAdapter(new GridViewAdapter(context, rpicurls));
			rep_gridview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					imageBrower(position, rpicurls);
				}
			});
		}
	}

	private void imageBrower(int position, ArrayList<String> picurls) {
		Intent intent = new Intent(context, ImagePagerActivity.class);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, picurls);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
		context.startActivity(intent);
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

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO 自动生成的方法存根
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
		return super.onTouchEvent(ev);
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

	public void updateCount(int cc, int rc) {
		TextView rep_count = (TextView) header2
				.findViewById(R.id.forward_count);
		TextView comment_count = (TextView) header2
				.findViewById(R.id.comment_count);
		rep_count.setText(rc + "");
		comment_count.setText(cc + "");
	}

	class ClickListener implements OnClickListener {
		Status sta;

		public ClickListener(Status sta) {
			this.sta = sta;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, DetialWeiboActivity.class);
			intent.putExtra("status", sta);
			((DetialWeiboActivity) context).startActivity(intent);
		}

	}

	class cancelTask extends TimerTask {

		@Override
		public void run() {
			handler.sendEmptyMessage(102);
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 102) {
				refreshComplete();
			}
		}
	};
}
