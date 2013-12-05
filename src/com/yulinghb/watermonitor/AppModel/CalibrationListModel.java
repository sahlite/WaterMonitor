package com.yulinghb.watermonitor.AppModel;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;

import com.yulinghb.watermonitor.service.DataFactory;

public class CalibrationListModel {
	public static final String PROBE_INDEX = "index"; //̽ͷ��������Ϊ���ӵ�meter�Ĳ�ں�
	public static final String PROBE_TYPE = "type"; //̽ͷ����
	
	public static final String PROBE_TYPE_DO = "DO"; //���� ̽ͷ
	public static final String PROBE_TYPE_UNKOWN = "UNKOWN"; // δ֪����
	
	public static final String SALINITY = "salinity";
	
	public static final int CALIBRATION_TYPE_NONE = 0;
	public static final int CALIBRATION_TYPE_DO = 1;
	
	static List<String> mListItems; //������ʾ���ַ���
	static ArrayList<ContentValues> mProbes; //����̽ͷ��Ϣ���б�
	
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
		//��DataFactory��ȡ��ǰ ���ӵ���probe��Ϣ
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
