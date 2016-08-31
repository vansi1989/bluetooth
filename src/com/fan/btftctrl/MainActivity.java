package com.fan.btftctrl;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView readValues;
	private Button scanDevices;
	private Button clearButton;
	private Button setButton;
	private Button powerButton;
	private NumberPicker tempPicker;
	private NumberPicker timePicker;
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothReceiver mBluetoothReceiver;
	private BluetoothSocket btSocket;
	private OutputStream btOs;
	private InputStream btIs;
	
	private int isConnected; //0:not connect. 1:is connecting. 2:is connected
	public int curTemp;
	private int curTime;
	
	Handler btRecvHandler;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		setButton.setEnabled(false);
		powerButton.setEnabled(false);
		//获取蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mBluetoothReceiver = new BluetoothReceiver();
		registerReceiver(mBluetoothReceiver, filter);	
		btRecvHandler = new Handler() {
			public void handleMessage(Message msg) {
				readValues.append((String)msg.obj);
			};
		};
	}
	
	public void init() {
		readValues = (TextView) findViewById(R.id.readValues);
		scanDevices = (Button) findViewById(R.id.scanDevices);
		clearButton = (Button) findViewById(R.id.clearButton);
		setButton = (Button) findViewById(R.id.setButton);
		powerButton = (Button) findViewById(R.id.powerButton);
		tempPicker = (NumberPicker) findViewById(R.id.tempPicker);
		timePicker = (NumberPicker) findViewById(R.id.timePicker);

		isConnected = 0;
		
		scanDevices.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
					scanDevices.setText("扫描设备");
				} else {
					readValues.setText("");
					mBluetoothAdapter.startDiscovery();
					scanDevices.setText("停止扫描");
				}
			}
		});
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				readValues.setText("");
			}
		});
		setButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					System.out.println(curTemp);
					System.out.println(curTime);
					btOs.write((byte)curTemp);
					btOs.write((byte)curTime);
					btOs.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		tempPicker.setMaxValue(65);
		tempPicker.setMinValue(18);
		tempPicker.setValue(40);
		tempPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				curTemp = tempPicker.getValue();
			}
		});
		timePicker.setMaxValue(99);
		timePicker.setMinValue(1);
		timePicker.setValue(60);
		timePicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				curTime = timePicker.getValue();
			}
		});
	}
	
	private class BluetoothReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				System.out.println("开始扫描");
				Toast.makeText(MainActivity.this, "开始扫描", Toast.LENGTH_SHORT).show();
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				scanDevices.setText("扫描设备");
				System.out.println("扫描完成");
				Toast.makeText(MainActivity.this, "扫描完成", Toast.LENGTH_SHORT).show();
			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				System.out.println("发现蓝牙设备");
				Toast.makeText(MainActivity.this, "发现蓝牙设备", Toast.LENGTH_SHORT).show();
				//get the bluetooth device object from the intent
				final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				System.out.println((device.getName() + "---" + device.getAddress()));
				readValues.append((device.getName() + "---" + device.getAddress()));
				readValues.setTag(device);
				readValues.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try {
							if (0 == isConnected) {
								isConnected = 1;
								btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
								btSocket.connect();
								btOs = btSocket.getOutputStream();
								btIs = btSocket.getInputStream();
								isConnected = 2;
								MyThread btThread = new MyThread();
								btThread.start();
								System.out.println("已连接设备" + device.getName());
								Toast.makeText(MainActivity.this, "已连接设备" + device.getName(), Toast.LENGTH_SHORT).show();
								setButton.setEnabled(true);
								powerButton.setEnabled(true);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	private class MyThread extends Thread {
		@Override
		public void run() {
			while(true) {
				if (isConnected != 2)
					continue;
				
				byte readBuf[] = new byte[60];
				try {
					int length;
					length = btIs.read(readBuf);
					if (length > 0) {
						Message msg = Message.obtain();
						msg.obj = new String(readBuf);
						btRecvHandler.sendMessage(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
//				super.run();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBluetoothReceiver);
		mBluetoothReceiver = null;
		super.onDestroy();
	}

}
