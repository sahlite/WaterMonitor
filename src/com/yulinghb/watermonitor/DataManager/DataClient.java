package com.yulinghb.watermonitor.DataManager;

import android.os.Handler;
import android.os.Message;

/*
 * DataClient���DataServer�࣬������Ϣϵͳ��ÿ��Client������Ϣ��Server,
 * Ȼ����Server�ַ���ÿ��Client���˴˲���Ҫ֪���Է����򻯸���Э������Ĺ�ϵ
 * ��Ϣ��what��arg1��DataClient�ж��壬�ṩͨ�õ�Լ����arg2����Э���������淶
 * ÿ��Client����֮���һ����Ϣ���д���ͨ�����ж����Ϣ�������ֱ���ͬ����Ϣ
 */
public abstract class DataClient {
	/*
	 * ���ӵ��ķ���˴�����
	 * �Լ��Ŀͻ��˴�����
	 */
	private Handler sender;
	private final DataClientHandler receiver;
	
	/*
	 * �ܹ��������Ϣ���ͺͷ���
	 */
	private int type;
	private int oritation;
	
	protected DataClient(int what, int arg1){
		type = what;
		oritation = arg1;
		receiver = new DataClientHandler();
	}

	/*
	 * ������DataServer֮���˫��ͨ��
	 */
	public void bindServer(DataServer server){
		server.registerHandler(receiver);
		sender = server.getHandler();
	}
	
	public void unbindServer(DataServer server){
		server.unregisterHandler(receiver);
		sender = null;
		
	}
	
	/*
	 * ������Ϣ�ļ��ַ�װ
	 */
	protected void send(int what, int arg1, int arg2, Object obj){
		if (null != sender)
			sender.obtainMessage(what, arg1, arg2, obj).sendToTarget();
	}
	
//	protected void send(int what, Object obj){
//		sender.obtainMessage(what, ORITATION_OUT, -1, obj).sendToTarget();
//	}
	
	protected void send(int what, int arg1, int arg2){
		if (null != sender)
			sender.obtainMessage(what, arg1, arg2).sendToTarget();
	}
	
	protected void send(int what){
		if (null != sender)
			sender.obtainMessage(what).sendToTarget();
	}
	
	/*
	 * ��Ϣ����
	 */
	protected abstract void doJob(Message msg);
	
	public abstract void sendExpired();
	
	/*
	 * �ͻ��˴�����
	 * 1���ṩ�ж��Ƿ�Ϊ�Լ��ܴ������Ϣ
	 * 2���ж����Լ��ܹ��������Ϣ�������doJob����
	 */
	class DataClientHandler extends Handler{
		public void handleMessage(Message msg) {
			if (isMine(msg.what, msg.arg1)){
				doJob(msg);
			}
		}

		public boolean isMine(int what, int arg1){
			return ((what == type)&&(arg1 == oritation));
		}
	}

}
