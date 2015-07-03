/**
 * 
 */
package com.example.myappproject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.sbean.ActivityStack;

import adapter.ImagePagerAdapter;
import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 韶阳
 *
 */
public class ImagePagerActivity extends FragmentActivity {
	private HackyViewPager pager;
	private TextView indicator;
	private Button buttonDownload;
	private int pagerPosition;  
	private ActivityStack stack;
	private static final String STATE_POSITION = "STATE_POSITION";  
    public static final String EXTRA_IMAGE_INDEX = "image_index";  
    public static final String EXTRA_IMAGE_URLS = "image_urls";
	private static final String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/fakeweibo/";
	private ArrayList<String> urls;
	private String mSaveMessage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_layout);
		ActionBar ab=getActionBar();
		if(ab!=null)
		ab.setDisplayShowHomeEnabled(false);
		
		stack=ActivityStack.getInstance();
		stack.push(this);
		indicator=(TextView) findViewById(R.id.indicator);
		pagerPosition=getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		urls=getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
		pager=(HackyViewPager) findViewById(R.id.pager);
		
		ImagePagerAdapter mAdapter=new ImagePagerAdapter(getSupportFragmentManager(), urls);
		pager.setAdapter(mAdapter);
		
		
		String text=getString(R.string.viewpager_indicator, 1, pager.getAdapter().getCount());
		indicator.setText(text);

		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO 自动生成的方法存根
				CharSequence text = getString(R.string.viewpager_indicator,  
                        arg0 + 1,pager.getAdapter().getCount());  
                indicator.setText(text); 
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO 自动生成的方法存根
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO 自动生成的方法存根
				
			}
		});
		if (savedInstanceState != null) {  
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);  
        }  
  
        pager.setCurrentItem(pagerPosition);
        //////////////////////////////////////////////
        buttonDownload=(Button) findViewById(R.id.button_download);
        buttonDownload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(saveFileRunnable).start();
			}
		});
        //////////////////////////////////////////////
	}
	//////////////////////////////////////////////
	private Handler messageHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
//            Log.d(TAG, mSaveMessage);  
            Toast.makeText(ImagePagerActivity.this, mSaveMessage, Toast.LENGTH_SHORT).show();  
        }  
    };  
	private Runnable saveFileRunnable=new Runnable() {
		
		@Override
		public void run() {
			
			Bitmap bitmap;
			String urlName=urls.get(pager.getCurrentItem());
			String filename=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".jpg";
			try{
				bitmap=BitmapFactory.decodeStream(getImageStream(urlName));
				saveFile(bitmap,filename);
				mSaveMessage="图片保存成功!";
			}catch(Exception e){
				mSaveMessage="图片保存失败!";
				e.printStackTrace();
			}
//			Toast.makeText(ImagePagerActivity.this, mSaveMessage, Toast.LENGTH_SHORT).show();
			messageHandler.sendMessage(messageHandler.obtainMessage());
		}
	};
    private InputStream getImageStream(String path) throws Exception{  
        URL url = new URL(path);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        conn.setConnectTimeout(5 * 1000);  
        conn.setRequestMethod("GET");  
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){  
            return conn.getInputStream();  
        }  
        return null;  
    }   
	public void saveFile(Bitmap bm, String fileName) throws IOException {
		if(bm==null||TextUtils.isEmpty(fileName))
			throw new IOException();
        File dirFile = new File(ALBUM_PATH);  
        if(!dirFile.exists()){  
            dirFile.mkdir();  
        }  
        File myCaptureFile = new File(ALBUM_PATH + fileName);  
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));  
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);  
        bos.flush();  
        bos.close();  
    }  

	/////////////////////////////////////////////
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO 自动生成的方法存根
		outState.putInt(STATE_POSITION, pager.getCurrentItem());  
	}
	
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		stack.remove(this);
		super.onDestroy();
	}
}
