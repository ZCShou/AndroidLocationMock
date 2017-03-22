package com.example.locationmock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "SetLocation";
	private LocationManager mLocationManager;
	private Context mContext;
	private String mMockProviderName = LocationManager.GPS_PROVIDER;
	private double mLongitude = 116.395636;
	private double mLatitude = 39.929983;
	private Button mSetBtn;
	private Button mUpBtn;
	private Button mDownBtn;
	private Button mStoreCurLocBtn;
	private Button mGetStoreLocBtn;
	private Button mEmptyStoreLocBtn;
	private Button mFloatWinSwitchBtn;
	private EditText mLongitudeEdit;
	private EditText mLatitudeEdit;
	private TextView mStepTxt;
	private RelativeLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private double stepLength = 0.000007;
	private double stepOnce = 0.0000005;
	private boolean isStart = false;
	private boolean isFloatWinEnable = true;
	private double curBearing = 180;
	private List<String> dataNameList = new ArrayList<String>();
	private SharedPreferences preferences;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);

		mLongitudeEdit = (EditText) findViewById(R.id.txt_id_Longitude);
		mLatitudeEdit = (EditText) findViewById(R.id.txt_id_Latitude);
		mSetBtn = (Button) findViewById(R.id.btn_id_set);
		mUpBtn = (Button) findViewById(R.id.btn_id_upstep);
		mDownBtn = (Button) findViewById(R.id.btn_id_downstep);
		mStepTxt = (TextView) findViewById(R.id.txt_id_stepLength);
		mStoreCurLocBtn = (Button) findViewById(R.id.btn_id_storeCurLocate);
		mGetStoreLocBtn = (Button) findViewById(R.id.btn_id_chooseStoredLocation);
		mEmptyStoreLocBtn = (Button) findViewById(R.id.btn_id_emptyStoredLocation);
		mFloatWinSwitchBtn = (Button) findViewById(R.id.btn_id_floatWinSwitch);
		
		preferences = this.getSharedPreferences("t1", Context.MODE_PRIVATE);
		editor = preferences.edit();
		readNameList();
		if (dataNameList.contains("lastLocation")) {
			mLongitude = preferences.getFloat("lastLocationLo", 0);
			mLatitude = preferences.getFloat("lastLocationLa", 0);
		}

		DisplayMetrics dm2 = getResources().getDisplayMetrics();

		GlobalValue.sWinH = dm2.heightPixels;
		GlobalValue.sWinW = dm2.widthPixels;

		try {
			String providerStr = LocationManager.GPS_PROVIDER;
			LocationProvider provider = mLocationManager
					.getProvider(providerStr);
			Log.e(TAG, "1");
			if (provider != null) {
				Log.e(TAG, "2");
				mLocationManager.addTestProvider(provider.getName(),
						provider.requiresNetwork(),
						provider.requiresSatellite(), provider.requiresCell(),
						provider.hasMonetaryCost(),
						provider.supportsAltitude(), provider.supportsSpeed(),
						provider.supportsBearing(),
						provider.getPowerRequirement(), provider.getAccuracy());
			} else {
				Log.e(TAG, "3");
				mLocationManager.addTestProvider(providerStr, true, true,
						false, false, true, true, true, Criteria.POWER_HIGH,
						Criteria.ACCURACY_FINE);
			}
			mLocationManager.setTestProviderEnabled(providerStr, true);
			mLocationManager.setTestProviderStatus(providerStr,
					LocationProvider.AVAILABLE, null,
					System.currentTimeMillis());

		} catch (SecurityException e) {
			Log.e(TAG, "error");
		}

		createFloatView();
		updateLocation();
		mSetBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mStartBtn onClick");
				try {
					mLongitude = Double.valueOf(mLongitudeEdit.getText()
							.toString());
					mLatitude = Double.valueOf(mLatitudeEdit.getText()
							.toString());
					Toast.makeText(getApplicationContext(),
							"����:" + mLongitude + ",ά��:" + mLatitude,
							Toast.LENGTH_SHORT).show();

				} catch (NumberFormatException e) {
				}
			}
		});
		mStepTxt.setText("��ǰ����:" + (int) (stepLength / stepOnce));
		mUpBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength += stepOnce;
				mStepTxt.setText("��ǰ����:" + (int) (stepLength / stepOnce));
			}
		});

		mDownBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength -= stepOnce;
				stepLength = stepLength < stepOnce ? stepOnce : stepLength;
				mStepTxt.setText("��ǰ����:" + (int) (stepLength / stepOnce));
			}
		});

		final Context ctxt = this;

		mStoreCurLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText mNameEdit = new EditText(ctxt);
				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("�����ղص�ǰ�ص�����")
						.setView(mNameEdit)
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										storeCurLocat(mNameEdit.getText()
												.toString());
										storeLastLocat();
										wirteNameList();
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});
		mFloatWinSwitchBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isFloatWinEnable)
				{
					mFloatLayout.setVisibility(View.GONE);
					isFloatWinEnable = false;
					mFloatWinSwitchBtn.setText("����������");
				}
				else
				{
					mFloatLayout.setVisibility(View.VISIBLE);
					isFloatWinEnable = true;
					mFloatWinSwitchBtn.setText("�رո�����");
				}
			}
			
		});
		mGetStoreLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final String[] strList = new String[dataNameList.size()];

				for (int i = 0; i < dataNameList.size(); i++) {
					strList[i] = dataNameList.get(i);
				}

				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("ѡ���ղصص�")
						.setIcon(R.drawable.ic_launcher)
						.setSingleChoiceItems(strList, 0,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Log.d(TAG, "ѡ����"+which);
										mLongitude = preferences.getFloat(strList[which]+"Lo", 0);
										mLatitude = preferences.getFloat(strList[which]+"La", 0);
										Log.d(TAG, "mLongitude:"+mLongitude+"mLatitude:"+mLatitude);
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});

		mEmptyStoreLocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Dialog alertDialog = new AlertDialog.Builder(ctxt)
						.setTitle("��ʾ")
						.setMessage("�Ƿ����")
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dataNameList.clear();
										storeLastLocat();
										wirteNameList();
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				alertDialog.show();
			}
		});
		
		new Thread(new RunnableMockLocation()).start();
	}

	private class RunnableMockLocation implements Runnable {

		@SuppressLint("NewApi")
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);

					// if (isStart == false)
					// break;
					updateLocation();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ֹͣģ��λ�ã���������ģ�����ݺ��޷���ԭʹ��ϵͳλ�� ��ģ��λ��δ��������removeTestProvider�����׳��쳣��
	 * ����addTestProvider�󣬹ر�ģ��λ�ã�δremoveTestProvider������ϵͳGPS�����ݸ��£�
	 */
	public void stopMockLocation() {
		try {
			mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			// ��δ�ɹ�addTestProvider������ϵͳģ��λ���ѹر����Ȼ�����
		}
	}

	private void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		// ��ȡWindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication().getSystemService(
				getApplication().WINDOW_SERVICE);
		// ����window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		// ����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGB_888;
		// ���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// ������������ʾ��ͣ��λ��Ϊ����
		wmParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = 0;
		wmParams.y = 0;

		// �����������ڳ�������
		// wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		int sqrLength = Math.min((int) GlobalValue.sWinH / 15,
				(int) GlobalValue.sWinH / 15);
		wmParams.width = sqrLength;
		wmParams.height = sqrLength;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// ��ȡ����������ͼ���ڲ���
		try {
			mFloatLayout = (RelativeLayout) inflater.inflate(
					R.layout.cam_for_server, null);
			// ���mFloatLayout
			mWindowManager.addView(mFloatLayout, wmParams);
		} catch (java.lang.RuntimeException e) {
			return;
		}

		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		// ���ü����������ڵĴ����ƶ�
		mFloatLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, "onTouch");
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_UP:
					isStart = false;
					//mFloatLayout.setBackgroundColor(0xFF000000);
					mFloatLayout.setVisibility(View.VISIBLE);
					break;
				case MotionEvent.ACTION_MOVE:
					double dy = event.getRawY() - GlobalValue.sWinH / 2;
					double dx = event.getRawX() - GlobalValue.sWinW / 2;
					double length = Math.sqrt(dx * dx + dy * dy);

					double dLocalY = -stepLength / length * dy;
					double dLocalX = stepLength / length * dx;

					curBearing = Math.toDegrees(Math.atan(-dLocalX / dLocalY));

					mLatitude += dLocalY;
					mLongitude += dLocalX;

					Log.i(TAG, "event.getRawY()=" + event.getRawY()
							+ ",event.getRawX()=" + event.getRawX()
							+ ",GlobalValue.sWinH / 2=" + GlobalValue.sWinH / 2
							+ ",GlobalValue.sWinW / 2=" + GlobalValue.sWinW / 2
							+ ",length=" + length + ",mLatitude=" + mLatitude
							+ ",mLongitude" + mLongitude + ",dLocalY="
							+ dLocalY + ",dLocalX=" + dLocalX + ",curBearing"
							+ curBearing);

					break;
				case MotionEvent.ACTION_DOWN:
					isStart = true;
					//mFloatLayout.setBackgroundColor(0xFFFFFFFF);
					mFloatLayout.setVisibility(View.INVISIBLE);
					break;
				default:
					break;
				}

				return false;
			}
		});

		mFloatLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onClick");
			}
		});
	}

	@SuppressLint("NewApi")
	private void updateLocation() {
		try {
			// ģ��λ�ã�addTestProvider�ɹ���ǰ���£�
			String providerStr = LocationManager.GPS_PROVIDER;
			Location mockLocation = new Location(providerStr);
			mockLocation.setLatitude(mLatitude); // ά�ȣ��ȣ�
			mockLocation.setLongitude(mLongitude); // ���ȣ��ȣ�
			mockLocation.setAltitude(30); // �̣߳��ף�
			mockLocation.setBearing((float) curBearing); // ���򣨶ȣ�
			mockLocation.setSpeed(5); // �ٶȣ���/�룩
			mockLocation.setAccuracy(0.1f); // ���ȣ��ף�
			mockLocation.setTime(new Date().getTime()); // ����ʱ��
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				mockLocation.setElapsedRealtimeNanos(SystemClock
						.elapsedRealtimeNanos());
			}
			mLocationManager.setTestProviderLocation(providerStr, mockLocation);
		} catch (Exception e) {
			// ��ֹ�û���������й����йر�ģ��λ�û�ѡ������Ӧ��
			stopMockLocation();
		}
	}

	private void wirteNameList() {
		editor.putInt("dataListNameLength", dataNameList.size());

		Log.d(TAG, "wirteNameList dataNameList.size():"+dataNameList.size());
		
		for (int i = 0; i < dataNameList.size(); i++) {
			editor.putString("dataListName" + i, dataNameList.get(i));
			Log.d(TAG, "wirteNameList editor.putString:"+"dataListName" + i+","+dataNameList.get(i));
		}
		editor.commit();
	}

	private void readNameList() {
		int length = 0;
		length = preferences.getInt("dataListNameLength", 0);
		Log.d(TAG, "readNameList length:"+length);
		
		for (int i = 0; i < length; i++) {
			dataNameList.add(preferences.getString("dataListName" + i, "none"));
			Log.d(TAG, "readNameList "+"dataListName" + i+","+dataNameList.get(i));
		}
		
	}

	protected void onDestroy() {
		super.onDestroy();
		storeLastLocat();
		wirteNameList();
	}

	private void storeCurLocat(String name) {
		if (dataNameList.contains(name)) {
			Toast.makeText(this, "�����ظ�", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "�����ظ�");
			return;
		}
		editor.putFloat(name + "Lo", (float) mLongitude);
		editor.putFloat(name + "La", (float) mLatitude);
		dataNameList.add(name);
		Log.d(TAG, "storeCurLocat,name:"+name+",mLongitude:"+mLongitude+",mLatitude"+mLatitude);
		editor.commit();
	}
	
	private void storeLastLocat() {
		if (!dataNameList.contains("lastLocation")) {
			dataNameList.add("lastLocation");
		}
		editor.putFloat("lastLocationLo", (float) mLongitude);
		editor.putFloat("lastLocationLa", (float) mLatitude);
		editor.commit();
	}
}
