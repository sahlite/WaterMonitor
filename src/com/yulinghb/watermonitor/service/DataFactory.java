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
 * ���ݹ����࣬�������ӷ�ʽ�����Ӳ�����Ϊ����ͨ�������ṩ����ˣ��ṩλ����Ϣ�����ṩ���幤���Ĵ���
 */
public class DataFactory {
	// ģ����ʱ�ر���������
	public static final boolean BT_ON_DEVICE = true;
	
	// ΨһDataFactoryʵ��
	private static DataFactory mMyself;
	
	// ��������
	private BluetoothService btService;
	
	// λ����Ϣ����
	private LocationService locationService;
	
	// ����ͨ�������
	private DataServer server;
	
	// ���Դ���ģ�����ӵ����豸���ṩ����ͨѶ����
//	private JobAgent mTestAgent = null;
	
	// �豸��Ϣ�����ṩ���ӵ����豸��Ϣ�͸��¹���
//	private JobAgent mDeviceInfoAgent = null;
	
	private float salinity = 0f;
	
	// ˽�й��캯����ʹDataFactory�����Լ�ʵ�������ҳ���Ψһʵ��
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
	
	// ��ȡDataFactory��ʵ��
	public static DataFactory getInstance(){
		if (null == mMyself){
			mMyself = new DataFactory();
		}
		return mMyself;
	}
	
	// �������Ӧ��
	// ���񲿷ֹ�����Ҫ����Ӧ�ã�
	public void init(Activity mActivity){
		if (BT_ON_DEVICE){
			btService.setActivity(mActivity);
			locationService.setActivity(mActivity);
		}
	}
	
	/*
	 * �ṩһϵ�д���ʵ�־��幤��
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
	 * ������ز���
	 * Ŀǰֻ������һ�����ӷ�ʽ
	 * ��ȡӦ�÷����� ����״̬��������ر��������������������ܣ����ӻ�Ͽ�����
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
	 * ������ֹͣλ�÷���
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
	 * �󶨺ͽ���ͻ��˴�����
	 */
	public void putClient(DataClient c){
		c.bindServer(server);
	}
	
	public void removeClient(DataClient c){
		c.unbindServer(server);
	}
	
	// ��ȡ���ӵ���̽ͷ��Ϣ
	// ����CalibrationListActivity��ʾ��ʽ
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
