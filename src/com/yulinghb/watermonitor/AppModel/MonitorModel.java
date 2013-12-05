package com.yulinghb.watermonitor.AppModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;

import com.yulinghb.watermonitor.R;
import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.service.DataFactory;
import com.yulinghb.watermonitor.service.JobAgent;

public class MonitorModel {
	public static final String MONITOR_BUTTON_STATE = "monitor button visibility";
	public static final String DATA_PANEL_STATE = "data panel visibility";
	public static final int INTENT_GET_LOCATION = 11;
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	public static final int STATE_DEVICE_REQUIRING = 3;
	public static final int STATE_DEVICE_OK = 4;
	public static final int STATE_DATA_COLLECTING = 5;
	
	public interface UpdateStateInfo{
		void setState(int state);
		void setConnectionName(String device);
		void setLocationInfo(String name, String imagepath);
		void setBattery(int status);
	}
	
	public interface UpdateDeviceInfo{
		void updateSmartmeter(String id, String sn);
		void updateProbeSN(int index, String sn);
	}
	
	public interface UpdateMeasureData{
		void updateTemp(String temp);
		void updateDO(String doValue);
		void updateSAT(String sat);
		void updateBaro(String baro);
		void updateProgress(int value);
		void updateProgressMax(int maxValue);
	}
	
	private static boolean is_collect_data = false;
	private static boolean is_collecting = false;
	DataFactory factory;
	
	/*
	 * 连接和设备信息客户端
	 */
    ConnectionNameClient mConnectionNameClient;
    DeviceInfoClient mDeviceInfoClient;
    private JobAgent mDeviceInfoAgent = null;
	MeasureDataClient client;
	
	/*
	 * 连接到的设备，包括smarter和probe
	 */
	private static final int INVALID_INDEX = 9;
	private int[] mProbeIndexTable = {INVALID_INDEX, INVALID_INDEX, INVALID_INDEX,
									  INVALID_INDEX, INVALID_INDEX, INVALID_INDEX,
									  INVALID_INDEX, INVALID_INDEX};
	
	private int collect_count = 0;
	private int table_index = 0;
	
	Activity mActivity;
	UpdateStateInfo mStateCallback;
	UpdateDeviceInfo mDeviceCallback;
	UpdateMeasureData mDataCallback;
	
	/*
	 * 更新采集数据的线程
	 */
	private UpdateDataThread mUpdateDataThread;
			
	public MonitorModel(Activity activity, UpdateStateInfo stateCallback, UpdateDeviceInfo deviceCallback, UpdateMeasureData dataCallback) {
		factory = DataFactory.getInstance();
		mActivity = activity;
		mStateCallback = stateCallback;
		mDeviceCallback = deviceCallback;
		mDataCallback = dataCallback;
		
		mStateCallback.setState(STATE_IDLE);
	}
	
	public void pause(){
		if ((is_collecting)&&(null != mUpdateDataThread)){
			mUpdateDataThread.cancel();
			mUpdateDataThread = null;
		}
	}
	
	public void resume(){
		
		mStateCallback.setState(STATE_IDLE);
		
		// if in collecting restart the working thread
		if (is_collecting){
			mStateCallback.setState(STATE_DEVICE_OK);
//			mDataCallback.updateProgressMax(mUpdateDataThread.updateDelayTime - 1);
			mUpdateDataThread = new UpdateDataThread(0, 5);
			mUpdateDataThread.start();
		}
		
		// recovery the location selected last time
		SharedPreferences preference = mActivity.getSharedPreferences(mActivity.getString(R.string.txt_location), Activity.MODE_PRIVATE);
		String name = preference.getString(LocationListModel.GET_CONTENT_NAME, "");
		String imagepath = preference.getString(LocationListModel.GET_CONTENT_IMAGE, "");
		if (("" != name)&&("" != imagepath)){
			mStateCallback.setLocationInfo(name, imagepath);
		}
	}
	
	public void destory(){
		if (null != mUpdateDataThread){
			mUpdateDataThread.cancel();
			mUpdateDataThread = null;
		}
	}
	
	/*
	 * 开始或停止统计数据
	 * 停止时将会计算平均值同时建立记录保存到数据库中
	 */
	public void monitorOnClick(){
		if (0 == factory.getProbeNum()){
			return;
		}
		
		is_collect_data = true;
	}
	
	/*
	 * 连接或断开连接
	 */
	public void connectOnClick(){
		
		if (null != mConnectionNameClient){
			factory.removeClient(mConnectionNameClient);
			mConnectionNameClient = null;
		}
		mConnectionNameClient = new ConnectionNameClient();
		factory.putClient(mConnectionNameClient);
		factory.connect();
	}
	
	public void disconnectOnClick(){
		// 停止采集线程
		
		if (null != mUpdateDataThread){
			mUpdateDataThread.cancel();
			mUpdateDataThread = null;
			is_collecting = false;
		}
		
		if (null != mDeviceInfoClient){
			mDeviceInfoClient.sendGetDeviceInfo();
			factory.removeClient(mDeviceInfoClient);
			mDeviceInfoClient = null;
		}
		
		cleardeviceinfo();
		factory.cleardeviceinfo();
		factory.disconnect();
		mStateCallback.setState(STATE_IDLE);
	}
	
	public void cleardeviceinfo(){
		for (int i=0;i<8;i++){
			mProbeIndexTable[i] = 0;
		}
		
		collect_count = 0;
		table_index = 0;
	}
	
	public void startCollecting(){
		if (0 != factory.getProbeNum()){
			if (null != mUpdateDataThread){
				mUpdateDataThread.cancel();
				mUpdateDataThread = null;
			}
			mUpdateDataThread = new UpdateDataThread(0, 5);
			mUpdateDataThread.start();
			mStateCallback.setState(STATE_DATA_COLLECTING);
			
			is_collecting = true;
		}
	}
	
	public void stopCollecting(){
		// 停止采集线程
		if (null != mUpdateDataThread){
			mUpdateDataThread.cancel();
			mUpdateDataThread = null;
		}
		mStateCallback.setState(STATE_DEVICE_OK);
		is_collecting = false;
	}
	
	public boolean isCollecting(){
		return is_collecting;
	}
	
	public void selectProbe(int index){
//		mCurrentProbeIndex = index;
	}
	
	public void changeLocation(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		mActivity.startActivityForResult(intent, INTENT_GET_LOCATION);
	}
	
	public void updateSelectedLocation(Intent data){
		if (null != data){
			SharedPreferences preference = mActivity.getSharedPreferences(mActivity.getString(R.string.txt_location), Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preference.edit();
			int location_id = data.getIntExtra(LocationListModel.GET_CONTENT_ID, 1);
			String name = data.getStringExtra(LocationListModel.GET_CONTENT_NAME);
			String imagepath = data.getStringExtra(LocationListModel.GET_CONTENT_IMAGE);
			editor.putInt(LocationListModel.GET_CONTENT_ID, location_id);
			editor.putString(LocationListModel.GET_CONTENT_NAME, name);
			editor.putString(LocationListModel.GET_CONTENT_IMAGE, imagepath);
			editor.commit();
			
			mStateCallback.setLocationInfo(name, imagepath);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		updateSelectedLocation(data);
	}

	/*
	 * 连接客户端，接收连接信息，当连接建立时，发起设备信息请求
	 */
	class ConnectionNameClient extends DataClient implements WaterMonitorClientContract{
		ConnectionNameClient(){
			super(TYPE_CONNECTION_INFO, ORITATION_IN);
		}

		@Override
		protected void doJob(Message msg) {
			if (-1 == msg.arg2){
				disconnectOnClick();
			}else if (0 == msg.arg2){
				String deviceName = (String)msg.obj;
				if (deviceName.contains("connecting")){
					mStateCallback.setState(STATE_CONNECTING);
				}else if (deviceName.contains("connect to")){
					mStateCallback.setState(STATE_CONNECTED);
					mStateCallback.setConnectionName(deviceName.substring(11));
					if (null != mDeviceInfoAgent){
						mDeviceInfoAgent.destory();
						mDeviceInfoAgent = null;
					}
					mDeviceInfoAgent = factory.getDeviceInfoAgent();
					
					if (null != mDeviceInfoClient){
						factory.removeClient(mDeviceInfoClient);
						mDeviceInfoClient = null;
					}
					mDeviceInfoClient = new DeviceInfoClient();
					factory.putClient(mDeviceInfoClient);

					mDeviceInfoClient.sendGetDeviceInfo();
					mStateCallback.setState(STATE_DEVICE_REQUIRING);
				}
			}
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			
		}
	}
	
	/*
	 * 设备信息客户端
	 * 设备信息保存在列表中反馈回来，列表项包含id, sn和index
	 * smarter的index为JobAgent.SMARTER_INDEX
	 * probe的index为0~7
	 */
	class DeviceInfoClient extends DataClient implements WaterMonitorClientContract{

		protected DeviceInfoClient() {
			super(TYPE_DEVICE_INFO, ORITATION_IN);
		}

		@Override
		protected void doJob(Message msg) {
			// TODO Auto-generated method stub
			if (-1 == msg.arg2){
				disconnectOnClick();
			}else if (0 == msg.arg2){
				if (null != msg.obj){
					ArrayList<ContentValues> devices = (ArrayList<ContentValues>)msg.obj;
					Iterator<ContentValues> i = devices.iterator();
					int count = 0;
					while(i.hasNext()){
						ContentValues item = i.next();
						if (item.getAsByte(JobAgent.PROBE_INDEX).byteValue() == JobAgent.SMARTER_INDEX){
							mDeviceCallback.updateSmartmeter(String.valueOf(item.getAsByte(JobAgent.DEVICE_ID).byteValue()),
									new String(item.getAsByteArray(JobAgent.DEVICE_SN)));
						}else{
							int position = item.getAsInteger(JobAgent.PROBE_INDEX).intValue();
							mProbeIndexTable[position] = count++;
							
							String probe_info = String.valueOf(position+1);
							probe_info += " " + String.valueOf(item.getAsByte(JobAgent.DEVICE_ID).byteValue());
							probe_info += " " + new String(item.getAsByteArray(JobAgent.DEVICE_SN));
							mDeviceCallback.updateProbeSN(position, probe_info);
							
						}
					}
					
					if (0 < devices.size()){
						mStateCallback.setState(STATE_DEVICE_OK);
					}else{
						mStateCallback.setState(STATE_CONNECTED);
					}
					
					if (null != mConnectionNameClient){
						factory.removeClient(mConnectionNameClient);
						mConnectionNameClient = null;
					}
					
					if (null != mDeviceInfoAgent){
						mDeviceInfoAgent.destory();
						mDeviceInfoAgent = null;
					}
				}else if (1 == msg.arg2){
					
				}else if (2 == msg.arg2){
					
				}
			}
		}
		
		public void sendGetDeviceInfo(){
			send(TYPE_DEVICE_INFO, ORITATION_OUT, 0, null);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/*
	 * 采样数据客户端，将采样数据显示到界面的同时，分别叠加，求得总和
	 */
	class MeasureDataClient extends DataClient implements WaterMonitorClientContract{
		private boolean measuredata = false;
		MeasureDataClient(){
			super(TYPE_MEASURE_DATA, ORITATION_IN);
		}

		@Override
		/*
		 * 显示工厂发过来的测量数据，并在统计模式下求和以备后续求平均数所用
		 */
		protected void doJob(Message msg) {
			measuredata = true;
			if (-1 == msg.arg2){
				disconnectOnClick();
			}else if(0 == msg.arg2){
				ContentValues data = (ContentValues)msg.obj;
				
				mDataCallback.updateTemp(String.format("%.2f", data.getAsFloat(DataEntry.COLUMN_NAME_TEMP).floatValue()));
				mDataCallback.updateBaro(String.format("%.2f", data.getAsFloat(DataEntry.COLUMN_NAME_BARO).floatValue()));
				
				float calculatedo = 0;
				float calculatesat = 0;
				calculatedo = data.getAsFloat(DataEntry.COLUMN_NAME_RDO).floatValue();
				calculatesat = data.getAsFloat(DataEntry.COLUMN_NAME_SAT).floatValue();
				mDataCallback.updateDO(String.format("%.2f", calculatedo*100));
				mDataCallback.updateSAT(String.format("%.2f", calculatesat));
				mStateCallback.setBattery(data.getAsInteger("batterystatus"));
				if (is_collect_data){
					String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
					ContentValues values = new ContentValues();
					values.put(DataEntry.COLUMN_NAME_TIME, time);
					//读取当前选择的地点id
					SharedPreferences preference = mActivity.getSharedPreferences(mActivity.getString(R.string.txt_location), Activity.MODE_PRIVATE);
					int location_id = preference.getInt(LocationListModel.GET_CONTENT_ID, 1);
					values.put(DataEntry.COLUMN_NAME_LOCATION_ID, location_id); 
					values.put(DataEntry.COLUMN_NAME_TEMP, data.getAsFloat(DataEntry.COLUMN_NAME_TEMP).floatValue());
			
					values.put(DataEntry.COLUMN_NAME_RDO, calculatedo);
					
					values.put(DataEntry.COLUMN_NAME_SAT, calculatesat);
					
					values.put(DataEntry.COLUMN_NAME_BARO, data.getAsFloat(DataEntry.COLUMN_NAME_BARO).floatValue());
					
					//保存到数据库
					mActivity.getContentResolver().insert(DataEntry.CONTENT_URI, values);
					
					is_collect_data = false;
				}
			}
		}
		
		boolean noMeasureData(){
			return !measuredata;
		}
		
		void sendMeasureRequest(int probe_index){
			send(TYPE_MEASURE_DATA, ORITATION_OUT, -1, probe_index);
			measuredata = false;
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			
		}
	}
	
	/*
	 * 采集数据工作线程
	 * 定时请求数据采集，并不会考虑上一次的采集是否成功
	 * 这里的逻辑还需商榷
	 */
	class UpdateDataThread extends Thread{
		private boolean needUpdateData = false;
		public int updateDelayTime = 500; //暂时定时为固定值，以后考虑增加设置
		private int updateTimeCount = 0;
		JobAgent jaUpdateData;
		MeasureDataClient mMeasureDataClient;
		int mProbeIndex = 0;
		
		UpdateDataThread(int probe_index, int delay_time){
			mProbeIndex = probe_index;
			updateDelayTime = delay_time;
			jaUpdateData = factory.getMeasureDataAgent();
			
			mMeasureDataClient = new MeasureDataClient();
			factory.putClient(mMeasureDataClient);
			
			needUpdateData = true;
			updateTimeCount = 0;
			mDataCallback.updateProgressMax(updateDelayTime - 1);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(needUpdateData){
				if (0 == updateTimeCount){
					mMeasureDataClient.sendMeasureRequest(mProbeIndex);
				}
				mDataCallback.updateProgress(updateTimeCount);
				try {
					sleep(800);
					updateTimeCount ++;
					updateTimeCount = updateTimeCount%updateDelayTime;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		void cancel(){
			needUpdateData = false;
			mDataCallback.updateProgress(0);
			jaUpdateData.destory();
			factory.removeClient(mMeasureDataClient);
		}
	}
}
