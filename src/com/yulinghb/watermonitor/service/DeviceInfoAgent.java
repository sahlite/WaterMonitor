package com.yulinghb.watermonitor.service;

import java.util.ArrayList;

import android.content.ContentValues;
import android.os.Message;

import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * 设备信息代理
 * 接收设备信息请求，完成于设备通信获取设备信息，并反馈信息
 */
public class DeviceInfoAgent extends JobAgent {
	

	// 设备信息客户端
    private DeviceInfoClient mDeviceInfoClient;
    // 连接的probe索引，对应smarter的probe插口编号
    private byte[] probe_connected_index;
    // probe索引数组的索引
    private int probe_connected_index_index;
    // probe和smarter的信息列表，用于反馈
    private ArrayList<ContentValues> mDeviceList;
    
    private ContentValues probe_id_sn;

	public DeviceInfoAgent(DataServer server) {
		super(server, true);
		// TODO Auto-generated constructor stub
		
		mDeviceInfoClient = new DeviceInfoClient();
		mDeviceInfoClient.bindServer(server);
		usingClient = mDeviceInfoClient;
		//get id and sn of smarter
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetIdSN(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
        //get hardware version and software version of smarter
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetHWSW(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
    	//get the state of probes connected to smarter
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeNum(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
        //for the commands are predefined, all eight request commands list below
        //according to state, select desire command to send
    	//get the sn of probe connected to first slot
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x00);
        unitList.add(aUnit);
        //get hardware version and software version of probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetHWSW(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
//        //get the sn of probe connected to second slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x01);
//        unitList.add(aUnit);
//        //get the sn of probe connected to third slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x02);
//        unitList.add(aUnit);
//        //get the sn of probe connected to forth slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x03);
//        unitList.add(aUnit);
//      //get the sn of probe connected to fifth slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x04);
//        unitList.add(aUnit);
//        //get the sn of probe connected to sixth slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x05);
//        unitList.add(aUnit);
//        //get the sn of probe connected to seventh slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x06);
//        unitList.add(aUnit);
//        //get the sn of probe connected to eighth slot
//    	aUnit = new FlowUnit();
//        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(aUnit.bufWrite, aUnit.command, device_id, device_sn, (byte)0x07);
//        unitList.add(aUnit);
        
        mUpdate = new OnDoUpdate(){

			@Override
			public void update() {
				// TODO Auto-generated method stub
				// before feedback the list, add smarter to the end
				mDeviceList.add(smarter);
				mDeviceInfoClient.sendDeviceInfo(mDeviceList);
			}
        	
        };
        //initial
        probe_connected_index_index = 0;
        probe_connected_index = new byte[ProtocolStruct.PROBE_NUM + 1];
        probes.removeAll(probes);
        probe_state = 0x00;
        
        mDeviceList = new ArrayList<ContentValues>();
	}



	@Override
	protected void packageFrame(FlowUnit unit) {
		// TODO Auto-generated method stub
		switch(unit.command[ProtocolStruct.OB_TYPE]){
		case ProtocolStruct.OB_ID_SN:
			unit.bufWriteLen = ProtocolStruct.PackageGetIdSN(unit.bufWrite, unit.command, device_id, device_sn);
			break;
		case ProtocolStruct.OB_PROBE_NUM:
			unit.bufWriteLen = ProtocolStruct.PackageGetProbeNum(unit.bufWrite, unit.command, device_id, device_sn);
			break;
		case ProtocolStruct.OB_PROBE_SN:
			unit.bufWriteLen = ProtocolStruct.PackageGetProbeSN(unit.bufWrite, unit.command, device_id, device_sn, probe_connected_index[probe_connected_index_index]);
			break;
		case ProtocolStruct.OB_HW_SW:
			if (2 < curr_index)
			{
				unit.bufWriteLen = ProtocolStruct.PackageGetHWSW(unit.bufWrite, unit.command, device_id, device_sn);
			}
			break;
		}
	}



	@Override
	protected void doProcess(int result, byte[] command, byte[] data, int len) {
		// TODO Auto-generated method stub
		int myResult = result;
		super.doProcess(result, command, data, len);
		if (ProtocolStruct.OP_GET == command[ProtocolStruct.OP_TYPE]){
			switch(command[ProtocolStruct.OB_TYPE]){
			case ProtocolStruct.OB_ID_SN:
				device_id = ProtocolStruct.GetDataIdSN(data, device_sn);
				smarter.put(DEVICE_ID, device_id);
				smarter.put(DEVICE_SN, device_sn);
				smarter.put(PROBE_INDEX, SMARTER_INDEX);
				break;
				
			case ProtocolStruct.OB_PROBE_NUM:
				// get the situation of probes connected
				probe_state = ProtocolStruct.GetDataProbeState(data);
				if (0 == probe_state){
					curr_index += ProtocolStruct.PROBE_NUM - 1;
					break;
				}
				int probe_i = 0;
				for (byte i=0;i<ProtocolStruct.PROBE_NUM;i++){
					if (ProtocolStruct.isConnectedProbe(probe_state, i)){
						probe_connected_index[probe_i] = i;
						probe_i += 1;
					}
				}
				probe_connected_index[probe_i] = (byte)0xff;
				probe_connected_index_index = 0;
				break;
			case ProtocolStruct.OB_PROBE_SN:
				byte[] probe_sn = new byte[ProtocolStruct.SN_LEN];
				byte probe_index = ProtocolStruct.GetDataProbeIndexSN(data, probe_sn);
				// for some circumstances, we get extra data from connection.
				// so be driven to a unexpected flow to restart the request
				// then here comes the extra check for index array for sure
				if ((probe_index == probe_connected_index[probe_connected_index_index])
						&&((byte)0xff != probe_connected_index[probe_connected_index_index]))
				{
					probe_id_sn = new ContentValues();
					probe_id_sn.put(DEVICE_ID, ProtocolStruct.GetIdInSN(probe_sn));
					probe_id_sn.put(DEVICE_SN, probe_sn);
					probe_id_sn.put(PROBE_INDEX, probe_index);
					probes.add(probe_id_sn);
					mDeviceList.add(probe_id_sn);
					probe_connected_index_index += 1;
					if ((byte)0xff != probe_connected_index[probe_connected_index_index]){
						curr_index -= 1;
					}
				}
				
				break;
				
			case ProtocolStruct.OB_HW_SW:
				byte hwmaj = ProtocolStruct.GetDataHWMaj(data);
				byte hwmin = ProtocolStruct.GetDataHWMin(data);
				byte swmaj = ProtocolStruct.GetDataSWMaj(data);
				byte swmin = ProtocolStruct.GetDataSWMin(data);
				if (2 >= curr_index)
				{
					smarter.put(HW_VERSION, String.valueOf(hwmaj) + "." + String.valueOf(hwmin));
					smarter.put(SW_VERSION, String.valueOf(swmaj) + "." + String.valueOf(swmin));
				}
				else
				{
					probe_id_sn.put(HW_VERSION, String.valueOf(hwmaj) + "." + String.valueOf(hwmin));
					probe_id_sn.put(SW_VERSION, String.valueOf(swmaj) + "." + String.valueOf(swmin));
				}
				break;
				
			default:
				myResult = ProtocolStruct.RESULT_OB_ERR;
				break;
			}
		}
		doUpdate(myResult);
	}



	@Override
	public void destory() {
		// TODO Auto-generated method stub
		super.destory();
		mDeviceInfoClient.unbindServer(mServer);
	}
	
	// clear all devices information saved
	void clear(){
		// remove all smart meter and probes information
		smarter.clear();
		probes.clear();
		
		// reset current used device id and sn
		device_id = ProtocolStruct.DEFAULT_ID;
        for (int i = 0; i < ProtocolStruct.SN_LEN; i++)
        {
            device_sn[i] = (byte)ProtocolStruct.DEFAULT_SN[i];
        }
	}

	/*
	 * Device information client used to receive the request and response with devices' information
	 */
	class DeviceInfoClient extends DataClient implements WaterMonitorClientContract{
    	DeviceInfoClient(){
    		super(TYPE_DEVICE_INFO, ORITATION_OUT);
    	}

		@Override
		protected void doJob(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.arg2){
			case 0:
				doAct();
				break;
			case 1:
				clear();
				break;
			}
			
		}
		
		void sendDeviceInfo(Object obj){
			send(TYPE_DEVICE_INFO, ORITATION_IN, 0, obj);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			send(TYPE_DEVICE_INFO, ORITATION_IN, -1);
		}
    }

}
