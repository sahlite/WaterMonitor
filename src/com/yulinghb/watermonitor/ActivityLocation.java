package com.yulinghb.watermonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yulinghb.watermonitor.AppModel.LocationEditModel;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowBaseInfo;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowGPS;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowImage;

public class ActivityLocation extends FragmentActivity implements LocationShowBaseInfo, LocationShowImage, LocationShowGPS{
//	private static final String TAG = "LocationFragment";
    
	private EditText etLocation;
	private EditText etDescription;
	private Button bSave;
	private Button bCancel;
	private ImageButton bCamera;
	private ImageButton bImage;
	private ImageButton bGPS;
	private ImageView iImage;
	private TextView vtLatitude;
	private TextView vtLongtitude;
	
	LocationEditModel model;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.location);

		model = new LocationEditModel(this, getIntent(), this ,this ,this);
		
		etLocation = (EditText)findViewById(R.id.et_salinity);
		etDescription = (EditText)findViewById(R.id.editText2);
		bSave = (Button)findViewById(R.id.btn_save);
		bSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				model.saveOnClick(etLocation.getText().toString(), etDescription.getText().toString());
				finish();
			}
		});
		
		bCancel = (Button)findViewById(R.id.btn_cancel);
		bCancel.setVisibility(View.INVISIBLE);
		
		bCamera = (ImageButton)findViewById(R.id.btn_camera);
		bCamera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				model.takePhotoOnClick();
			}
		});
		
		bImage = (ImageButton)findViewById(R.id.btn_image);
		bImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				model.takeImageOnClick();
			}
		});
		iImage = (ImageView)findViewById(R.id.iv_pic);
		
		bGPS = (ImageButton)findViewById(R.id.btn_gps);
		bGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				model.getGPSOnClick();
			}
		});
		
		vtLatitude = (TextView)findViewById(R.id.view_latitude_value);
		vtLongtitude = (TextView)findViewById(R.id.view_longtitude_value);
		
		model.update();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
//		model.onPause();
		super.onPause();
	}

	public void update(){
		model.update();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (Activity.RESULT_OK == resultCode){
			switch(requestCode){
				case LocationEditModel.TAKE_PHOTO_CODE:
				{
					model.onTakePhotoResult(data);
				}
				break;
				
				case LocationEditModel.PICK_IMAGE_CODE:
				{
					model.onTakeImageResult(data);
				}
				break;
			}
		}
	}


	@Override
	public void showGPS(double longtitude, double latitude) {
		// TODO Auto-generated method stub
		String messagelongtitude = String.valueOf(longtitude);
		String messagelatitude = String.valueOf(latitude);
		try{
			messagelongtitude = String.format("%.4f", longtitude);
			messagelatitude = String.format("%.4f", latitude);
		}catch(Exception e){
			
		}
		vtLatitude.setText(messagelatitude);
		vtLongtitude.setText(messagelongtitude);
	}

	@Override
	public void showImage(Bitmap data) {
		// TODO Auto-generated method stub
		iImage.setImageBitmap(data);
	}

	@Override
	public void showinfo(String name, String description) {
		// TODO Auto-generated method stub
		etLocation.setText(name);
		etDescription.setText(description);
	}

	@Override
	public int getImageWidth() {
		// TODO Auto-generated method stub
		return iImage.getWidth();
	}

	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return iImage.getHeight();
	}

	@Override
	public void createlocation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDefaultImage() {
		// TODO Auto-generated method stub
		iImage.setImageResource(R.drawable.logo);
	}

}
