package adapter;

import java.util.ArrayList;

import com.example.myappproject.ImageDetailFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {
	private ArrayList<String> list;
	public ImagePagerAdapter(FragmentManager fm,ArrayList<String> list) {
		super(fm);
		this.list=list;
	}

	@Override
	public Fragment getItem(int position) {
		String url = list.get(position);
		return ImageDetailFragment.newInstance(url);
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

}
