package com.yulinghb.watermonitor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yulinghb.watermonitor.AppModel.MonitorModel;
import com.yulinghb.watermonitor.AppModel.MonitorModel.UpdateDeviceInfo;
import com.yulinghb.watermonitor.AppModel.MonitorModel.UpdateMeasureData;
import com.yulinghb.watermonitor.AppModel.MonitorModel.UpdateStateInfo;

/**
 * 监视数据界面
 * 如果连接到设备，将按照设置，实时监视数据信息
 * 可以通过统计记录数据
 */
public class FragmentMonitor extends Fragment implements UpdateStateInfo, UpdateDeviceInfo, UpdateMeasureData, OnClickListener{
	public static final String MONITOR_BUTTON_STATE = "monitor button visibility";
	public static final String DATA_PANEL_STATE = "data panel visibility";

	private static TextView temp_v;
	private static TextView rdo_v;
	private static TextView rdo_sat_v;
	private static TextView baro_v;
	private static Button bMonitor;
	private static Button bConnect;
	private static LinearLayout dataPanel;
	private static ProgressBar mUpdateProgress;
	private static TextView tvConnection;
	private static TextView tvLocation;
	private static ImageView ivLocation;
	private static ProgressBar mBattery;
	
	private String savedconnection;
	private String saveddo;
	private String savedsat;
	private String savedtemp;
	private String savedbaro;
	
//	private FragmentProgressPrompt prompt;
	private ProgressDialog prompt;

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
//		DataFactory.getInstance().onDestory();
		if (bConnect.getText()==getString(R.string.txt_disconnect)){
			if (model.isCollecting()){
    			model.stopCollecting();
    		}
			model.disconnectOnClick();
		}
		model.destory();
		model = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		model.resume();
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		savedconnection = tvConnection.getText().toString();
		saveddo = rdo_v.getText().toString();
		savedsat = rdo_sat_v.getText().toString();
		savedtemp = temp_v.getText().toString();
		savedbaro = baro_v.getText().toString();
		model.pause();
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		model.destory();
//		model = null;
	}

	private int currentState;
	private static MonitorModel model;
	/*
	 * 开始或停止统计数据
	 * 停止时将会计算平均值同时建立记录保存到数据库中
	 */
	private OnClickListener btnMonitorListener = new OnClickListener(){
		@Override
        public void onClick(View view) {
			model.monitorOnClick();
        }
	};
	
	/*
	 * 连接或断开连接
	 */
	private OnClickListener btnConnectListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (bConnect.getText()==getString(R.string.txt_connect)){
				model.connectOnClick();
//				prompt = new FragmentProgressPrompt();
//				prompt.show(getFragmentManager(), getString(R.string.txt_delete_quire));
//				prompt = ProgressDialog.show(getActivity(), "", "");
			}else{
				if (model.isCollecting()){
	    			model.stopCollecting();
	    		}
				model.disconnectOnClick();
//				bMonitor.setVisibility(View.VISIBLE);
//				dataPanel.setVisibility(View.VISIBLE);
//				bConnect.setText(getString(R.string.txt_connect));
			}
				
		}
	};
	
	public static MonitorModel getModel(){
		return model;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_monitor, container, false);
		temp_v = (TextView)rootView.findViewById(R.id.view_temp_value);
		rdo_v = (TextView)rootView.findViewById(R.id.view_rdo_value);
		rdo_sat_v = (TextView)rootView.findViewById(R.id.view_rdo_sat_value);
		baro_v = (TextView)rootView.findViewById(R.id.view_baro_value);
		bMonitor =  (Button)rootView.findViewById(R.id.btn_monitor);
		bMonitor.setOnClickListener(btnMonitorListener);
		bConnect = (Button)rootView.findViewById(R.id.btn_connect);
		bConnect.setOnClickListener(btnConnectListener);
		dataPanel = (LinearLayout)rootView.findViewById(R.id.layout_data_panel);
		mUpdateProgress = (ProgressBar)rootView.findViewById(R.id.pgb_monitor_progress);
		tvConnection = (TextView)rootView.findViewById(R.id.tv_connection);
		tvLocation = (TextView)rootView.findViewById(R.id.tv_location);
		ivLocation = (ImageView)rootView.findViewById(R.id.iv_location_image);
		mBattery = (ProgressBar)rootView.findViewById(R.id.pgb_battery_status);
		
		tvLocation.setOnClickListener(this);
		ivLocation.setOnClickListener(this);
		
		if (null == savedconnection){
			savedconnection = getString(R.string.txt_unkown);
		}else{
			tvConnection.setText(savedconnection);
		}
		
		if (null == saveddo){
			saveddo = getString(R.string.txt_zero);
		}else{
			rdo_v.setText(saveddo);
		}
		
		if (null == savedtemp){
			savedtemp = getString(R.string.txt_zero);
		}else{
			temp_v.setText(savedtemp);
		}
		
		if (null == savedsat){
			savedsat = getString(R.string.txt_zero);
		}else{
			rdo_sat_v.setText(savedsat);
		}
		
		if (null == savedbaro){
			savedbaro = getString(R.string.txt_zero);
		}else{
			baro_v.setText(savedbaro);
		}

		if (null == model){
			model = new MonitorModel(getActivity(), this, this, this);
		}else{
//			model.init();
		}
		
		if (null != savedInstanceState){
			currentState = savedInstanceState.getInt(MONITOR_BUTTON_STATE);
			setState(currentState);
		}
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(MONITOR_BUTTON_STATE, currentState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void updateTemp(String temp) {
		// TODO Auto-generated method stub
		temp_v.setText(temp);
	}

	@Override
	public void updateDO(String doValue) {
		// TODO Auto-generated method stub
		rdo_v.setText(doValue);
	}

	@Override
	public void updateSAT(String sat) {
		// TODO Auto-generated method stub
		rdo_sat_v.setText(sat);
	}

	@Override
	public void updateBaro(String baro) {
		// TODO Auto-generated method stub
		baro_v.setText(baro);
	}

	@Override
	public void updateSmartmeter(String id, String sn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setState(int state) {
		// TODO Auto-generated method stub
		currentState = state;
		switch(state){
			case MonitorModel.STATE_IDLE:
				bMonitor.setVisibility(View.INVISIBLE);
				dataPanel.setVisibility(View.INVISIBLE);
				bConnect.setText(getString(R.string.txt_connect));
				if (null != prompt){
					prompt.dismiss();
					prompt = null;
				}
				break;
			case MonitorModel.STATE_CONNECTING:
				if (null == prompt){
					prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_connecting));
				}
				break;
			case MonitorModel.STATE_CONNECTED:
				temp_v.setText(getString(R.string.txt_zero));
				rdo_v.setText(getString(R.string.txt_zero));
				rdo_sat_v.setText(getString(R.string.txt_zero));
				baro_v.setText(getString(R.string.txt_zero));
				bMonitor.setVisibility(View.INVISIBLE);
				dataPanel.setVisibility(View.INVISIBLE);
				bConnect.setText(getString(R.string.txt_disconnect));
				break;
			case MonitorModel.STATE_DEVICE_REQUIRING:
				break;
			case MonitorModel.STATE_DEVICE_OK:
				bMonitor.setVisibility(View.VISIBLE);
				dataPanel.setVisibility(View.VISIBLE);
				bConnect.setText(getString(R.string.txt_disconnect));
				if (!model.isCollecting()){
	    			model.startCollecting();
	    		}
				break;
			case MonitorModel.STATE_DATA_COLLECTING:
				if (null != prompt){
					prompt.dismiss();
					prompt = null;
				}
				break;
		}
	}

	@Override
	public void updateProbeSN(int index, String sn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateProgress(int value) {
		// TODO Auto-generated method stub
		mUpdateProgress.setProgress(value);
	}

	@Override
	public void updateProgressMax(int maxValue) {
		// TODO Auto-generated method stub
		mUpdateProgress.setMax(maxValue);
	}

	@Override
	public void setConnectionName(String device) {
		// TODO Auto-generated method stub
		tvConnection.setText(device);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		model.changeLocation();
	}

	@Override
	public void setLocationInfo(String name, String imagepath) {
		// TODO Auto-generated method stub
		tvLocation.setText(name);
		Bitmap bitmap = RecorderFile.getBitmap(getActivity(), 
												imagepath, 
												getActivity().getString(R.string.txt_unkown), 
												72, 
												72);
		ivLocation.setImageBitmap(bitmap);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (Activity.RESULT_OK == resultCode){
			switch(requestCode){
				case MonitorModel.INTENT_GET_LOCATION:
				{
					model.onActivityResult(requestCode, resultCode, data);
				}
				break;
			}
		}
	}

	@Override
	public void setBattery(int status) {
		// TODO Auto-generated method stub
		mBattery.setProgress(status);
	}
}