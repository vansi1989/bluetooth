package com.fan.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothReceiver mBluetoothReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//��ȡ����������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		IntentFilter filter = new IntentFilter();
		//��ʼɨ��Ĺ㲥
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		//ɨ����ɵĹ㲥
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//���ֿ����豸�Ĺ㲥
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		mBluetoothReceiver = new BluetoothReceiver();
		//ע�����
		registerReceiver(mBluetoothReceiver, filter);
	}

	private class BluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Toast.makeText(context, "��ʼɨ�������豸", 0).show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Toast.makeText(context, "ɨ�������豸���", 0).show();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Toast.makeText(context, "�����������豸", 0).show();
			} 
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBluetoothReceiver);
		mBluetoothReceiver = null;
		super.onDestroy();
	}
	private void scan(View view) {
		mBluetoothAdapter.startDiscovery();
	}
}
