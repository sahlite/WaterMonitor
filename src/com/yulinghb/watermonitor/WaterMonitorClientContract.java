package com.yulinghb.watermonitor;

/*
 * DataClient消息内容定义
 */
public interface WaterMonitorClientContract {
	/*     
	 * what                     arg1                        arg2          obj        description                                     
	 * TYPE_TEXT                ORITATION_OUT                -1            message    用于直接显示到界面上的字符串
	 * TYPE_MEASURE_DATA        ORITATION_OUT                -1         probe index   请求获取DO,温度，气压值
	 *                          ORITATION_IN                  0         ConteValues   应答DO，温度和气压值    
	 *                          ORITATION_IN                 -1            null       超时    
	 * TYPE_CALIBRATION_DATA    ORITATION_OUT                 0         probe index   两点校准--零点
	 *                          ORITATION_IN                  0            result     两点校准结果--零点
	 *                          ORITATION_OUT                 1         probe index   空气校准
	 *                          ORITATION_IN                  1            result     空气校准结果
	 *                          ORITATION_OUT                 2         probe index   恢复出厂设置
	 *                          ORITATION_IN                  2            result     恢复 出厂结果
	 *                          ORITATION_OUT                 3         probe index   两点校准--空气
	 *                          ORITATION_IN                  3            result     两点校准结果--空气
	 *                          ORITATION_OUT                 4 {probe index, salinity}设置盐度
	 *                          ORITATION_IN                  4            result     设置盐度结果
	 *                          ORITATION_IN                 -1            null       超时
	 * TYPE_RAW_DATA            ORITATION_OUT/ORITATION_IN   data length   data       传递十六进制数，为通过连接发送和接收的自定义数据帧
	 * TYPE_CONNECTION_INFO     ORITATION_IN                  0            message    连接状态
	 * 							ORITATION_IN                 -1            message    超时
	 * TYPE_DEVICE_INFO         ORITATION_OUT                 0            null       请求更新连接到的探头信息
	 *                          ORITATION_OUT                 1            null       清除 记录 的设备信息
	 *                          ORITATION_IN                  0    List<ContentValues>更新后的连接到探头信息，包括索引，id和sn
	 *                          ORITATION_IN                 -1    		   null		     超时
	 * TYPE_LOCATION_INFO       ORITATION_OUT                -1            null       获取GPS信息
	 *                          ORITATION_IN                  0           Location    GPS信息    
	 *                          ORITATION_IN                 -1           Location    超时                
	 */
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_MEASURE_DATA = 1;
	public static final int TYPE_CALIBRATION_DATA = 2;
	public static final int TYPE_RAW_DATA = 3;
	public static final int TYPE_CONNECTION_INFO = 4;
	public static final int TYPE_DEVICE_INFO = 5;
	public static final int TYPE_LOCATION_INFO = 6;
	
	public static final int ORITATION_IN = 0;
	public static final int ORITATION_OUT = 1;
	
	public static final int LOCATION_LIST_LOADER = 0;
	public static final int LOCATION_EDITOR_LOADER = 1;
	public static final int RECORDER_LIST_LOADER = 2;
	public static final int RECORDER_EDITOR_LOADER = 3;
	public static final int IMAGE_LOADER = 4;
	public static final int RECORDER_LOCATION_LOADER = 5;
	
	public static final int INTENT_GET_LOCATION = 11;
	
	public static final String SALINITY_INDEX = "salinity index";
	public static final String SALINITY_VALUE = "salinity value";
}
