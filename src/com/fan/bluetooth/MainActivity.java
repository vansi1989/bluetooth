package com.fan.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.R.layout;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothReceiver mBluetoothReceiver;
	private LinearLayout ll_containner;
	private BluetoothSocket socket;
	private OutputStream os;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		//获取蓝牙管理器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		IntentFilter filter = new IntentFilter();
		//开始扫描的广播
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		//扫描完成的广播
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//发现可用设备的广播
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		mBluetoothReceiver = new BluetoothReceiver();
		//注册监听
		registerReceiver(mBluetoothReceiver, filter);
	}

	private class BluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Toast.makeText(context, "开始扫描蓝牙设备", Toast.LENGTH_SHORT).show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Toast.makeText(context, "扫描蓝牙设备完成", Toast.LENGTH_SHORT).show();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				System.out.println("发现了蓝牙设备");
				//get the bluetooth device object from the intent
				final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//				System.out.println((device.getName() + "\n" + device.getAddress()));
				TextView tv = new TextView(MainActivity.this);
				tv.setText((device.getName() + "--" + device.getAddress()));
				tv.setTextSize(25);
				tv.setTag(device);
				tv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						try {
							socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//							socket = device.createInsecureRfcommSocketToServiceRecord(UUID.randomUUID());
							socket.connect();
							os = socket.getOutputStream();
							System.out.println("asdf");
							Toast.makeText(MainActivity.this, "成功连接"+device.getName(), Toast.LENGTH_SHORT).show();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				ll_containner.addView(tv);
			} 
		  }
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBluetoothReceiver);
		mBluetoothReceiver = null;
		super.onDestroy();
	}
	public void scan(View view) {
		mBluetoothAdapter.startDiscovery();
		ll_containner.removeAllViews();
	}
	public void stop(View view) {
		mBluetoothAdapter.cancelDiscovery();
	}
	public void switchLight(View view) {
		try {
			os.write(0x33);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void connect(View view) {
		return;
	}
	
	public void init() {
		ll_containner = (LinearLayout) findViewById(R.id.ll_containner);
	}
}
