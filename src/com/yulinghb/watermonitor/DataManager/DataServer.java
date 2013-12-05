package com.yulinghb.watermonitor.DataManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yulinghb.watermonitor.DataManager.DataClient.DataClientHandler;

/*
 * DataClient���DataServer�࣬������Ϣϵͳ��ÿ��Client������Ϣ��Server,
 * Ȼ����Server�ַ���ÿ��Client���˴˲���Ҫ֪���Է����򻯸���Э������Ĺ�ϵ
 * ��Ϣ��what��arg1��Ҫʹ���߶��壬�ṩͳһ��Լ����arg2����ʹ�����Լ�����
 */
public class DataServer {
	public static final String TAG = "DataServer";
	
	/*
	 * ע�ᵽ�˷���˵����пͻ��˴���
	 */
	private List<DataClientHandler> mHandlerList = new ArrayList<DataClientHandler>();
	
	/*
	 * ���ͻ��������˷�����Ϣʱ��
	 * ����˽������Ϣ�㲥�������ܴ��������Ϣ�Ŀͻ��˴���
	 */
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if (!mHandlerList.isEmpty()){
				Iterator<DataClientHandler> i = mHandlerList.iterator();
				while(i.hasNext()){
					try{
						DataClientHandler item = i.next();
						if ((null != item)&&(item.isMine(msg.what, msg.arg1)))
							item.dispatchMessage(msg);
					}catch(Exception e){
						Log.e(TAG, "dispatch message error!", e);
					}
					
				}
			}
		}
	};
	
	/*
	 * ע��ͻ��˴�����
	 */
	public void registerHandler(DataClientHandler handler){
		mHandlerList.add(handler);
	}
	
	/*
	 * ע���ͻ��˴�����
	 */
	public void unregisterHandler(DataClientHandler handler){
		mHandlerList.remove(handler);
	}
	
	/*
	 * ��ȡ����˵Ĵ�����
	 */
	public Handler getHandler(){
		return mHandler;
	}
}
