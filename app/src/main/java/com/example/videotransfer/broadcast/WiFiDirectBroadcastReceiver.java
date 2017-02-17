package com.example.videotransfer.broadcast;

import com.example.videotransfer.R;
import com.example.videotransfer.activity.TransferActivity;
import com.example.videotransfer.fragment.DeviceDetailFragment;
import com.example.videotransfer.fragment.DeviceListFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * BroadcastReceiver监听器
 * 
 * @author lovekun
 *
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager manager;
	private Channel channel;
	private TransferActivity transferActivity;

	private DeviceListFragment listFragment;
	
	/**
	 * 构造方法
	 * @param manager
	 * @param channel
	 * @param transferActivity
	 */
	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			TransferActivity transferActivity) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.transferActivity = transferActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {		//重写的方法

		listFragment = (DeviceListFragment) transferActivity
				.getFragmentManager().findFragmentById(R.id.fra_list);

		String action = intent.getAction();
		//下面是判断各种状态的变化，并执行相应的操作
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// 如果本机P2P打开关闭状态发送变化，执行下面操作
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {	//判断本机的P2P服务是否打开
				transferActivity.setIsWifiP2pEnabled(true);
			} else {
				transferActivity.setIsWifiP2pEnabled(false);
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			// 本机WiFi状态监听
			listFragment.updateThisDevice((WifiP2pDevice) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// 设备变化监听
			if (manager != null) {
				manager.requestPeers(
						channel,
						(PeerListListener) listFragment);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			// 连接状态监听
			NetworkInfo networkInfo = intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (networkInfo.isConnected()) {
				DeviceDetailFragment detail_fragment = (DeviceDetailFragment) transferActivity
						.getFragmentManager().findFragmentById(R.id.fra_detail);
				detail_fragment.showDetails();
				manager.requestConnectionInfo(channel, detail_fragment);
			} else {
				transferActivity.resetData();
			}
		}

	}

}
