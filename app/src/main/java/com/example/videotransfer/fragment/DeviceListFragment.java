package com.example.videotransfer.fragment;

import java.util.ArrayList;
import java.util.List;

import com.example.videotransfer.R;
import com.example.videotransfer.activity.TransferActivity;
import com.example.videotransfer.adapter.WiFiPeerListAdapter;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends Fragment implements PeerListListener {

	private View view;
	private ListView listView;
	private Button btn_search;
	private TextView tv_peers_tip;

	private WifiP2pDevice device;

	private ProgressDialog progressDialog;

	private List<WifiP2pDevice> peersList = new ArrayList<WifiP2pDevice>();

	private TransferActivity transferActivity;

	private WiFiPeerListAdapter adapter;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_list, container, false);
		
		init();
		
		btn_search.setOnClickListener(new OnClickListener() {		//search按钮的监听

			@Override
			public void onClick(View v) {
				transferActivity.resetData();
				transferActivity.searchPeers();
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {		//listView的监听

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				WifiP2pDevice item_device = peersList.get(position);
				transferActivity.showConnBtn(item_device);

			}
		});
		return view;
	}
	
	public void init(){
		listView = (ListView) view.findViewById(R.id.list_peers);
		btn_search = (Button) view.findViewById(R.id.btn_search);
		tv_peers_tip = (TextView) view.findViewById(R.id.tv_peers_tip);
		
		transferActivity = (TransferActivity) getActivity();
	}
	
	/**
	 * 更新本机信息的显示
	 * @param device
	 */
	public void updateThisDevice(WifiP2pDevice device) {
		this.device = device;
		TextView tv_myname = (TextView) view.findViewById(R.id.tv_myname);
		tv_myname.setText(device.deviceName);
		TextView tv_mystate = (TextView) view.findViewById(R.id.tv_mystate);
		tv_mystate.setText(getDeviceStatus(device.status));
	}

	/**
	 * 将对应的WiFi状态以字符串的形式返回
	 * @param deviceStatus
	 * @return
	 */
	public static String getDeviceStatus(int deviceStatus) {
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.INVITED:
			return "Invited";
		case WifiP2pDevice.CONNECTED:
			return "Connected";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";

		}
	}

	/**
	 * 只是在搜索设备的过程中，让界面显示一个progressbar
	 */
	public void onInitiateDiscovery() {
		progressDialog = ProgressDialog.show(getActivity(),
				"press back to cancel ", "finding peers", true, true,
				new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();

					}
				});
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {		//重写PeerListListener接口的方法
		if (peers.getDeviceList().size() == 0) {
			tv_peers_tip.setText("No devices found. Turn on P2P and perform discovery for peers");
			transferActivity.resetData();
		}else {
			tv_peers_tip.setText("Available Peers List:");
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		peersList.clear();
		peersList.addAll(peers.getDeviceList());
		adapter = new WiFiPeerListAdapter(peersList, getActivity());
		listView.setAdapter(adapter);
	}
	
	/**
	 * 清除list中的值
	 */
	public void clearPeers() {
		peersList.clear();
		listView.setAdapter(adapter);
	}

	/**
	 * An interface-callback for the activity to listen to fragment interaction
	 * events.
	 */
	public interface DeviceActionListener {

		void showConnBtn(WifiP2pDevice device);

		void cancelDisconnect();

		void connect(WifiP2pConfig config);

		void disconnect();
	}
	
}
