package com.yulinghb.watermonitor;

/*
 * DataClient��Ϣ���ݶ���
 */
public interface WaterMonitorClientContract {
	/*     
	 * what                     arg1                        arg2          obj        description                                     
	 * TYPE_TEXT                ORITATION_OUT                -1            message    ����ֱ����ʾ�������ϵ��ַ���
	 * TYPE_MEASURE_DATA        ORITATION_OUT                -1         probe index   �����ȡDO,�¶ȣ���ѹֵ
	 *                          ORITATION_IN                  0         ConteValues   Ӧ��DO���¶Ⱥ���ѹֵ    
	 *                          ORITATION_IN                 -1            null       ��ʱ    
	 * TYPE_CALIBRATION_DATA    ORITATION_OUT                 0         probe index   ����У׼--���
	 *                          ORITATION_IN                  0            result     ����У׼���--���
	 *                          ORITATION_OUT                 1         probe index   ����У׼
	 *                          ORITATION_IN                  1            result     ����У׼���
	 *                          ORITATION_OUT                 2         probe index   �ָ���������
	 *                          ORITATION_IN                  2            result     �ָ� �������
	 *                          ORITATION_OUT                 3         probe index   ����У׼--����
	 *                          ORITATION_IN                  3            result     ����У׼���--����
	 *                          ORITATION_OUT                 4 {probe index, salinity}�����ζ�
	 *                          ORITATION_IN                  4            result     �����ζȽ��
	 *                          ORITATION_IN                 -1            null       ��ʱ
	 * TYPE_RAW_DATA            ORITATION_OUT/ORITATION_IN   data length   data       ����ʮ����������Ϊͨ�����ӷ��ͺͽ��յ��Զ�������֡
	 * TYPE_CONNECTION_INFO     ORITATION_IN                  0            message    ����״̬
	 * 							ORITATION_IN                 -1            message    ��ʱ
	 * TYPE_DEVICE_INFO         ORITATION_OUT                 0            null       ����������ӵ���̽ͷ��Ϣ
	 *                          ORITATION_OUT                 1            null       ��� ��¼ ���豸��Ϣ
	 *                          ORITATION_IN                  0    List<ContentValues>���º�����ӵ�̽ͷ��Ϣ������������id��sn
	 *                          ORITATION_IN                 -1    		   null		     ��ʱ
	 * TYPE_LOCATION_INFO       ORITATION_OUT                -1            null       ��ȡGPS��Ϣ
	 *                          ORITATION_IN                  0           Location    GPS��Ϣ    
	 *                          ORITATION_IN                 -1           Location    ��ʱ                
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
