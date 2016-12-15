package com.example.locationmock;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
	private EditText mLongitudeEdit;
	private EditText mLatitudeEdit;
	private TextView mStepTxt;
	private RelativeLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager mWindowManager;
	private double stepLength = 0.000007;
	private double stepOnce = 0.0000005;
	private boolean isStart = false;
	private double curBearing = 180;

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

		// mStartBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// Log.i(TAG, "mStartBtn onClick");
		// new Thread(new RunnableMockLocation()).start();
		// mStartBtn.setClickable(false);
		//
		// }
		// });
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
		
		mUpBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength += stepOnce;
				mStepTxt.setText("��ǰ����:"+(int)(stepLength/stepOnce));
			}
		});
		
		mDownBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stepLength -= stepOnce;
				stepLength = stepLength < stepOnce ? stepOnce :stepLength;
				mStepTxt.setText("��ǰ����:"+(int)(stepLength/stepOnce));
			}
		});
	}

	private class RunnableMockLocation implements Runnable {

		@SuppressLint("NewApi") @Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);

					if (isStart == false)
						break;
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
		int sqrLength = Math.min((int)GlobalValue.sWinH / 15, (int)GlobalValue.sWinH / 15);
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
					mFloatLayout.setBackgroundColor(Color.BLACK);
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
					new Thread(new RunnableMockLocation()).start();
					mFloatLayout.setBackgroundColor(Color.WHITE);
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
	
	@SuppressLint("NewApi") private void updateLocation()
	{
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
			mLocationManager.setTestProviderLocation(providerStr,
					mockLocation);
		} catch (Exception e) {
			// ��ֹ�û���������й����йر�ģ��λ�û�ѡ������Ӧ��
			stopMockLocation();
		}
	}

}
