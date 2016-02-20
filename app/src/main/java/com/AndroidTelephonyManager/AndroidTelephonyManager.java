package com.AndroidTelephonyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("CommitPrefEdits")
public class AndroidTelephonyManager extends Activity {

	int dBm;

	TextView sig;
    TextView textAccelerometer;
    TextView textCompass;

	MyPhoneStateListener MyListener;

	LocationManager locationManager;
	LocationListener ll;
	double Lat, Long;
	double latitude ,longitude;
	int mcc,mnc;
	Gpstracker gps;
	File myFile;
	FileOutputStream fOut;
	OutputStreamWriter myOutWriter;
	SignalStrength signalStrength;
	private TextView textLat;
	private TextView textLong;
	private TextView time;
	private TextView textMCC;
	private TextView textMNC;
	private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor compass;
    private SensorEventListener accelerometerListner;
    private SensorEventListener compassListner;
	
	PhoneStateListener pslistener;
	String signal;
	TelephonyManager Tel;

	SignalStrength s1;
	SimpleDateFormat sdf = new SimpleDateFormat(
			"dd:MMMM:yyyy KK:mm:ss a"); 
	String strDate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerListner = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
//                Log.d("pranjal","sensorchanged accelerometer events = ");


                final double alpha = 0.8;
                final double linear_acceleration[];
                final double gravity[];

                gravity = new double[3];
                linear_acceleration = new double[3];

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];

                textAccelerometer.setText("Accelerometer Data X: "+ String.valueOf(linear_acceleration[0])+" Y: "+String.valueOf(linear_acceleration[1])+" Z: "+String.valueOf(linear_acceleration[2]));

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        compassListner = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
//                Log.d("pranjal","sensorchanged compass events = "+event);

                final double alpha = 0.8;
                final double linear_acceleration[];
                final double gravity[];

                gravity = new double[3];
                linear_acceleration = new double[3];

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];

                textCompass.setText("Compass Data X: "+ String.valueOf(linear_acceleration[0])+" Y: "+String.valueOf(linear_acceleration[1])+" Z: "+String.valueOf(linear_acceleration[2]));


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

		setContentView(R.layout.main);

		Button bt = (Button) findViewById(R.id.gsmshow);

//		final TextView textGsmCellLocation = (TextView) findViewById(R.id.gsmcelllocation);
		textMCC = (TextView)findViewById(R.id.mcc);
		textMNC = (TextView)findViewById(R.id.mnc);
		textLat = (TextView) findViewById(R.id.lat);
		textLong = (TextView) findViewById(R.id.log);
		time = (TextView) findViewById(R.id.dateAndTime);
        textAccelerometer = (TextView) findViewById(R.id.accelerometer);
        textCompass = (TextView) findViewById(R.id.compass);
		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// retrieve a reference to an instance of TelephonyManager

		bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				updateLocation();

			}
		});

		String svcName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(svcName);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		String provider = locationManager.getBestProvider(criteria, true);

		Location l = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 2000, 5, locationListener);
		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		// 2000, 5, locationListener);
	}



	@Override
	protected void onPause() {
		super.onPause();
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        mSensorManager.unregisterListener(accelerometerListner);
        mSensorManager.unregisterListener(compassListner);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mSensorManager.registerListener(accelerometerListner, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(compassListner, compass, SensorManager.SENSOR_DELAY_NORMAL);

    }

	private void updateLocation() {
		// TODO Auto-generated method stub
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				int timeToBlink = 1000; // in ms
				try {
					Thread.sleep(timeToBlink);
				} catch (Exception e) {

				}
				handler.post(new Runnable() {
					@SuppressLint("NewApi")
					@Override
					public void run() {

						final File myFile = new File("/sdcard/cell.txt");

						if (!myFile.exists()) {
							try {
								myFile.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						Calendar c = Calendar.getInstance();
						
					    
						final TextView Neighboring = (TextView) findViewById(R.id.neighboring);
						final TextView textCID = (TextView) findViewById(R.id.cid);
						// final TextView Mrssi = (TextView)
						// findViewById(R.id.Mrssi);
						// int abc1 = (2 * s1.getGsmSignalStrength()) - 113;
						// Mrssi.setText("hi=" + abc1);
						final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
						final GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager
								.getCellLocation();
						
						locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
						boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
					
						String networkOperator = telephonyManager.getNetworkOperator();
						
						
						
						
						try {
							strDate = sdf.format(c.getTime());
							List<NeighboringCellInfo> NeighboringList = telephonyManager
									.getNeighboringCellInfo();

							int cid = cellLocation.getCid();
							// String signal=
							// String.valueOf(cellSignalStrengthGsm.getDbm());

							String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";

							for (int i = 0; i < NeighboringList.size(); i++) {

								String dBm;
								int rssi = NeighboringList.get(i).getRssi();
								rssi = (2 * rssi) -113 ;
								if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
									dBm = "Unknown RSSI";
								} else {
									dBm = String.valueOf(rssi) + " dBm";
								}

								stringNeighboring = stringNeighboring
										+ String.valueOf(NeighboringList.get(i)
												.getLac())
										+ "\t     :    "
										+ String.valueOf(NeighboringList.get(i)
												.getCid()) + "\t     :    "
										+ dBm + "\n";
							}
							
							if (networkOperator != null) {
						         mcc = Integer.parseInt(networkOperator.substring(0, 3));
						         mnc = Integer.parseInt(networkOperator.substring(3));
						    }

							gps = new Gpstracker(AndroidTelephonyManager.this);
						//	if(statusOfGPS){
							 latitude = gps.getLatitude();
							 longitude = gps.getLongitude();
						//	}
						//	else 
						//	{
						//		 latitude = 0;
						//		 longitude = 0;
						//	}
							
							
							textCID.setText("gsm cell id:"
									+ String.valueOf(cid));
							textMCC.setText("MCC:" + mcc);
							textMNC.setText("MNC:" + mnc);
							// Mrssi.setText("Main Rssi     "+
							// String.valueOf(signalStrength.getGsmSignalStrength()));
							time.setText("date&time :" + strDate);
							textLat.setText("Latitude   :"
									+ String.valueOf(latitude));
							textLong.setText("Longitude :"
									+ String.valueOf(longitude));
							Neighboring.setText(stringNeighboring);

							SharedPreferences prefs = getSharedPreferences(
									"DD", 0);

							String bool = prefs.getString("signal", "drdrd");

							// Toast.makeText(
							// Tom Xue: lifecycle related
							// getApplicationContext(), sig.getText(),
							// Toast.LENGTH_SHORT).show();
						} catch (Exception e2) {
							// TODO: handle exception
							strDate = sdf.format(c.getTime());
							
							time.setText("date&time :" + strDate);
							textCID.setText("gsm cell id: -1 ");
							Toast.makeText(getApplicationContext(), "Network Not Available", Toast.LENGTH_SHORT).show();
							
							//System.exit(0);
						}
						try {
							FileOutputStream fOut = new FileOutputStream(
									myFile, true);
							OutputStreamWriter myOutWriter = new OutputStreamWriter(
									fOut);

                            myOutWriter.append("\n");
							myOutWriter.append(textMCC.getText());
							myOutWriter.append("\n");
							myOutWriter.append(textMNC.getText());
							myOutWriter.append("\n");
							myOutWriter.append(textCID.getText());
							myOutWriter.append("\n");
							myOutWriter.append(textLat.getText());
							myOutWriter.append("\n");
							myOutWriter.append(textLong.getText());
							myOutWriter.append("\n");
							myOutWriter.append(time.getText());
							myOutWriter.append("\n");						
							myOutWriter.append(sig.getText());
							myOutWriter.append("\n"); 
							myOutWriter.append(Neighboring.getText());
                            myOutWriter.append(textCompass.getText());
                            myOutWriter.append("\n");
                            myOutWriter.append(textAccelerometer.getText());
                            myOutWriter.append("\n");
							myOutWriter.close();
							fOut.close();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						updateLocation();
					}
				});
			}
		}).start();

	}// / end content view

	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each time there is an
		 * update
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);

			sig = (TextView) findViewById(R.id.Mrssi);

			String abc1 = String.valueOf((2 * signalStrength
					.getGsmSignalStrength()) - 113);

			SharedPreferences prefs = getSharedPreferences("DD", 0);

			SharedPreferences.Editor Editor = prefs.edit();

			Editor.putString("signal", abc1);

			String str = String.valueOf(signalStrength.getGsmSignalStrength());

			sig.setText("main cell dbm : " + abc1);
			// Toast.makeText(
			// Tom Xue: lifecycle related
			// getApplicationContext(),
			// "Main Cell Dbm : "
			// + sig.getText(),
			// Toast.LENGTH_SHORT).show();

		}
	};/* End of private Class */

}
