package com.example.myappproject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sina.weibo.sdk.openapi.models.User;

import android.app.Application;
import android.graphics.Bitmap;

public class MyApplication extends Application {
	private static MyApplication app;
	public User user;
	public static MyApplication getInstance() {  
        return app;  
    } 
	@Override
	public void onCreate() {
		super.onCreate();
		app=this;
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder() //
				.showImageForEmptyUri(R.drawable.white) //
				.showImageOnFail(R.drawable.white) //
				.cacheInMemory(true) //
				.cacheOnDisk(true) //
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();//
		ImageLoaderConfiguration config = new ImageLoaderConfiguration//
		.Builder(getApplicationContext())//
				.defaultDisplayImageOptions(defaultOptions)//
				.discCacheSize(100 * 1024 * 1024)//
				.discCacheFileCount(100)// 缓存一百张图片
				.writeDebugLogs()//
				.build();//
		
		ImageLoader.getInstance().init(config);
	}
	public void setUsr(User user){
		this.user=user;
	}
}
