package com.yulinghb.watermonitor.AppModel;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;

import com.yulinghb.watermonitor.service.DataFactory;

public class CalibrationListModel {
	public static final String PROBE_INDEX = "index"; //探头索引，即为连接到meter的插口号
	public static final String PROBE_TYPE = "type"; //探头类型
	
	public static final String PROBE_TYPE_DO = "DO"; //溶氧 探头
	public static final String PROBE_TYPE_UNKOWN = "UNKOWN"; // 未知类型
	
	public static final String SALINITY = "salinity";
	
	public static final int CALIBRATION_TYPE_NONE = 0;
	public static final int CALIBRATION_TYPE_DO = 1;
	
	static List<String> mListItems; //用于显示的字符串
	static ArrayList<ContentValues> mProbes; //保存探头信息的列表
	
	public interface CalibrationSelectedListener{
		void selectCalibration(int position);
	}

	CalibrationSelectedListener mListener;

	public CalibrationListModel(Activity activity){
	}

//	@Override
	public static List<String> getCalibrationList() {
		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
		//从DataFactory获取当前 连接到的probe信息
		mProbes = DataFactory.getInstance().getProbesID();
		int size = mProbes.size();
		mListItems = new ArrayList<String>();
		for (int i=0; i<size; i++){
			ContentValues probe = mProbes.get(i);
			mListItems.add(probe.getAsString(PROBE_TYPE) + " #"
							+ String.valueOf(probe.getAsInteger(PROBE_INDEX))
							+ " Calibration");
		}
		return mListItems;
	}

	
	public void selectCalibration(int position){
		if (mListItems.get(position).contains(CalibrationListModel.PROBE_TYPE_DO)){
			if (null == mListener){
			}else{
				mListener.selectCalibration(position);
			}
			
		}
	}
	
	public void onAttach(CalibrationSelectedListener listener){
		mListener = listener;
	}
}
