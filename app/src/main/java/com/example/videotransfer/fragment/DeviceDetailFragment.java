package com.example.videotransfer.fragment;


import com.example.videotransfer.R;
import com.example.videotransfer.activity.TransferActivity;
import com.example.videotransfer.fragment.DeviceListFragment.DeviceActionListener;
import com.example.videotransfer.thread.GoThread;
import com.example.videotransfer.thread.OwnerThread;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DeviceDetailFragment extends Fragment implements
		ConnectionInfoListener {

	private View view;
	private Button btn_connect;
	private Button btn_disconnect;
	private Button btn_getip;
	private Button btn_transfer;
	private Button btn_receive;
	
	private TextView tv_ownerInfo;
	private TextView tv_goInfo;

	private WifiP2pDevice device;
	private WifiP2pInfo info;

	private ProgressDialog progressDialog = null;
	
	private String clientIP;
	
	/**
	 * 获取ip线程GoThread接收后，进入handler更新主UI
	 */
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x01:
				clientIP = msg.getData().getString("ip");
				System.out.println("clientIP:"+ clientIP);
				tv_goInfo.setText("client ip is :" + msg.getData().getString("ip"));
				break;

			default:
				break;
			}
		}
		
	};

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_detail, container, false);
		
		init();
		btn_listener();
		
		return view;
	}
	
	public void init(){
		btn_connect = (Button) view.findViewById(R.id.btn_connect);
		btn_disconnect = (Button) view.findViewById(R.id.btn_diconnect);
		btn_getip = (Button) view.findViewById(R.id.btn_getip);
		btn_transfer = (Button) view.findViewById(R.id.btn_transfer);
		btn_receive = (Button) view.findViewById(R.id.btn_receive);
		tv_ownerInfo = (TextView) view.findViewById(R.id.tv_ownerinfo);
		tv_goInfo = (TextView) view.findViewById(R.id.tv_goinfo);
	}
	
	public void btn_listener(){
		btn_connect.setOnClickListener(listener);
		btn_disconnect.setOnClickListener(listener);
		btn_getip.setOnClickListener(listener);
		btn_transfer.setOnClickListener(listener);
		btn_receive.setOnClickListener(listener);
	}
	
	OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_connect:
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
				config.groupOwnerIntent = 15;
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				progressDialog = ProgressDialog.show(getActivity(),
						"press back to cancel", "connecting to"
								+ device.deviceAddress, true, true,
						new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								((DeviceActionListener) getActivity())
										.cancelDisconnect();

							}
						});
				((DeviceActionListener) getActivity()).connect(config);
				break;
			case R.id.btn_diconnect:
				((DeviceActionListener) getActivity()).disconnect();
				break;
			case R.id.btn_getip:
				new GoThread(info.groupOwnerAddress.getHostAddress(), 8080).start();
				break;
			case R.id.btn_transfer:
				btn_disconnect.setVisibility(View.GONE);
				btn_getip.setVisibility(View.GONE);
				if (info.isGroupOwner) {
					// 接收视频
					((TransferActivity) getActivity())
					.transfer(clientIP);
				} else {
					// 拍摄并发送视频
					((TransferActivity) getActivity())
							.transfer(info.groupOwnerAddress.toString());
				}
				break;
			case R.id.btn_receive:
				btn_disconnect.setVisibility(View.VISIBLE);
				btn_getip.setVisibility(View.GONE);
				// 接收视频
				((TransferActivity) getActivity()).receive();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {		//重写了ConnectionInfoListener接口中的方法
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		this.info = info;
		//判断是owner还是client
		if (info.groupFormed && info.isGroupOwner) {
			tv_ownerInfo.setText("this device will act as owner,ip is "
					+ info.groupOwnerAddress.getHostAddress());
			new OwnerThread(handler,8080).start();
		} else if (info.groupFormed) {
			btn_getip.setVisibility(View.VISIBLE);
			btn_transfer.setText("send video");
			tv_goInfo.setText("this device will act as client.");
		}
	}

	/**
	 * 第一个状态：
	 * 点击listView的item是只是显示connect按钮
	 * @param device
	 */
	public void showConnBtn(WifiP2pDevice device) {
		this.device = device;
		getView().setVisibility(View.VISIBLE);
	}
	
	/**
	 * 第二个状态：
	 * 点击connect按钮成功连接后，
	 * connect button按钮消失，显示断开连接和接收发送视频按钮
	 */
	public void showDetails() {
		btn_connect.setVisibility(View.GONE);
		btn_disconnect.setVisibility(View.VISIBLE);
		btn_transfer.setVisibility(View.VISIBLE);
		btn_receive.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 重置页面显示
	 */
	public void resetViews() {
		this.getView().setVisibility(View.GONE);
		tv_ownerInfo.setText("");
		tv_goInfo.setText("");
		btn_connect.setVisibility(View.VISIBLE);
		btn_disconnect.setVisibility(View.GONE);
		btn_getip.setVisibility(View.GONE);
		btn_transfer.setVisibility(View.GONE);
		btn_receive.setVisibility(View.GONE);
	}

	

}
