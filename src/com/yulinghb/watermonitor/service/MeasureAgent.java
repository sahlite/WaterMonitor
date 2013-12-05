package com.yulinghb.watermonitor.service;

import android.content.ContentValues;
import android.os.Message;

import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * Measure data agent used to communicate through connection 
 * and provide data from smarter and given probe once
 */
public class MeasureAgent extends JobAgent {
	
    private MeasureDataClient mMeasureDataClient;
	private float current_do = 0f;
	private float current_pressure = 0f;
	private float current_temperature = 0f;
	
	CommandThread mCommandThread;

	public MeasureAgent(DataServer server) {
		super(server, true);
		// TODO Auto-generated constructor stub
    	mMeasureDataClient = new MeasureDataClient();
    	mMeasureDataClient.bindServer(mServer);
    	usingClient = mMeasureDataClient;
    	
    	//get baro from smart meter
		unitList.removeAll(unitList);
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetPressure(aUnit.bufWrite, aUnit.command, 
        															smarter.getAsByte(DEVICE_ID), 
        															smarter.getAsByteArray(DEVICE_SN));
        unitList.add(aUnit);
    	//active probe to calculate do
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetLedActive(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DETECT_LED_RED_BLUE, 0f);
        unitList.add(aUnit);
        //get temperature
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetTemperature(aUnit.bufWrite, aUnit.command, device_id, device_sn);
        unitList.add(aUnit);
    	//get do from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetDO(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.DO_DATA_CALCULATED);
        unitList.add(aUnit);
        
        mUpdate = new OnDoUpdate(){

			@Override
			public void update() {
				// TODO Auto-generated method stub
				// here suppose to be eight parameters for a complete recorder
				// as this time we can only get four of them
				// just leave others zero
				ContentValues product = new ContentValues();
				float salinity = DataFactory.getInstance().getSalinity();
				float saturation = (float)calculateSaturation(current_do, current_temperature, current_pressure, salinity);
				product.put(DataEntry.COLUMN_NAME_TEMP, current_temperature);
				product.put(DataEntry.COLUMN_NAME_DEPTH, 0f);
				product.put(DataEntry.COLUMN_NAME_RDO, current_do);
				product.put(DataEntry.COLUMN_NAME_SAT, saturation);
				product.put(DataEntry.COLUMN_NAME_ORP, 0f);
				product.put(DataEntry.COLUMN_NAME_PH, 0f);
				product.put(DataEntry.COLUMN_NAME_ACT, 0f);
				product.put(DataEntry.COLUMN_NAME_BARO, current_pressure);
				product.put("batterystatus", batterystatus);
				mMeasureDataClient.sendProduct(product);
			}
        	
        };
    }
	
	public double calculateSaturation(float dissolveOxy, float temperatureinc, float pressureinkpa, float salinity){
		double saturation = 0f;
		double temperatureink = 273.5+temperatureinc;
		
		/*
		 * ln DO = Al + A2 100/T + A3 ln T/1OO + A4 T/1OO 
		 *         + S [B1 + B2 T/100 + B3 (T/100)2]
		 * Al = -173.4292                 T = temperature  degrees K
		 * A2 = 249.6339                      (273.15 + t  degrees C)
		 * A3 = 143.3483
		 * A4 = -21.8492                 
		 *                 
         * Bl = -0.033096				  S = salinity in g/kg (o/oo)
		 * B2 =  0.014259
		 * B3 = -0.001700
		 */
		saturation = -173.4292 
				+ 249.6339*(100f/temperatureink) 
				+ 143.3483*Math.log(temperatureink/100)
				+ -21.8492*(temperatureink/100)
				+ salinity*(-0.033096
						    + 0.014259*(temperatureink/100)
						    + -0.001700*(temperatureink/100)*(temperatureink/100));
		
		saturation = Math.exp(saturation);
		
		/*
		 * 1 ml/L = 1.4276 mg/L
		 */
		saturation = saturation*1.4276;
		
		// log u = 8.10765 - (1750.286/ (235+t))
		double u = 8.10765 - (1750.286/ (235+temperatureinc));
		u = Math.pow(10, u);
		
		double pressureinhmgg = pressureinkpa*760/101.325;
		// DO' = D0! (P-u/760-u)
		saturation = saturation * ((pressureinhmgg-u)/(760-u));
		saturation = saturation*dissolveOxy;
		return saturation;
	}


	@Override
	protected void doProcess(int result, byte[] command, byte[] data, int len) {
		// TODO Auto-generated method stub
		int myResult = result;
		super.doProcess(result, command, data, len);
    	if (ProtocolStruct.OP_GET == command[ProtocolStruct.OP_TYPE]){
			switch(command[ProtocolStruct.OB_TYPE]){
			case ProtocolStruct.OB_PRESSURE:
				current_pressure = ProtocolStruct.GetDataPressure(data);
				break;
				
			case ProtocolStruct.OB_DO:
				current_do = ProtocolStruct.GetDataDO(data);
				break;
			case ProtocolStruct.OB_TEMPERATURE:
				current_temperature = ProtocolStruct.GetDataTemperature(data);
				break;
			default:
				myResult = ProtocolStruct.RESULT_OB_ERR;
				break;
			}
    	}else{
			switch(command[ProtocolStruct.OB_TYPE]){
			case ProtocolStruct.OB_DETECT:
				break;
			default:
				myResult = ProtocolStruct.RESULT_OB_ERR;
				break;
					
			}
    	}
    	if ((ProtocolStruct.OP_SET == command[ProtocolStruct.OP_TYPE])
    			&&(ProtocolStruct.OB_DETECT == command[ProtocolStruct.OB_TYPE])){
    		if (null == mCommandThread){
    			mCommandThread = new CommandThread();
    			mCommandThread.putResult(myResult);
    			mCommandThread.start();
    		}
    		
    	}else{
    		doUpdate(myResult);
    	}
    		
    	
//    	if (null != mCommandThread){
//    		mCommandThread = null;
//    	}
//    	mCommandThread = new CommandThread();
//    	mCommandThread.putResult(result);
//    	mCommandThread.start();
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
			
		case ProtocolStruct.OB_DO:
			if (null != mCommandThread){
	    		mCommandThread = null;
	    	}
			unit.bufWriteLen = ProtocolStruct.PackageGetDO(unit.bufWrite, unit.command, device_id, device_sn, ProtocolStruct.DO_DATA_CALCULATED);
			break;
			
		case ProtocolStruct.OB_TEMPERATURE:
			unit.bufWriteLen = ProtocolStruct.PackageGetTemperature(unit.bufWrite, unit.command, device_id, device_sn);
			break;

		}
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub
		mMeasureDataClient.unbindServer(mServer);
		mMeasureDataClient = null;
		super.destory();
	}
	
	// Thread to delay enough time for device process
	class CommandThread extends Thread{
		
		private int myResult;
		
		void putResult(int aResult){
			myResult = aResult;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doUpdate(myResult);
		}
	}

    // Measure data client used to receive request and response with data collected once
	class MeasureDataClient extends DataClient implements WaterMonitorClientContract{

    	MeasureDataClient(){
    		super(TYPE_MEASURE_DATA, ORITATION_OUT);
    	}

		@Override
		protected void doJob(Message msg) {
			// set device_id and device_sn to chose probe's
			changeSNByIndex(((Integer)msg.obj).intValue());
			
//			// first get baro
//			beBaroAgent();
			curr_index = 0;
			doAct();
		}
		
		void sendProduct(Object obj){
			send(TYPE_MEASURE_DATA, ORITATION_IN, JobAgent.COMMAND_DATA, obj);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			send(TYPE_MEASURE_DATA, ORITATION_IN, -1);
		}
    }
}
