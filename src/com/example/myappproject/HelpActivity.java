package com.example.myappproject;

import com.example.sbean.ActivityStack;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class HelpActivity extends Activity {

	private ActivityStack stack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_layout);
		
		stack = ActivityStack.getInstance();
		stack.push(this);

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);
	}
	
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		stack.remove(this);
		super.onDestroy();
	}
	
	public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			stack.remove(this);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	};

}
