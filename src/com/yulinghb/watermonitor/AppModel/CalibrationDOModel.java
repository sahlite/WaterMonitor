package com.yulinghb.watermonitor.AppModel;

import java.util.ArrayList;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Message;

import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.service.DataFactory;
import com.yulinghb.watermonitor.service.JobAgent;

public class CalibrationDOModel {
	public interface CalibrationDOModelGetDO{
		final int RESULT_OK = 0;
		final int RESULT_ERR = -1;
		
		void onZeroResult(int result);
		void OnSencondAirResult(int result);
		void onAirResult(int result);
		void onRestoreResult(int result);
		void onSalinityResult(int result);
	}
	
	/*
	 * 婧舵哀鏍″噯浠ｇ悊
	 * 
	 */
	JobAgent mDOCalibrationAgent;
	
	/*
	 * 鏍″噯瀹㈡埛绔�
	 */
	CalibrationClient mCalibrationClient;
	
	CalibrationDOModelGetDO mCallback;

	/*
	 * 鍒濆鍖�
	 * 
	 */
	public CalibrationDOModel(CalibrationDOModelGetDO callback){
		mCallback = callback;
		mDOCalibrationAgent = DataFactory.getInstance().getDOCalibrationAgent();
		mCalibrationClient = new CalibrationClient();
		DataFactory.getInstance().putClient(mCalibrationClient);
	}	
	
	public void destory(){
		DataFactory.getInstance().removeClient(mCalibrationClient);
		mCalibrationClient = null;
		
		mDOCalibrationAgent.destory();
		mDOCalibrationAgent = null;
	}

	/*
	 * 鑾峰彇閲囨牱鐐�
	 */
	public void CalibrateZeroOnClick(Bundle bundle){
		mCalibrationClient.sendCalibrateZero(bundle);
	}
	
	
	/*
	 * 鑾峰彇閲囨牱鐐�
	 */
	public void CalibrateAirOnClick(Bundle bundle){
		mCalibrationClient.sendCalibrateAir(bundle);
	}
	
	/*
	 * 璁＄畻骞跺啓鍏ユ牎鍑嗗弬鏁�
	 */
	public void RestoreOnClick(Bundle bundle){
		mCalibrationClient.sendRestore(bundle);
	}
	
	/*
	 * 脕陆碌茫脨拢脳录--驴脮脝酶
	 */
	public void CalibrateSecAirOnClick(Bundle bundle){
		mCalibrationClient.sendCalibrateSecAir(bundle);
	}
	
	public void SetSalinityOnClick(Bundle bundle){
		mCalibrationClient.sendSetSalinity(bundle);
	}
	
	/*
	 * 鏍″噯瀹㈡埛绔�
	 * 鍙戦�锛屾帴鏀舵憾姘ф牎鍑嗘秷鎭�
	 */
	class CalibrationClient extends DataClient implements WaterMonitorClientContract{

		CalibrationClient(){
			super(TYPE_CALIBRATION_DATA, ORITATION_IN);
		}

		@Override
		protected void doJob(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.arg2){
			case 0:
				mCallback.onZeroResult(CalibrationDOModelGetDO.RESULT_OK);
				break;
			case 1:
				mCallback.onAirResult(CalibrationDOModelGetDO.RESULT_OK);
				break;
			case 2:
				mCallback.onRestoreResult(CalibrationDOModelGetDO.RESULT_OK);
				break;
			case 3:
				mCallback.OnSencondAirResult(CalibrationDOModelGetDO.RESULT_OK);
				break;
			}
			
		}
		
		void sendCalibrateZero(Bundle bundle){
			int index = bundle.getInt(CalibrationListModel.PROBE_INDEX);
			send(TYPE_CALIBRATION_DATA, ORITATION_OUT, 0, index);
		}
		
		void sendCalibrateAir(Bundle bundle){
			int index = bundle.getInt(CalibrationListModel.PROBE_INDEX);
			send(TYPE_CALIBRATION_DATA, ORITATION_OUT, 1, index);
		}
		
		void sendRestore(Bundle bundle){
			int index = bundle.getInt(CalibrationListModel.PROBE_INDEX);
			send(TYPE_CALIBRATION_DATA, ORITATION_OUT, 2, index);
		}
		
		void sendCalibrateSecAir(Bundle bundle){
			int index = bundle.getInt(CalibrationListModel.PROBE_INDEX);
			send(TYPE_CALIBRATION_DATA, ORITATION_OUT, 3, index);
		}
		
		void sendSetSalinity(Bundle bundle){
			int index = bundle.getInt(CalibrationListModel.PROBE_INDEX);
			float salinity = bundle.getFloat(CalibrationListModel.SALINITY);
			ContentValues values = new ContentValues();
			values.put(WaterMonitorClientContract.SALINITY_INDEX, index);
			values.put(WaterMonitorClientContract.SALINITY_VALUE, salinity);
			send(TYPE_CALIBRATION_DATA, ORITATION_OUT, 3, values);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			mCallback.onZeroResult(CalibrationDOModelGetDO.RESULT_ERR);
			mCallback.onAirResult(CalibrationDOModelGetDO.RESULT_ERR);
			mCallback.onRestoreResult(CalibrationDOModelGetDO.RESULT_ERR);
			mCallback.OnSencondAirResult(CalibrationDOModelGetDO.RESULT_ERR);
			mCallback.onSalinityResult(CalibrationDOModelGetDO.RESULT_ERR);
		}
	}
}
