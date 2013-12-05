package com.yulinghb.watermonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yulinghb.watermonitor.AppModel.RecorderEditModel;
import com.yulinghb.watermonitor.AppModel.RecorderEditModel.RecorderUpdateOnClick;

public class ActivityRecorder extends FragmentActivity implements OnClickListener, RecorderUpdateOnClick{
	public static final String TAG = "RecorderFragment";
	
    private TextView vTime;
    private TextView vTemp;
    private TextView vRDO;
    private TextView vSAT;
    private TextView vBaro;
    private TextView vLocation;
    private ImageView ivLocation;
    private Button bEmail;
    
    private RecorderEditModel model;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.recorder);
		model = new RecorderEditModel(this, this);
        
        vTime = (TextView) findViewById(R.id.view_time_value);
        vTemp = (TextView) findViewById(R.id.view_detail_temp_value);
        vRDO = (TextView) findViewById(R.id.view_detail_rdo_value);
        vSAT = (TextView) findViewById(R.id.view_detail_rdo_sat_value);
        vBaro = (TextView) findViewById(R.id.view_detail_baro_value);
        vLocation = (TextView)findViewById(R.id.view_detail_location_name);
        ivLocation = (ImageView) findViewById(R.id.image_detail_location_image);
        bEmail = (Button) findViewById(R.id.btn_email);
        bEmail.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				model.emailOnClick();
			}
        	
        });
        vLocation.setOnClickListener(this);
        ivLocation.setOnClickListener(this);
        
        model.update();
	}
	
//	public void selectRecorder(Intent intent){
//		model.selectRecorder(intent);
//	}
	
	public void update(){
		model.update();
//		model.updateRecorderInfo();
//		model.updateLocationInfo();
	}

//	@Override
//	public void onStart() {
//		// TODO Auto-generated method stub
//		super.onStart();
//		model.updateRecorderInfo();
//	}
//
//	@Override
//	public void onResume() {
//		// TODO Auto-generated method stub
//		super.onResume();
//		model.updateLocationInfo();
//	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		model.changeLocationOnClick();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
//		if ((RecorderEditModel.INTENT_GET_LOCATION == requestCode)&&(Activity.RESULT_OK == resultCode)){
////			selected_location = data.getIntExtra(LocationListModel.GET_CONTENT_ID, 0);
//			model.updateSelectedLocation(data);
//		}
		model.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void updateTimeOnClick(String time) {
		// TODO Auto-generated method stub
		vTime.setText(time);
	}

	@Override
	public void updateDOOnClick(String doValue) {
		// TODO Auto-generated method stub
		
		vRDO.setText(doValue);
	}

	@Override
	public void updateTempOnClick(String temperature) {
		// TODO Auto-generated method stub
		vTemp.setText(temperature);
	}

	@Override
	public void updateSATOnClick(String sat) {
		// TODO Auto-generated method stub
		vSAT.setText(sat);
	}

	@Override
	public void updateBaroOnClick(String baro) {
		// TODO Auto-generated method stub
		vBaro.setText(baro);
	}

	@Override
	public void updateLocationOnClick(String location) {
		// TODO Auto-generated method stub
		vLocation.setText(location);
	}

	@Override
	public void updateImageOnClick(Bitmap data) {
		// TODO Auto-generated method stub
		ivLocation.setImageBitmap(data);
	}

	@Override
	public int getImageWidth() {
		// TODO Auto-generated method stub
		return ivLocation.getWidth();
	}

	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return ivLocation.getHeight();
	}

	@Override
	public void updateDefaultImage() {
		// TODO Auto-generated method stub
		ivLocation.setImageResource(R.drawable.logo);
	}
}
