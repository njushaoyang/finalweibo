/**
 * 
 */
package com.example.myappproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.sbean.ActivityStack;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 韶阳
 *
 */
public class GetImageActivity extends Activity {
	private TextView fa;
	private TextView fc;
	public static final String SAVE_IMAGE_CAMERA = "/mnt/sdcard/";
	public static final String BACKGROUND_IMAGE = "background_iamge.jpg";
	private String capturePath = null;
	public static final int CHOICE_CAMERA = 1;
	public static final int CHOICE_PHOTO = 2;
	ActivityStack stack;
	private final int height=250;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getimage_layout);
		stack = ActivityStack.getInstance();
		stack.push(this);
		
		ActionBar ab = getActionBar();
		if(ab!=null){
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);
		}
		
		fa = (TextView) findViewById(R.id.from_album);
		fc = (TextView) findViewById(R.id.from_camera);
		fa.setOnClickListener(new ImageListenner());
		fc.setOnClickListener(new ImageListenner());
	}

	protected void getImageFromAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");// 相片类型
		startActivityForResult(intent, CHOICE_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == CHOICE_CAMERA && resultCode == RESULT_OK) {
			getCropImage(Uri.fromFile(new File(capturePath)));
		}
		if (requestCode == CHOICE_PHOTO && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			getCropImage(uri);

		}
		if (requestCode == 200 && resultCode == RESULT_OK) {
			Bitmap bmap = data.getParcelableExtra("data");
			// 保存图片
			// 创建一个位于SD卡的文件
			File dir = getFilesDir();
			File file = new File(dir, BACKGROUND_IMAGE);

			/*
			 * File file = new File(Environment.getExternalStorageDirectory(),
			 * "phoneName.jpg");
			 */
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(file);
				bmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
				fileOutputStream.close();
				setResult(12);
				Intent intent=new Intent(this,WeiboListActivity.class);
				stack.clearOther(GetImageActivity.class);
				startActivity(intent);
				stack.clearOther(WeiboListActivity.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void getImageFromCamera() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			Intent getImageByCamera = new Intent(
					"android.media.action.IMAGE_CAPTURE");
			String out_file_path = SAVE_IMAGE_CAMERA;
			File dir = new File(out_file_path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			capturePath = SAVE_IMAGE_CAMERA + System.currentTimeMillis()
					+ ".jpg";
			getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(capturePath)));
			getImageByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(getImageByCamera, CHOICE_CAMERA);
		} else {
			Toast.makeText(getApplicationContext(), "请确认已经插入SD卡",
					Toast.LENGTH_LONG).show();
		}
	}

	private void getCropImage(Uri mUri) {
		if (null == mUri)
			return;

		Intent intent = new Intent();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);

		int width = metric.widthPixels; // 宽度（PX）
		
		width=Px2Dp(width);
		intent.setAction("com.android.camera.action.CROP");
		intent.setDataAndType(mUri, "image/*");// mUri是已经选择的图片Uri
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", width);// 裁剪框比例
		intent.putExtra("aspectY", height);
		intent.putExtra("outputX", width);// 输出图片大小
		intent.putExtra("outputY", height);
		intent.putExtra("return-data", true);

		startActivityForResult(intent, 200);
	}

	class ImageListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.from_album) {
				getImageFromAlbum();
			} else if (v.getId() == R.id.from_camera) {
				getImageFromCamera();
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		stack.remove(this);
	}
	public int Dp2Px(float dp) {  
	    final float scale = getResources().getDisplayMetrics().density;  
	    return (int) (dp * scale + 0.5f);  
	}  

	public int Px2Dp(float px) {  
	    final float scale = getResources().getDisplayMetrics().density;  
	    return (int) (px / scale + 0.5f);  
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
