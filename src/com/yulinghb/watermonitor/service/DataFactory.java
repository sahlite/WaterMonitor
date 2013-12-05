package com.yulinghb.watermonitor.service;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;

import com.yulinghb.watermonitor.LocationService;
import com.yulinghb.watermonitor.AppModel.CalibrationListModel;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * 数据工厂类，控制连接方式和连接操作，为数据通道功能提供服务端，提供位置信息服务，提供具体工作的代理
 */
public class DataFactory {
	// 模拟器时关闭蓝牙功能
	public static final boolean BT_ON_DEVICE = true;
	
	// 唯一DataFactory实例
	private static DataFactory mMyself;
	
	// 蓝牙服务
	private BluetoothService btService;
	
	// 位置信息服务
	private LocationService locationService;
	
	// 数据通道服务端
	private DataServer server;
	
	// 测试代理，模拟连接到的设备，提供数据通讯测试
//	private JobAgent mTestAgent = null;
	
	// 设备信息代理，提供连接到的设备信息和更新功能
//	private JobAgent mDeviceInfoAgent = null;
	
	private float salinity = 0f;
	
	// 私有构造函数，使DataFactory仅可自己实例化，且持有唯一实例
	private DataFactory(){
		server = new DataServer();
		if (BT_ON_DEVICE){
			btService = new BluetoothService(server);
			locationService = new LocationService(server);
		}else{
//			mTestAgent = new TestAgent(server);
		}
//		mDeviceInfoAgent = getDeviceInfoAgent();
	}
	
	// 获取DataFactory的实例
	public static DataFactory getInstance(){
		if (null == mMyself){
			mMyself = new DataFactory();
		}
		return mMyself;
	}
	
	// 将服务绑定应用
	// 服务部分功能需要依赖应用，
	public void init(Activity mActivity){
		if (BT_ON_DEVICE){
			btService.setActivity(mActivity);
			locationService.setActivity(mActivity);
		}
	}
	
	/*
	 * 提供一系列代理，实现具体工作
	 */
	public JobAgent getDOCalibrationAgent(){
		return new DOCalibrationAgent(server);
	}
	
	public JobAgent getMeasureDataAgent(){
		return new MeasureAgent(server);
	}
	
	public JobAgent getDeviceInfoAgent(){
		return new DeviceInfoAgent(server);
	}
	
	/*
	 * 连接相关操作
	 * 目前只有蓝牙一种连接方式
	 * 获取应用反馈， 蓝牙状态，开启或关闭蓝牙服务，启动蓝牙功能，连接或断开连接
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		btService.onActivityResult(requestCode, resultCode, data);
	}
	
	public boolean isConnectionEnabled(){
		if (!BT_ON_DEVICE)
			return false;
		return btService.isEnabled();
	}
	
	public void enableConnection(){
		if (BT_ON_DEVICE)
			btService.enable();
	}
	
	public void startConnectionServer(){
		if (BT_ON_DEVICE)
			btService.startServer();
	}
	
	public void stopConnectionServer(){
		if (BT_ON_DEVICE)
			btService.stopServer();
	}
		
	public void connect(){
		if (BT_ON_DEVICE)
			btService.connect();
	}
	
	public void disconnect(){
		if (BT_ON_DEVICE)
			btService.disconnect();
	}

	/*
	 * 启动和停止位置服务
	 */
	public void onStart(){
//		if ((BT_ON_DEVICE)&&(!locationService.isStart())){
//			locationService.start();
//		}
	}
	
	public void onStop(){
//		if ((BT_ON_DEVICE)&&(locationService.isStart())){
//			locationService.stop();
//		}
		stopConnectionServer();
		disconnect();
	}
	
	/*
	 * 绑定和解除客户端处理者
	 */
	public void putClient(DataClient c){
		c.bindServer(server);
	}
	
	public void removeClient(DataClient c){
		c.unbindServer(server);
	}
	
	// 获取连接到的探头信息
	// 按照CalibrationListActivity显示格式
	public ArrayList<ContentValues> getProbesID(){
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int size = JobAgent.getProbeNum();
		for(int i=0;i<size;i++){
			ContentValues item = new ContentValues();
			ContentValues probe = JobAgent.getProbeIDSN(i);
			item.put(CalibrationListModel.PROBE_INDEX, probe.getAsInteger(DeviceInfoAgent.PROBE_INDEX));
			if (0x01 == probe.getAsByte(DeviceInfoAgent.DEVICE_ID)){
				item.put(CalibrationListModel.PROBE_TYPE, CalibrationListModel.PROBE_TYPE_DO);
			}else{
				item.put(CalibrationListModel.PROBE_TYPE, CalibrationListModel.PROBE_TYPE_UNKOWN);
			}
			result.add(item);
		}
		return result;
	}
	
	public String getSmarterSN(){
		ContentValues idsn = JobAgent.getSmarterIDSN();
		if (null == idsn.getAsByteArray(JobAgent.DEVICE_SN)){
			return null;
		}
		
		return new String(idsn.getAsByteArray(JobAgent.DEVICE_SN));
	}
	
	public String getSmarterHW(){
		return JobAgent.getSmarterIDSN().getAsString(JobAgent.HW_VERSION);
	}
	
	public String getSmarterSW(){
		return JobAgent.getSmarterIDSN().getAsString(JobAgent.SW_VERSION);
	}
	
	public int getProbeNum(){
		return JobAgent.getProbeNum();
	}
	
	public String getProbeSN(int index){
		ContentValues idsn = JobAgent.getProbeIDSN(index);
		if (null == idsn.getAsByteArray(JobAgent.DEVICE_SN)){
			return null;
		}
		
		return new String(idsn.getAsByteArray(JobAgent.DEVICE_SN));
	}
	
	public String getProbeHW(int index){
		return JobAgent.getProbeIDSN(index).getAsString(JobAgent.HW_VERSION);
	}
	
	public String getProbeSW(int index){
		return JobAgent.getProbeIDSN(index).getAsString(JobAgent.SW_VERSION);
	}
	
	public void cleardeviceinfo(){
		JobAgent.clearall();
	}
	
	public float getSalinity(){
		return salinity;
	}
	
	public void setSalinity(float ins){
		salinity = ins;
	}
}
