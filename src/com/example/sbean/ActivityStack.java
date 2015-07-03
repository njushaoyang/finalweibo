package com.example.sbean;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Activity;

public class ActivityStack {
	private static ActivityStack stack;
	private static LinkedList<Activity> list;
	private ActivityStack(){}
	public static ActivityStack getInstance(){
		if(stack==null)
			stack=new ActivityStack();
		return stack;
	}
	public void push(Activity a){
		if(list==null)
			list=new LinkedList<Activity>();
		list.push(a);
	}
	public void pop(){
		Activity a=list.pop();
		a.finish();
		a=null;
	}
	public Activity peek(){
		return list.peek();
	}
	public void remove(Activity a){
		if(a==null)
			return ;
		list.remove(a);
		a.finish();
		a=null;
	}
	public void remove(Class<?> cls){
		Iterator<Activity> it=list.iterator();
		while(it.hasNext()){
			Activity a=it.next();
			if(a.getClass().equals(cls)){
				it.remove();
			}
			a.finish();
		}
	}
	public void clearOther(Class<?> cls){
		Iterator<Activity> it=list.iterator();
		while(it.hasNext()){
			Activity a=it.next();
			if(!a.getClass().equals(cls)){
				it.remove();
				a.finish();
			}
		}
	}
	public Class topsPre(){
		int index=list.size()-2;
		if(index>0){
			return list.get(index).getClass();
		}else
			return null;
	}
	public int getTopIndex(){
		return list.size()-1;
	}
	public Activity getActivity(int index){
		if(index>=0&&index<list.size())
			return list.get(index);
		else
			return null;
	}
}
