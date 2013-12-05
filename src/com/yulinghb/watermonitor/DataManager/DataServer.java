package com.yulinghb.watermonitor.DataManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yulinghb.watermonitor.DataManager.DataClient.DataClientHandler;

/*
 * DataClient配合DataServer类，建立消息系统，每个Client发送消息到Server,
 * 然后由Server分发到每个Client，彼此不需要知道对方，简化各个协作对象的关系
 * 消息的what和arg1需要使用者定义，提供统一的约束，arg2留给使用者自己处理
 */
public class DataServer {
	public static final String TAG = "DataServer";
	
	/*
	 * 注册到此服务端的所有客户端处理
	 */
	private List<DataClientHandler> mHandlerList = new ArrayList<DataClientHandler>();
	
	/*
	 * 当客户端向服务端发送消息时，
	 * 服务端将这个消息广播到所有能处理这个消息的客户端处理
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
	 * 注册客户端处理者
	 */
	public void registerHandler(DataClientHandler handler){
		mHandlerList.add(handler);
	}
	
	/*
	 * 注销客户端处理者
	 */
	public void unregisterHandler(DataClientHandler handler){
		mHandlerList.remove(handler);
	}
	
	/*
	 * 获取服务端的处理者
	 */
	public Handler getHandler(){
		return mHandler;
	}
}
