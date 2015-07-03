package adapter;

import java.util.ArrayList;
import java.util.List;

import myview.MyGridView;

import com.example.myappproject.DetialWeiboActivity;
import com.example.myappproject.ImagePagerActivity;
import com.example.myappproject.NewCommentActivity;
import com.example.myappproject.NewRepostActivity;
import com.example.myappproject.R;
import com.example.myappproject.WeiboListActivity;
import com.example.myappproject.R.id;
import com.example.myappproject.R.layout;
import com.example.sbean.Span;
import com.example.sbean.Status;
import com.example.sbean.WeiboTime;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewAdapter extends BaseAdapter {
	List<Status> list;
	private LayoutInflater mInflater;
	Context c;

	public ViewAdapter(Context context, List<Status> list) {
		c = context;
		mInflater = LayoutInflater.from(context);
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder vh;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item, null);

			vh.name = (TextView) convertView.findViewById(R.id.username);
			vh.headerImage = (ImageView) convertView
					.findViewById(R.id.user_image);
			vh.content = (TextView) convertView
					.findViewById(R.id.weibo_content);
			vh.mg = (MyGridView) convertView.findViewById(R.id.gridview);
			vh.time = (TextView) convertView.findViewById(R.id.create_time);
			vh.forward = (TextView) convertView
					.findViewById(R.id.forward_a_weibo);
			vh.comment = (TextView) convertView
					.findViewById(R.id.comment_a_weibo);

			// 转发内容
			vh.t_name = (TextView) convertView.findViewById(R.id.forward_name);
			vh.t_content = (TextView) convertView
					.findViewById(R.id.forward_content);
			vh.t_mg = (MyGridView) convertView
					.findViewById(R.id.forward_gridview);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		Status ib = list.get(position);
		// 加载图片
		vh.forward.setOnClickListener(new ClickRepost(list.get(position)));
		vh.comment.setOnClickListener(new ClickComment(list.get(position)));
		vh.content.setOnClickListener(new ClickContent(list.get(position)));

		vh.name.setText(ib.user.name);
		vh.content.setText(Span.changeName(ib.text));
		vh.time.setText(WeiboTime.format(ib.created_at));

		com.nostra13.universalimageloader.core.ImageLoader.getInstance()
				.displayImage(ib.user.avatar_large, vh.headerImage,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								// TODO 自动生成的方法存根
								super.onLoadingComplete(imageUri, view,
										loadedImage);
							}
						});

		final ArrayList<String> ulist = ib.pic_urls;
		GridViewAdapter gd = new GridViewAdapter(c, ulist);
		vh.mg.setAdapter(gd);
		if (ulist != null && ulist.size() > 0)
			vh.mg.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					imageBrower(position, ulist);
				}
			});

		if (ib.retweeted_status != null) {
			vh.t_name.setVisibility(View.VISIBLE);
			vh.t_content.setVisibility(View.VISIBLE);
			Status sta = ib.retweeted_status;
			final ArrayList<String> r_list = sta.pic_urls;
			if (r_list == null || r_list.size() == 0) {
				vh.t_mg.setVisibility(View.GONE);
			} else {
				vh.t_mg.setVisibility(View.VISIBLE);
				for (int j = 0; j < r_list.size(); j++) {
					r_list.set(j, r_list.get(j).replace("thumbnail", "bmiddle"));
				}
				GridViewAdapter gd2 = new GridViewAdapter(c, r_list);
				vh.t_mg.setAdapter(gd2);
				vh.t_mg.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						imageBrower(position, r_list);
					}
				});
			}
			if (ib.retweeted_status.user == null) {
				vh.t_name.setText("抱歉");
				vh.t_content.setText(Span.changeName(ib.retweeted_status.text));
				vh.t_content.setOnClickListener(null);
			} else {
				vh.t_name.setText(ib.retweeted_status.user.name);
				vh.t_content.setText(Span.changeName(ib.retweeted_status.text));

				vh.t_content.setOnClickListener(new ClickContent(list
						.get(position)));
			}

		} else {
			vh.t_name.setVisibility(View.GONE);
			vh.t_content.setVisibility(View.GONE);
			vh.t_mg.setVisibility(View.GONE);
		}
		return convertView;
	}

	public class ViewHolder {
		public TextView name;
		public ImageView headerImage;
		public TextView content;
		public MyGridView mg;

		public TextView t_name;
		public TextView t_content;
		public MyGridView t_mg;

		public TextView time;
		public TextView forward;
		public TextView comment;
	}

	private void imageBrower(int position, final ArrayList<String> imageurl) {
		// TODO 自动生成的方法存根
		Intent intent = new Intent(c, ImagePagerActivity.class);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, imageurl);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
		c.startActivity(intent);
	}

	public class ClickComment implements OnClickListener {
		Status status;

		public ClickComment(Status sta) {
			this.status = sta;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(c, NewCommentActivity.class);
			intent.putExtra("weiboId", status.id);
			((WeiboListActivity) c).startActivityForResult(intent, 0);
		}
	}

	public class ClickRepost implements OnClickListener {
		Status status;

		public ClickRepost(Status sta) {
			this.status = sta;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(c, NewRepostActivity.class);
			intent.putExtra("weiboId", status.id);
			intent.putExtra("rep_url", status.user.avatar_large);
			intent.putExtra("rep_name", status.user.name);
			intent.putExtra("rep_content", status.text);
			((WeiboListActivity) c).startActivityForResult(intent, 0);
		}
	}

	public class ClickContent implements OnClickListener {
		Status status;

		public ClickContent(Status sta) {
			this.status = sta;
		}

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			if (v.getId() == R.id.weibo_content) {
				Intent intent = new Intent(c, DetialWeiboActivity.class);
				intent.putExtra("status", status);
				((WeiboListActivity) c).startActivityForResult(intent, 10);;
			} else if (v.getId() == R.id.forward_content) {
				Intent intent = new Intent(c, DetialWeiboActivity.class);
				intent.putExtra("status", status.retweeted_status);
				((WeiboListActivity) c).startActivityForResult(intent, 10);;
			}
		}

	}

}
