package adapter;

import java.util.ArrayList;

import com.example.myappproject.R;
import com.example.myappproject.R.id;
import com.example.myappproject.R.layout;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class GridViewAdapter extends BaseAdapter {
	private ArrayList<String> list;
	Context context;
	LayoutInflater mInflater;

	public GridViewAdapter(Context c, ArrayList<String> list) {
		context = c;
		this.list = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO 自动生成的方法存根
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder vh;
		if (convertView == null) {
			vh = new Holder();
			convertView = mInflater.inflate(R.layout.pic_item, null);
			vh.image = (ImageView) convertView.findViewById(R.id.url_image);

			convertView.setTag(vh);
		} else {
			vh = (Holder) convertView.getTag();
		}
		// 加载图片
		String url = list.get(position);

		com.nostra13.universalimageloader.core.ImageLoader.getInstance()
				.displayImage(url, vh.image, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
						case IO_ERROR:
							message = "下载错误";
							break;
						case DECODING_ERROR:
							message = "图片无法显示";
							break;
						case NETWORK_DENIED:
							message = "网络有问题，无法下载";
							break;
						case OUT_OF_MEMORY:
							message = "图片太大无法显示";
							break;
						case UNKNOWN:
							message = "未知的错误";
							break;
						}
					}
					
				});

		return convertView;
	}

	public class Holder {
		public ImageView image;
	}

}
