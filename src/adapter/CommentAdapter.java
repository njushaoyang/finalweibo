package adapter;

import java.util.ArrayList;

import com.example.myappproject.R;
import com.example.myappproject.R.id;
import com.example.myappproject.R.layout;
import com.example.sbean.Span;
import com.example.sbean.WeiboTime;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sina.weibo.sdk.openapi.models.Comment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter {
	private ArrayList<Comment> list;
	private LayoutInflater mInflater;
	Context context;

	public CommentAdapter(Context c, ArrayList<Comment> list) {
		this.list = list;
		this.context = c;
		mInflater = LayoutInflater.from(context);
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
		ViewHolder vh;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.comment_item, null);
			vh = new ViewHolder();
			vh.head = (ImageView) convertView.findViewById(R.id.comment_head);
			vh.name = (TextView) convertView.findViewById(R.id.comment_name);
			vh.time = (TextView) convertView.findViewById(R.id.comment_time);
			vh.content = (TextView) convertView
					.findViewById(R.id.comment_content);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Comment cm = list.get(position);
		vh.name.setText(cm.user.name);
		vh.time.setText(WeiboTime.getDetialTime(cm.created_at));
		ImageLoader.getInstance().displayImage(cm.user.avatar_large, vh.head,
				new SimpleImageLoadingListener());
		vh.content.setText(Span.changeName(cm.text));
		return convertView;
	}

	class ViewHolder {
		ImageView head;
		TextView name;
		TextView time;
		TextView content;
	}

}
