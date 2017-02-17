package com.example.videotransfer.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.videotransfer.R;
import com.example.videotransfer.activity.TransferActivity;
import com.example.videotransfer.fragment.DeviceListFragment;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * listView 的适配器，显示每一个搜索到的WiFi设备
 * 
 * @author lovekun
 *
 */
public class WiFiPeerListAdapter extends BaseAdapter {
	
	private List<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
	private Context context;
	
	/**
	 * 构造方法
	 * @param list
	 * @param context
	 */
	public WiFiPeerListAdapter(List<WifiP2pDevice> list, Context context) {
		super();
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
		TextView tv_peer = (TextView) convertView.findViewById(R.id.tv_peer_name);
		tv_peer.setText(list.get(position).deviceName);
		TextView tv_peer_status = (TextView) convertView.findViewById(R.id.tv_peers_status);
		tv_peer_status.setText(DeviceListFragment.getDeviceStatus(list.get(position).status));
		return convertView;
	}

}
