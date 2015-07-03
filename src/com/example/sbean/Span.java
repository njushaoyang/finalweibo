package com.example.sbean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

public class Span {
	private static String nameReg = "@[^`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]+";
	private static String urlReg = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?";

	public static Spannable changeName(String str) {
		Spannable word = new SpannableString(str);
		Pattern p = Pattern.compile(nameReg);
		Matcher m = p.matcher(str);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			CharacterStyle sp = new ForegroundColorSpan(Color.argb(255, 74,
					112, 139));
			word.setSpan(sp, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}

		p = Pattern.compile(urlReg);
		m = p.matcher(str);
		while (m.find()) {
			int start = m.start();
			int end = m.end();

			CharacterStyle sp = new ForegroundColorSpan(Color.argb(255, 92,
					172, 238));
			word.setSpan(sp, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}

		return word;
	}
	
	public static Spannable changeSize(String str){
		String t[]=str.split(" ");
		String day=t[0];
		str=t[0]+t[1];
		Spannable word = new SpannableString(str);
		word.setSpan(new AbsoluteSizeSpan(26,true), 0, day.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		word.setSpan(new AbsoluteSizeSpan(14,true), day.length(), str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		//word.setSpan(new AbsoluteSizeSpan(18), 0, str.length()-1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return word;
	}
/*	public static Spannable changeUrl(String str) {
		Spannable word = new SpannableString(str);
		Pattern p = Pattern.compile(urlReg);
		Matcher m = p.matcher(str);
		while (m.find()) {
			int start = m.start();
			int end = m.end();

			CharacterStyle sp = new ForegroundColorSpan(Color.argb(255, 92,
					172, 238));
			word.setSpan(sp, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}

		return word;
	}*/

	/*public class MyLinkSpan extends ClickableSpan {

		String text;

		public MyLinkSpan(String text) {
			super();
			this.text = text;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(ds.linkColor);
			ds.setUnderlineText(false); // 去掉下划线
		}

		@Override
		public void onClick(View widget) {
			// 点击超链接时调用
		}

	}*/
}
