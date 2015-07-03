package com.example.sbean;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

public class FriendListCache implements Serializable{
	public long friendTimeStamp;
	private ArrayList<Status> myList;
	private long cacheTime = 3600000; // 缓存的有效时间
	private String uid;

	public void setMyList(ArrayList<Status> list,String uid) {
		this.myList = list;
		friendTimeStamp = System.currentTimeMillis();
		this.uid=uid;
	}

	public ArrayList<Status> getMyList() {
		long time = System.currentTimeMillis();
		if (time - friendTimeStamp > cacheTime) {
			// 缓存失效
			Log.i("weibo", "friend缓存失效"+ cacheTime +""+(time - friendTimeStamp));
			myList = null;
		}
		return myList;
	}
	public String getUid(){
		return uid;
	}
}
