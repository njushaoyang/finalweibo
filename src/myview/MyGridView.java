package myview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class MyGridView extends GridView {

	public MyGridView(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
	}
	public MyGridView(Context context,AttributeSet as) {
		super(context,as);
		// TODO 自动生成的构造函数存根
	}
	public MyGridView(Context context,AttributeSet as,int a) {
		super(context,as,a);
		// TODO 自动生成的构造函数存根
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO 自动生成的方法存根
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
