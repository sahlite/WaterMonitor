package com.yulinghb.watermonitor;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yulinghb.watermonitor.AppModel.CalibrationDOModel;
import com.yulinghb.watermonitor.AppModel.CalibrationDOModel.CalibrationDOModelGetDO;
import com.yulinghb.watermonitor.AppModel.CalibrationListModel;
import com.yulinghb.watermonitor.service.DataFactory;

public class FragmentCalibrationDO extends Fragment implements CalibrationDOModel.CalibrationDOModelGetDO{

	/*
	 * 
	 */
	Button zero;
	Button air;
	Button restore;
	LinearLayout mPanel;
	EditText salinity;
	Button setsalinity;
	
	
	CalibrationDOModel model;
	
	EditText deviceinfo;

	private ProgressDialog prompt;

	/*
	 * 鲁玫脢录禄炉
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(
				R.layout.calibration_do, container, false);
		
		zero = (Button)rootView.findViewById(R.id.btnDOZero);
		air = (Button)rootView.findViewById(R.id.btnDOCalibration);
		restore = (Button)rootView.findViewById(R.id.btnDORestore);
		mPanel = (LinearLayout)rootView.findViewById(R.id.calibration_do_panel);
		
		zero.setOnClickListener(mZeroListener);
		air.setOnClickListener(mAirListener);
		restore.setOnClickListener(mRestoreListener);

		deviceinfo = (EditText)rootView.findViewById(R.id.et_device_info);
		deviceinfo.setEnabled(false);
		
		salinity = (EditText)rootView.findViewById(R.id.et_salinity);
		setsalinity = (Button)rootView.findViewById(R.id.btn_set_salinity);
		setsalinity.setOnClickListener(mSalinityListener);
		
		return rootView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String information = "meter : ";
		String metersn = DataFactory.getInstance().getSmarterSN();
		if (null != metersn){
			information += metersn;
		}else{
			information += "------------";
		}
		information += " \t\n";
		
		String meterhw = DataFactory.getInstance().getSmarterHW();
		if (null != metersn){
			information += "HW rev : " + meterhw;
			information += " \t\n";
		}
		
		String metersw = DataFactory.getInstance().getSmarterSW();
		if (null != metersn){
			information += "SW rev : " + metersw;
			information += " \t\n";
		}

		information += "probe : ";
		if (0 == DataFactory.getInstance().getProbeNum()){
			information += "------------";
			mPanel.setVisibility(View.GONE);
		}else{
			information += DataFactory.getInstance().getProbeSN(0);
			information += " \t\n";
			information += "HW rev : " + DataFactory.getInstance().getProbeHW(0);
			information += " \t\n";
			information += "SW rev : " + DataFactory.getInstance().getProbeSW(0);
			information += " \t\n";
			mPanel.setVisibility(View.VISIBLE);
		}
		deviceinfo.setText(information);
		
		model = new CalibrationDOModel(this);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		model.destory();
		model = null;
	}

	/*
	 * 鑾峰彇閲囨牱鐐�
	 */
	OnClickListener mZeroListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			Bundle arg = new Bundle();
//			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
//			model.CalibrateZeroOnClick(arg);
//			if (null == prompt){
//				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_calibrate));
//			}
			FragmentDeletePrompt prompt = new FragmentDeletePrompt();
			prompt.setCallback(new CalibrateZeroPromptListener());
			prompt.show(getFragmentManager(), getString(R.string.txt_zero_prompt));
		}
		
	};
	
	/*
	 * 鑾峰彇閲囨牱鐐�
	 */
	OnClickListener mAirListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle arg = new Bundle();
			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
			model.CalibrateAirOnClick(arg);
			if (null == prompt){
				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_calibrate));
			}
		}
		
	};
	
	/*
	 * 璁＄畻骞跺啓鍏ユ牎鍑嗗弬鏁�
	 */
	OnClickListener mRestoreListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle arg = new Bundle();
			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
			model.RestoreOnClick(arg);
			if (null == prompt){
				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_restore));
			}
		}
		
	};
	
	/*
	 * 璁＄畻骞跺啓鍏ユ牎鍑嗗弬鏁�
	 */
	OnClickListener mSalinityListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle arg = new Bundle();
			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
			arg.putFloat(CalibrationListModel.SALINITY, 0f);
			model.RestoreOnClick(arg);
			if (null == prompt){
				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_restore));
			}
		}
		
	};

	@Override
	public void onZeroResult(int result) {
		// TODO Auto-generated method stub
		if (null != prompt){
			prompt.dismiss();
			prompt = null;
		}
		
		if (CalibrationDOModelGetDO.RESULT_OK == result){
			FragmentDeletePrompt prompt = new FragmentDeletePrompt();
			prompt.setCallback(new CalibrateAirPromptListener());
			prompt.show(getFragmentManager(), getString(R.string.txt_air_prompt));
		}
	}

	@Override
	public void onAirResult(int result) {
		// TODO Auto-generated method stub
		if (null != prompt){
			prompt.dismiss();
			prompt = null;
		}
		if (CalibrationDOModelGetDO.RESULT_OK == result){
			Toast.makeText(getActivity(), getString(R.string.txt_calibration_complete), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getActivity(), getString(R.string.txt_calibration_incomplete), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRestoreResult(int result) {
		// TODO Auto-generated method stub
		if (null != prompt){
			prompt.dismiss();
			prompt = null;
		}
		if (CalibrationDOModelGetDO.RESULT_OK == result){
			Toast.makeText(getActivity(), getString(R.string.txt_restore_complete), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getActivity(), getString(R.string.txt_restore_incomplete), Toast.LENGTH_SHORT).show();
		}
	}
	
	class CalibrateZeroPromptListener implements FragmentDeletePrompt.OnDeleteListener{

		@Override
		public void comfirm() {
			// TODO Auto-generated method stub
			Bundle arg = new Bundle();
			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
			model.CalibrateZeroOnClick(arg);
			if (null == prompt){
				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_calibrate));
			}
		}

		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getMessageId() {
			// TODO Auto-generated method stub
			return R.string.txt_zero_prompt;
		}
		
	}
	
	class CalibrateAirPromptListener implements FragmentDeletePrompt.OnDeleteListener{

		@Override
		public void comfirm() {
			// TODO Auto-generated method stub
			Bundle arg = new Bundle();
			arg.putInt(CalibrationListModel.PROBE_INDEX, 0);
			model.CalibrateSecAirOnClick(arg);
			if (null == prompt){
				prompt = ProgressDialog.show(getActivity(), getString(R.string.txt_wait), getString(R.string.txt_wait_calibrate));
			}
		}

		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getMessageId() {
			// TODO Auto-generated method stub
			return R.string.txt_air_prompt;
		}
		
	}

	@Override
	public void OnSencondAirResult(int result) {
		// TODO Auto-generated method stub
		if (null != prompt){
			prompt.dismiss();
			prompt = null;
		}
		if (CalibrationDOModelGetDO.RESULT_OK == result){
			Toast.makeText(getActivity(), getString(R.string.txt_calibration_complete), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getActivity(), getString(R.string.txt_calibration_incomplete), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onSalinityResult(int result) {
		// TODO Auto-generated method stub
		
		if (null != prompt){
			prompt.dismiss();
			prompt = null;
		}
	}
}
