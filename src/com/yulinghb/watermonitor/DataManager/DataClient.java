package com.yulinghb.watermonitor.DataManager;

import android.os.Handler;
import android.os.Message;

/*
 * DataClient配合DataServer类，建立消息系统，每个Client发送消息到Server,
 * 然后由Server分发到每个Client，彼此不需要知道对方，简化各个协作对象的关系
 * 消息的what和arg1在DataClient中定义，提供通用的约束，arg2留给协作对象来规范
 * 每个Client对象之针对一个消息进行处理，通过持有多个消息对象来分别处理不同的消息
 */
public abstract class DataClient {
	/*
	 * 连接到的服务端处理者
	 * 自己的客户端处理者
	 */
	private Handler sender;
	private final DataClientHandler receiver;
	
	/*
	 * 能够处理的消息类型和方向
	 */
	private int type;
	private int oritation;
	
	protected DataClient(int what, int arg1){
		type = what;
		oritation = arg1;
		receiver = new DataClientHandler();
	}

	/*
	 * 建议与DataServer之间的双向通道
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
	 * 发送消息的几种封装
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
	 * 消息处理
	 */
	protected abstract void doJob(Message msg);
	
	public abstract void sendExpired();
	
	/*
	 * 客户端处理者
	 * 1、提供判断是否为自己能处理的消息
	 * 2、判断是自己能够处理的消息，则调用doJob处理
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
