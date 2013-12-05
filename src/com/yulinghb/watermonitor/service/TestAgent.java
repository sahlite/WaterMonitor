package com.yulinghb.watermonitor.service;

import com.yulinghb.watermonitor.DataManager.DataServer;

// Test agent give us a way without real connection to test if the server and client system work smoothly
// just add responses needed below and register to server, then you can test new clients
// DONT FORGET TO SET BT_ON_SERVICE FALSE IN DATAFACTORY
public class TestAgent extends JobAgent {
	public TestAgent(DataServer server) {
		super(server, false);
		// TODO Auto-generated constructor stub
		mRawDataClient = new TestClient();
		mRawDataClient.bindServer(server);
		//get id and sn to make sure probe ready to work
    	FlowUnit aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetIdSNResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK);
        unitList.add(aUnit);
    	//active probe to calculate do
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetLedActiveResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK);
        unitList.add(aUnit);
    	//get do from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetDOResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK, 201.4f);
        unitList.add(aUnit);
        //active probe to calculate do
    	aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageSetUserCoResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK);
        unitList.add(aUnit);
        //get temperature from probe
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetTemperatureResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK, 27.2f);
        unitList.add(aUnit);
        //get probe state
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeNumResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK, (byte)0x8D);
        unitList.add(aUnit);
        //get probe sn
        aUnit = new FlowUnit();
        aUnit.bufWriteLen = ProtocolStruct.PackageGetProbeSNResponse(aUnit.bufWrite, aUnit.command, device_id, device_sn, ProtocolStruct.RESULT_OK, (byte)0x00, "YL01FFFFFFFFFF".getBytes());
        unitList.add(aUnit);
	}

	class TestClient extends RawDataClient{
    	TestClient(){
    		super(TYPE_RAW_DATA, ORITATION_OUT);
    	}
    }
}
