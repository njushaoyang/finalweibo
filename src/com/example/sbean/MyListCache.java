package com.example.sbean;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;


public class MyListCache implements Serializable {
	public long myTimeStamp;
	private ArrayList<Status> myList;
	private long cacheTime = 3600000; // 缓存的有效时间
	private String uid;

	public void setMyList(ArrayList<Status> list,String uid) {
		this.myList = list;
		myTimeStamp = System.currentTimeMillis();
		this.uid=uid;
	}


	public ArrayList<Status> getMyList() {
		long time = System.currentTimeMillis();
		if (time - myTimeStamp > cacheTime) {
			// 缓存失效
			Log.i("weibo", "my缓存失效 "+(time - myTimeStamp));
			myList = null;
		}
		return myList;
	}
	public String getUid(){
		return uid;
	}
}
