package com.yulinghb.watermonitor.service;

import android.content.ContentValues;
import android.os.Message;

import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * Calibrate DO for user proper
 * with two reference points using two point calibration
 * with one then one point calibration
 */
public class DOCalibrationAgent extends JobAgent {
	
	public static final int JOB_AIR_CALI = 0;
	public static final int JOB_ZERO_CALI = 1;
	public static final int JOB_SEC_AIR_CALI = 2;
	public static final int JOB_RESTORE = 3;
	public static final int JOB_SALINITY = 4;
	public static final int JOB_NOTHING = 5;
    private CalibrationDataClient mCalibrationDataClient;
    private float current_do = 0f;
    private float current_do_air = 0f;
    private float current_pressure = 0f;
    private float user_co0 = 1f;
    private float user_co1 = 0f;
    private int job_indicate = JOB_NOTHING;
    private float salinity = 0f;

	public DOCalibrationAgent(DataServer server) {
		super(server, true);
		// TODO Auto-generated constructor stub
		mCalibrationDataClient = new CalibrationDataClient();
    	mCalibrationDataClient.bindServer(mServer);
    	usingClient = mCalibrationDataClient;
	}

    @Override
	public void destory() {
		// TODO Auto-generated method stub
		super.destory();
		mCalibrationDataClient.unbindServer(mServer);
	}

	@Override
	protected void doProcess(int result, byte[] command, byte[] data, int len) {
		// TODO Auto-generated method stub
		super.doProcess(result, command, data, len);
		if (ProtocolStruct.OP_GET == command[ProtocolStruct.OP_TYPE]){
			switch(command[ProtocolStruct.OB_TYPE]){
				case ProtocolStruct.OB_DO:
					switch(job_indicate){
						case JOB_AIR_CALI:
						case JOB_ZERO_CALI:
							current_do = ProtocolStruct.GetDataDO(data);
							break;
						case JOB_SEC_AIR_CALI:
							current_do_air = ProtocolStruct.GetDataDO(data);
							break;
					}
					break;
				case ProtocolStruct.OB_PRESSURE:
					current_pressure = ProtocolStruct.GetDataPressure(data);
					break;
				case ProtocolStruct.OB_USR_CO:
					user_co0 = ProtocolStruct.GetDataUserCoL0(data);
					user_co1 = ProtocolStruct.GetDataUserCoL1(data);
					break;
			}
    	}else{
			switch(command[ProtocolStruct.OB_TYPE]){
			case ProtocolStruct.OB_DETECT:
				break;
			case ProtocolStruct.OB_USR_CO:
				break;
			case ProtocolStruct.OB_SALINITY:
				DataFactory.getInstance().setSalinity(salinity);
				break;
			}
    	}
		doUpdate(result);
	}

	// for the operations are separated
	// here afford two sequences as get DO and set coefficient
	public JobAgent beDOCalibrationAgent(){
		unitList.clear();
		
    	// get pressure from converter a.k.a smart meter
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetPressure(aUnit.bufWrite, aUnit.command, 
        														smarter.getAsByte(DEVICE_ID).byteValue(), 
        														smarter.getAsByteArray(DEVICE_SN));
        unitList.add(aUnit);
        
    	// active probe to get DO
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetLedActive(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DETECT_LED_RED_BLUE, 0f);
        unitList.add(aUnit);
    	
        // get coefficient saved in probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetUserCo(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
        
        //get DO from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetDO(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DO_DATA_CALCULATED);
        unitList.add(aUnit);
        
        // save adjusted coefficient to probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetUserCo(aUnit.bufWrite, aUnit.command, device_id, device_sn, 0f, 0f);
        unitList.add(aUnit);
        
        job_indicate = JOB_AIR_CALI;
        
        mUpdate = new OnDoUpdate(){
			@Override
			public void update() {
				// TODO Auto-generated method stub
				mCalibrationDataClient.sendAirResult(ProtocolStruct.RESULT_OK);
			}
        	
        };
        
    	return this;
    }
    
	public JobAgent beDOZeroCalibrationAgent(){
		unitList.clear();
		
    	// get pressure from converter a.k.a smart meter
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetPressure(aUnit.bufWrite, aUnit.command, 
        														smarter.getAsByte(DEVICE_ID).byteValue(), 
        														smarter.getAsByteArray(DEVICE_SN));
        unitList.add(aUnit);
        
    	// active probe to get DO
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetLedActive(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DETECT_LED_RED_BLUE, 0f);
        unitList.add(aUnit);
    	
        //get DO from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetDO(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DO_DATA_CALCULATED);
        unitList.add(aUnit); 
        
        job_indicate = JOB_ZERO_CALI;
        
        mUpdate = new OnDoUpdate(){
			@Override
			public void update() {
				// TODO Auto-generated method stub
				mCalibrationDataClient.sendZeroResult(ProtocolStruct.RESULT_OK);
			}
        	
        };
        
    	return this;
    }
	
	public JobAgent  beDOAirCalibrationAgent(){
		unitList.clear();
		
    	// get DO from probe
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetLedActive(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DETECT_LED_RED_BLUE, 0f);
        unitList.add(aUnit);
    	
        //get DO from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetDO(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DO_DATA_CALCULATED);
        unitList.add(aUnit);
        
        // save adjusted coefficient to probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetUserCo(aUnit.bufWrite, aUnit.command, device_id, device_sn, 0f, 0f);
        unitList.add(aUnit);
        
        job_indicate = JOB_SEC_AIR_CALI;
        
        mUpdate = new OnDoUpdate(){
			@Override
			public void update() {
				// TODO Auto-generated method stub
				mCalibrationDataClient.sendSecAirResult(ProtocolStruct.RESULT_OK);
			}
        	
        };
        
		return this;
	}

    @Override
	protected void packageFrame(FlowUnit unit) {
		// TODO Auto-generated method stub
		switch(unit.command[ProtocolStruct.OB_TYPE]){
		case ProtocolStruct.OB_DETECT:
			unit.bufWriteLen = ProtocolStruct.PackageSetLedActive(unit.bufWrite, unit.command, device_id, device_sn, 
																	ProtocolStruct.DETECT_LED_RED_BLUE, 
																	current_pressure);
			break;
		case ProtocolStruct.OB_USR_CO:
			switch(job_indicate){
				case JOB_AIR_CALI:
					if (ProtocolStruct.OP_SET == unit.command[ProtocolStruct.OP_TYPE]){
						user_co0 = user_co0/current_do;
						unit.bufWriteLen = ProtocolStruct.PackageSetUserCo(unit.bufWrite, unit.command, device_id, device_sn, 
								user_co0, user_co1);
					}
					break;

				case JOB_SEC_AIR_CALI:
					user_co0 = 1/(current_do_air - current_do);
					user_co1 = - user_co0 * current_do;
					unit.bufWriteLen = ProtocolStruct.PackageSetUserCo(unit.bufWrite, unit.command, device_id, device_sn, 
							user_co0, user_co1);
					break;
			}
			
			break;
		}
	}

	public JobAgent beDORestoreAgent(){
		unitList.clear();
		
    	//set calibration coefficient
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetUserCo(aUnit.bufWrite, aUnit.command, device_id, device_sn, 1f, 0f);
        unitList.add(aUnit);
        
        job_indicate = JOB_RESTORE;
        
        mUpdate = new OnDoUpdate(){
			@Override
			public void update() {
				// TODO Auto-generated method stub
				mCalibrationDataClient.sendRestoreResult(ProtocolStruct.RESULT_OK);
			}
        	
        };
    	return this;
    }
	
	public JobAgent beSetSalinityAgent(){
		unitList.clear();
		
    	//set calibration coefficient
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetSalinity(aUnit.bufWrite, aUnit.command, device_id, device_sn, salinity);
        unitList.add(aUnit);
        
        job_indicate = JOB_SALINITY;
        
        mUpdate = new OnDoUpdate(){
			@Override
			public void update() {
				// TODO Auto-generated method stub
				mCalibrationDataClient.sendSalinityResult(ProtocolStruct.RESULT_OK);
			}
        	
        };
    	return this;
    }
    
    /*
     * DO calibration client used to receive requests of get DO, or set coefficient 
     * response with DO , or result
     */
    class CalibrationDataClient extends DataClient implements WaterMonitorClientContract{
    	JobAgent ja;
    	
    	CalibrationDataClient(){
    		super(TYPE_CALIBRATION_DATA, ORITATION_OUT);
    	}

		@Override
		protected void doJob(Message msg) {
			// TODO Auto-generated method stub
			Thread delaythread;
			if (4 != msg.arg2){
				changeSNByIndex(((Integer)msg.obj).intValue());
			}
			
			switch(msg.arg2){
				case 0:
					// set device_id and device_sn to chose probe's
					ja = beDOZeroCalibrationAgent();
					delaythread = new Thread(){
						public void run(){
							try {
								sleep(10000);
								ja.doAct();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					delaythread.start();
					break;
					
				case 1:
					// set device_id and device_sn to chose probe's
					ja = beDOCalibrationAgent();
					ja.doAct();
					break;
					
				case 2:
					ja = beDORestoreAgent();
					ja.doAct();
					break;
					
				case 3:
					ja = beDOAirCalibrationAgent();
					delaythread = new Thread(){
						public void run(){
							try {
								sleep(10000);
								ja.doAct();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					delaythread.start();
					break; 
					
				case 4:
					ContentValues values = (ContentValues)msg.obj;
					int index = values.getAsInteger(WaterMonitorClientContract.SALINITY_INDEX);
					salinity = values.getAsFloat(WaterMonitorClientContract.SALINITY_VALUE);
					changeSNByIndex(index);
					ja = beSetSalinityAgent();
					ja.doAct();
					break;
			}
		}
		
		void sendZeroResult(int result){
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, 0, result);
		}
		
		void sendAirResult(int result){
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, 1, result);
		}
		
		void sendSecAirResult(int result){
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, 2, result);
		}
		
		void sendRestoreResult(int result){
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, 3, result);
		}
		
		void sendSalinityResult(int result){
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, 4, result);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			send(TYPE_CALIBRATION_DATA, ORITATION_IN, -1);
		}
    }
}
