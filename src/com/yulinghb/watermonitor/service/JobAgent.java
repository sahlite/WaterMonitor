package com.yulinghb.watermonitor.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.Message;

import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.DataManager.DataServer;

/*
 * JobAgent提供完整的通讯数据发送和接收处理，
 * 使用ProtocolStruct的接口完成数据包的协议解析和封包
 * 
 */
public class JobAgent
{
	/*
	 * 测试用RawDataClient的方向，
	 * 0  接收数据，处理后转交应用
	 * 1 发送数据，模拟连接的设备功能
	 */
	public static final int TEST_DOWNLOAD = 0;
	public static final int TEST_TEST = 1;
	
	/*
	 * MeasureData
	 */
	public static final int COMMAND_DATA = 0;
	
	/*
	 * 连接到的设备信息
	 * DEVICE_ID　　　　设备类型
	 * DEVICE_SN　　　　设备串号
	 * PROBE_INDEX　　　对应接口号
	 */
	public static final String DEVICE_ID = "device id";
	public static final String DEVICE_SN = "device sn";
	public static final String HW_VERSION = "hardware version";
	public static final String SW_VERSION = "software version";
	public static final String PROBE_INDEX = "probe index";
	
	/*
	 * smarter 设备接口号
	 */
	public static final byte SMARTER_INDEX = (byte)0xff;
	
	/*
	 * smarter的设备信息
	 * 所有接口上已连接probe的设备信息
	 * 获得的probe连接状态字
	 */
	protected static ArrayList<ContentValues> probes = new ArrayList<ContentValues>();
	protected static ContentValues smarter = new ContentValues();
	protected static byte probe_state = 0x00; 

	/*
	 * 从连接设备得到的数据存入rbuf中，in_buf_index为即将存入的索引
	 */
    private int in_buf_index = 0;
    private int retrys = 0;
    static final int MAX_RETRY = 3;
    private byte[] rbuf = new byte[ProtocolStruct.COMMAND_MAX_SIZE];
    private int rbuf_len;
    
    /*
     * 当前实用的id和sn
     * 由于可以同连接的smarter和其上接入的probe通讯 ，
     * 在操作时，需要指明希望通讯的设备，通常是probe
     */
    protected byte device_id;
    protected byte[] device_sn = new byte[ProtocolStruct.SN_LEN];
    
    /*
     * 当前实例为主端还是从端
     * 应用中实用代理都是主端，主动控制操作流程
     * 只有测试用代理为从端，模拟从设备即连接设备
     */
    protected boolean isMaster = true; 
    private boolean isWorking = false;
    
    /*
     * 服务端引用
     */
    protected DataServer mServer;
    
    /*
     * 与连接通信客户端
     */
    protected RawDataClient mRawDataClient;

    /*
     * 通讯指令序列
     * 子类通过建立自己的通讯指令序列，实现线性地与连接设备通讯
     */
	class FlowUnit{
		byte[] bufWrite = new byte[ProtocolStruct.COMMAND_MAX_SIZE];
		int bufWriteLen;
		byte[] command = new byte[ProtocolStruct.COMMAND_CORE_SIZE];
	}
	
	protected FlowUnit curr_unit; 
	protected int curr_index = 0;
	protected List<FlowUnit> unitList = new ArrayList<FlowUnit>();
	
	GuardThread mGuardThread = null;
	protected DataClient usingClient = null;
	
	protected int batterystatus = 0;
	
    /*
     * 更新UI接口
     * 子类将通过实例化mUpdate实现流程结束后将数据发送到UI
     */
    interface OnDoUpdate{
    	void update();
    }
	OnDoUpdate mUpdate = null;
    
	/*
	 * 初始化id和sn，使用协议默认值
	 * RawDataClient 用于通讯数据收发
	 */
    public JobAgent(DataServer server, boolean isRoleMaster)
    {
        device_id = ProtocolStruct.DEFAULT_ID;
        for (int i = 0; i < ProtocolStruct.SN_LEN; i++)
        {
            device_sn[i] = (byte)ProtocolStruct.DEFAULT_SN[i];
        }
        
        mServer = server; 
        mUpdate = null;
        
		
    	isMaster = isRoleMaster;
    	if (isMaster){
    		mRawDataClient = new DownloadClient();
        	mRawDataClient.bindServer(mServer);
    	}
    	
    }

    /*
     * 如果为主端开始执行预设序列
     */
    public void doAct(){
    	if ((isMaster)&&(0 == curr_index)){
    		doSend();
    		isWorking = true;
    		
    		if (null != mGuardThread){
    			mGuardThread = null;
    		}
    		mGuardThread = new GuardThread();
    		mGuardThread.start();
    	}
    }
    
    /*
     * 发送一帧数据
     */
    private void doSend(){
    	if (!isWorking){
    		curr_unit = unitList.get(curr_index);
    		packageFrame(curr_unit);
        	in_buf_index = 0;
        	mRawDataClient.sendRawData(curr_unit.bufWrite, curr_unit.bufWriteLen);
    	}
    }
    
    /*
     * 处理一帧数据
     */
    private void doFetch(){
		rbuf_len = in_buf_index;
		byte[] status = new byte[ProtocolStruct.CS_LEN+ProtocolStruct.PS_LEN];
		byte[] command = new byte[ProtocolStruct.COMMAND_CORE_SIZE];
        byte[] data = new byte[ProtocolStruct.COMMAND_MAX_SIZE];
        int[] data_len = new int[2];
        
        if (null != mGuardThread){
        	mGuardThread.answer();
        }
        
        int result = ProtocolStruct.PackageCommandDown(rbuf_len, rbuf, command, 
							                		data, data_len, status, 
							                		device_id, device_sn);
        
        if ((ProtocolStruct.RESULT_ID_ERR == result)||(ProtocolStruct.RESULT_ID_SN_ERR == result)){
        	if (0 != smarter.size())
	        	result = ProtocolStruct.PackageCommandDown(rbuf_len, rbuf, command, 
	            		data, data_len, status, 
	            		smarter.getAsByte(DEVICE_ID).byteValue(), 
	            		smarter.getAsByteArray(DEVICE_SN));
        }
        batterystatus = ProtocolStruct.GetBatteryStatus(status);
    	if ((ProtocolStruct.RESULT_OK == result)&&isMaster)
        {
    		if (null == curr_unit){
    			return;
    		}
    			
        	result = ProtocolStruct.IsResponseFor(curr_unit.command, command)?ProtocolStruct.RESULT_OK:ProtocolStruct.RESULT_COMMAND_ERR;
        }
    	doProcess(result, command, data, data_len[0]);
    	rbuf = new byte[ProtocolStruct.COMMAND_MAX_SIZE];
    }
    
    /*
     * 只处理get id sn帧，其余帧交给具体代理去处理
     */
    protected void doProcess(int result, byte[] command, byte[] data, int len){
    	if (isMaster){
        	retrys ++;
        	switch(result){
        	case ProtocolStruct.RESULT_OK:
        		curr_index += 1;
        			retrys = 0;
//        			if (ProtocolStruct.OP_GET == command[ProtocolStruct.OP_TYPE]){
//        				switch(command[ProtocolStruct.OB_TYPE]){
//        				case ProtocolStruct.OB_ID_SN:
//        					
//        					break;
//        				}
//        			}else if (ProtocolStruct.OP_SET == command[ProtocolStruct.OP_TYPE]){
//
//        				
//        			}else{
//        				
//        			}
        		break;
        	case ProtocolStruct.RESULT_HEAD_ERR:
        		break;
        	case ProtocolStruct.RESULT_UNFINISHED_PACKAGE:
        		break;
    		default:
        			
        	}
        	if (MAX_RETRY <= retrys){
        		// out of times
        	}else{
        		
        	}
    	}else{
    		// 作为从端找到一帧合适的应答帧，并发送
    		if (ProtocolStruct.RESULT_OK == result){
          		for (int i=0; i < unitList.size(); i++){
          			FlowUnit tmpUnit = unitList.get(i);
          			if (ProtocolStruct.IsRequestFor(tmpUnit.command, command)){
          				curr_index = i;
          				break;
          			}
          		}
          		doSend();
      		}
    	}
    }
    
    /*
     * 由具体代理在doProcess中，处理过自己需要的帧后调用，判断是否继续发送帧还是反馈结果
     */
    protected void doUpdate(int result){
    	isWorking = false;
    	if ((ProtocolStruct.RESULT_OK != result)||(curr_index >= unitList.size())){
			curr_index = 0;
			if (null != mUpdate){
				mUpdate.update();
			}
		}else{
			if ((isMaster)&&(ProtocolStruct.RESULT_OK == result))
				doSend();
		}
    }
    
    /*
     * 在代理释放前，需要将处理者从服务端解除掉
     */
    public void destory(){
    	mRawDataClient.unbindServer(mServer);
    }
    
    /*
     *  组帧
     */
    protected void packageFrame(FlowUnit unit){
    }
    
    /*
     * 变成当前使用的id和sn
     */
    protected void changeSNByIndex(int index){
    	// set device_id and device_sn to chose probe's
		ContentValues probe_data = probes.get(index);
		device_id = probe_data.getAsByte(DEVICE_ID).byteValue();
		for(int i=0;i<ProtocolStruct.SN_LEN;i++){
			device_sn[i] = probe_data.getAsByteArray(DEVICE_SN)[i];
		}
    }
    
    /*
     *  为DataFactory获取连接到的probe信息
     */
    public static int getProbeNum(){
    	return probes.size();
    }
    
    public static ContentValues getProbeIDSN(int index){
    	return probes.get(index);
    }
    
    public static ContentValues getSmarterIDSN(){
    	return smarter;
    }
    
    public static void clearall(){
    	probes.removeAll(probes);
    	smarter.clear();
    	probe_state = 0x00;
    }
    
    /*
     * 主端通讯客户端
     */
    class DownloadClient extends RawDataClient{
    	DownloadClient(){
    		super(TYPE_RAW_DATA, ORITATION_IN);
    	}
    }
   
    /*
     * 通讯客户端
     */
    class RawDataClient extends DataClient implements WaterMonitorClientContract{
    	private int partener = ORITATION_OUT;
    	 private boolean hasHead = false;
    	
    	protected RawDataClient(int type, int oritation){
    		super(type, oritation);
    		partener = (ORITATION_OUT == oritation)?ORITATION_IN:ORITATION_OUT;
    	}

		@Override
		protected void doJob(Message msg) {
				int data_len = (int)msg.arg2;
				int head_index = 0;
				int data_full_length = 0;
				byte[] data = (byte[])msg.obj;
				for (head_index=0; !hasHead&&(head_index<data_len);head_index++){
					if (((isMaster)&&(ProtocolStruct.IsRSPHead(data[head_index])))
							||(!isMaster)&&(ProtocolStruct.IsREQHead(data[head_index]))){
						hasHead = true;
						break;
					}
				}
				for (int i = head_index; i < data_len; i++, in_buf_index++){
					if (ProtocolStruct.COMMAND_MAX_SIZE <= in_buf_index){
						in_buf_index = 0;
					}
		            rbuf[in_buf_index] = data[i];
		        }
				data_full_length = ProtocolStruct.getPackageLen(rbuf, in_buf_index);
				if(data_full_length <= in_buf_index){
					hasHead = false;
					doFetch();
				}
		}
		
		public void sendRawData(byte[] buffer, int len){
			send(TYPE_RAW_DATA, partener, len, buffer);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			
		}
    }
    
    class GuardThread extends Thread{

    	private boolean isNotAnswered = true;
		@Override
		public void run() {
			// TODO Auto-generated method stub
//			super.run();
			try {
				sleep(5000);
				if ((isNotAnswered)&&(null != usingClient)){
					usingClient.sendExpired();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void answer(){
			isNotAnswered = false;
		}
    }
}