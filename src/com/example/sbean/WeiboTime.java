package com.example.sbean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class WeiboTime {
	public static Map<String, String> month = new HashMap<String, String>() {
		{
			put("Jan", "1");
			put("Feb", "2");
			put("Mar", "3");
			put("Apr", "4");
			put("May", "5");
			put("Jun", "6");
			put("Jul", "7");
			put("Aug", "8");
			put("Sep", "9");
			put("Oct", "10");
			put("Nov", "11");
			put("Dec", "12");
		}
	};
	public static Map<String, String> week = new HashMap<String, String>() {
		{
			put("Mon", "1");
			put("Tue", "2");
			put("Wed", "3");
			put("Thu", "4");
			put("Fri", "5");
			put("Sat", "6");
			put("Sun", "7");
		}
	};

	public static String format(String time) {
		String[] array = time.split(" ");
		int length = array.length;
		if (length != 6) {

			return "";
		}
		String w = week.get(array[0]);
		String m = month.get(array[1]);
		String d = array[2];
		String tempH = array[3];
		String y = array[5];
		String tmp[] = tempH.split(":");
		String hour = tmp[0];
		String minute = tmp[1];

		Calendar c = Calendar.getInstance();
		int nowD = c.get(Calendar.DAY_OF_MONTH);
		int nowM = c.get(Calendar.MONTH);
		int nowY = c.get(Calendar.YEAR);
		int nowH = c.get(Calendar.HOUR_OF_DAY);
		int nowMin = c.get(Calendar.MINUTE);
		
		if (Integer.parseInt(y) == nowY && Integer.parseInt(m) == (nowM+1)
				&& Integer.parseInt(d) == nowD) {
			int diffH=nowH-Integer.parseInt(hour);
			if(diffH==0){
				int diffM=nowMin-Integer.parseInt(minute);
				if(diffM==0)
					return "刚刚";
				else
					return diffM+"分钟前";
			}else if(diffH==1){
				int diffM=nowMin+60-Integer.parseInt(minute);
				if(diffM>60)
					return "1小时前";
				else
					return diffM+"分钟前";
			}else
				return diffH+"小时前";
		}
		return m + "-" + d + " " + hour + ":" + minute;
	}
	
	public static String getMonthDay(String time){
		String[] array = time.split(" ");
		int length = array.length;
		if (length != 6) {

			return "";
		}
		String w = week.get(array[0]);
		String m = month.get(array[1]);
		String d = array[2];
		return d+" "+m+"月";
	}
	
	public static String getDetialTime(String time){
		String[] array = time.split(" ");
		int length = array.length;
		if (length != 6) {

			return "";
		}

		String m = month.get(array[1]);
		String d = array[2];
		String tempH = array[3];
	
		String tmp[] = tempH.split(":");
		String hour = tmp[0];
		String minute = tmp[1];
		
		return m+"-"+d+" "+hour+":"+minute;
	}
}
