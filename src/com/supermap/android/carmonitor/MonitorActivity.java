package com.supermap.android.carmonitor;

import com.supermap.android.app.MyApplication;
import com.supermap.android.communication.MessageReciver;
import com.supermap.android.monitors.DisplayManager;
import com.supermap.android.monitors.SoundMonitor;
import com.supermap.data.GeometryType;
import com.supermap.data.Point2D;
import com.supermap.data.Workspace;
import com.supermap.CarsMonitorDemo.R;
import com.supermap.mapping.Map;
import com.supermap.mapping.MapView;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <p>
 * Title:�������
 * </p>
 * 
 * <p>
 * Description:
 * ============================================================================>
 * ------------------------------��Ȩ����----------------------------
 * ���ļ�Ϊ SuperMap iMobile ��ʾDemo�Ĵ��� 
 * ��Ȩ���У�������ͼ����ɷ����޹�˾
 * ----------------------------------------------------------------
 * ----------------------------SuperMap iMobile ��ʾDemo˵��---------------------------
 * 
 * 1��Demo��飺
 *   չʾ���ʵ�ֳ���״̬��غ�������
 * 2��Demo���ݣ�����Ŀ¼��"/SuperMap/Demos/Data/CarsMonitorData/"
 *           ��ͼ���ݣ�"carsmonitor.sxwu", "carsmonitor.bru", "carsmonitor.lsl", "carsmonitor.sym"
 *           ���Ŀ¼��"/SuperMap/License/"
 *           �켣�ļ�Ŀ¼��"/SuperMap/Demos/Data/Track/"
 * 3���ؼ�����/��Ա: 
 *	  LayerSettingVector.setStyle();             ����
 *    Layer.setAdditionalSetting();              ����
 *    MapControl.setAction();                    ����
 *	  MapControl.addGeometryAddedListener();     ����
 *    DynamicPoint.addAnimator();                ����
 *    DynamicPoint.setStyle();                   ����
 *	  DynamicPoint.setUserData();                ����
 *	  DynamicPoint.setOnClickListenner();        ����
 *    DynamicView.addElement();                  ����
 *    DynamicView.startAnimation();              ����
 *    Geometrist.canContain();                   ����
 *
 * 4������չʾ��
 *  (1)������أ���ʾ����س�����
 *  (2)���Ƽ��������س����Ľ�����
 * ------------------------------------------------------------------------------
 * ============================================================================>
 * </p> 
 * 
 * <p>
 * Company: ������ͼ����ɷ����޹�˾
 * </p>
 * 
 */
public class MonitorActivity extends Activity implements OnTouchListener{
	private MapView        mMapView           = null;
	private Map            m_Map              = null;
	private TextView       mWarningInfo       = null;
	private ImageButton    btn_start_monitor  = null;
	private ImageButton    btn_draw_fence     = null;
	private ImageButton    btn_clear_fence    = null;
	private ImageButton    btn_entire         = null;
	private ImageButton    btn_zoom_in        = null;
	private ImageButton    btn_zoom_out       = null;
	
	private MyApplication  mApp               = null;
	private MessageReciver mMessageReciver    = null;
	
	public static boolean  startMonitor       = false;
	private boolean        mExitEnable        = false;
	
	DisplayManager mDisplayManager            = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        // ��ʾ������ؽ���
		setContentView(com.supermap.CarsMonitorDemo.R.layout.monitorlayout);
		
		mApp = (MyApplication) getApplication();
		mApp.registerActivity(this);
		
		if(mMapView == null) {
			mMapView = (MapView) findViewById(R.id.mapview);           
			Workspace workspace = ((MyApplication)getApplication()).getWorkspace();
			
			mMapView.getMapControl().setOnTouchListener(this);
			mMapView.getMapControl().getMap().setWorkspace(workspace);
			mMapView.getMapControl().getMap().open(workspace.getMaps().get(0));
			mMapView.getMapControl().getMap().setCenter(new Point2D(12963755,4865688));
			mMapView.getMapControl().getMap().setScale(1/80000.0);
			mMapView.getMapControl().getMap().setFullScreenDrawModel(true);
			mMapView.getMapControl().getMap().refresh();
			
			m_Map = mMapView.getMapControl().getMap();
			
			
		}
		
		if(mWarningInfo == null){
			mWarningInfo = (TextView) findViewById(R.id.warninginfo);
			mWarningInfo.setVisibility(View.GONE);
		}
		
		mDisplayManager = new DisplayManager(mMapView);
		mDisplayManager.attachUI(mWarningInfo);
		// ��ռ����,�������˳�ʱ�����ܻ��м����δ���
		mDisplayManager.getDomainMonitor().clearMonitorDomain();
		
		btn_start_monitor = (ImageButton) findViewById(R.id.btn_start_monitor);
		btn_start_monitor.setOnClickListener(imageButtonListener);
		
		btn_draw_fence = (ImageButton) findViewById(R.id.btn_draw_fence);
		btn_draw_fence.setOnClickListener(imageButtonListener);
		
		btn_clear_fence = (ImageButton) findViewById(R.id.btn_clear_fence);
		btn_clear_fence.setOnClickListener(imageButtonListener);
		
		btn_entire = (ImageButton) findViewById(R.id.btn_entire);
		btn_entire.setOnClickListener(imageButtonListener);
		
		btn_zoom_in = (ImageButton) findViewById(R.id.btn_zoomIn);
		btn_zoom_in.setOnClickListener(imageButtonListener);
		
		btn_zoom_out = (ImageButton) findViewById(R.id.btn_zoomOut);
		btn_zoom_out.setOnClickListener(imageButtonListener);
		
		mMessageReciver = new MessageReciver(mDisplayManager);

		IntentFilter intentfilter = new IntentFilter(MyApplication.BroadcastAction);
		registerReceiver(mMessageReciver, intentfilter);
		
		//��ʼ����Ч������
		SoundMonitor.init(this);
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(!mExitEnable){
				Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT ).show();
				mExitEnable = true;
			}else{
				exit();
				
				//�����˳�����
				mApp.exit();
			}
			return true;
		}        
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		exit();
		super.onDestroy();
	}


	private void exit() {
		// ��ռ����
		mDisplayManager.getDomainMonitor().clearMonitorDomain();
		//�ͷŵ�ǰ��Դ
		mMapView.getMapControl().getMap().close();
		
		//ֹͣ��̨����
		Intent intent = new Intent();
		intent.setAction("com.supermap.backstageservice.START");	
		stopService(intent);
		unregisterReceiver(mMessageReciver);
	}
	
	/**
	 * ��ť��������
	 */
	OnClickListener imageButtonListener = new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) { 
			case R.id.btn_start_monitor:                    // �����������         
				startMonitor = true;

				break;
			case R.id.btn_draw_fence:                       // ���Ƽ����
				mDisplayManager.drawMonitorDomain();		
				
				break;
			case R.id.btn_clear_fence:                      // ��������
				
				mDisplayManager.getDomainMonitor().clearMonitorDomain();
				
				m_Map.getTrackingLayer().clear();
				m_Map.refresh();

				break;
			case R.id.btn_entire:
				m_Map.viewEntire();
				m_Map.refresh();

				break;
			case R.id.btn_zoomIn:
				m_Map.zoom(2);
				m_Map.refresh();

				break;
			case R.id.btn_zoomOut:
				m_Map.zoom(0.5);
				m_Map.refresh();

				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		
		mMapView.getMapControl().onMultiTouch(event);
		mExitEnable = false;                       // ����һ�η��ؼ�������Ļ��ȡ���˳�Ӧ��
		
		int action = event.getAction();
		
		// ��̧���֣��������µ�Region��ļ��ζ���ʱ���ύ���Ƶļ���ͼ��
		if(action == MotionEvent.ACTION_UP){
			if (mMapView.getMapControl().getCurrentGeometry() != null && mMapView.getMapControl().getCurrentGeometry().getType()==GeometryType.GEOREGION) {
				mMapView.getMapControl().submit();

				return true;
			}
		}
		return true;
	}
}
