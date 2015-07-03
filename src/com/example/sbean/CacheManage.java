package com.example.sbean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;

public class CacheManage {
	private static CacheManage cache;
	private static Context context;
	private static final String MINE_FILE_NAME = "weibolist";
	private static final String FRIEND_FILE_NAME = "friendweibolist";

	private CacheManage() {
	}

	private CacheManage(Context c) {
		this.context = c;
	}

	public static CacheManage getInstance(Context c) {
		if (cache == null) {
			cache = new CacheManage(c);
		}
		return cache;
	}

	public MyListCache readMine(String uid) {
		File dir = context.getCacheDir();
		File f = new File(dir, MINE_FILE_NAME);
		MyListCache weiboCache = null;
		if (!f.exists()) {
			return null;
		}
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(f));
			weiboCache = (MyListCache) ois.readObject();
			ois.close();
		} catch (Exception e) {
		}
		if (weiboCache.getUid().equals(uid))
			return weiboCache;
		else
			notifyInvalid();
		return null;
	}

	public void writeMine(MyListCache weiboCache) {
		File dir = context.getCacheDir();
		File f = new File(dir, MINE_FILE_NAME);
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e1) {
			}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(weiboCache);
			oos.close();
		} catch (IOException e) {
			Log.i("weibo", "io");
		}
	}

	public FriendListCache readFriend(String uid) {
		File dir = context.getCacheDir();
		File f = new File(dir, FRIEND_FILE_NAME);
		FriendListCache weiboCache = null;
		if (!f.exists()) {
			return null;
		}
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(f));
			weiboCache = (FriendListCache) ois.readObject();
			ois.close();
		} catch (Exception e) {
		}
		if (weiboCache.getUid().equals(uid))
			return weiboCache;
		else
			notifyInvalid();
		return null;
	}

	public void writeFriend(FriendListCache weiboCache) {
		File dir = context.getCacheDir();
		File f = new File(dir, FRIEND_FILE_NAME);
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e1) {
			}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(weiboCache);
			oos.close();
		} catch (IOException e) {
			Log.i("weibo", "io");
		}
	}

	public static void notifyInvalid() {
		File dir = context.getCacheDir();
		File f = new File(dir, MINE_FILE_NAME);
		f.delete();
		f = new File(dir, FRIEND_FILE_NAME);
		f.delete();
		Log.i("weibo", "缓存文件已经删除");
	}
}
