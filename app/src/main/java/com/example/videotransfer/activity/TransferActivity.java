package com.example.videotransfer.activity;



import com.example.videotransfer.R;
import com.example.videotransfer.broadcast.WiFiDirectBroadcastReceiver;
import com.example.videotransfer.fragment.DeviceDetailFragment;
import com.example.videotransfer.fragment.DeviceListFragment;
import com.example.videotransfer.fragment.DeviceListFragment.DeviceActionListener;
import com.example.videotransfer.others.SurfaceCallback;
import com.example.videotransfer.thread.ReceiveThread;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

/**
 * 继承ChannelListener和DeviceActionListener两个接口 主Activity，包含两个接收和发送的线程的内部类
 * 
 * @author lovekun
 *
 */
public class TransferActivity extends Activity implements ChannelListener,
		DeviceActionListener {

	private WifiP2pManager manager;
	private Channel channel;
	private boolean isWifiP2pEnabled = false;

	private final IntentFilter intentFilter = new IntentFilter();
	private WiFiDirectBroadcastReceiver receiver;
	private DeviceListFragment listFragment;
	private DeviceDetailFragment detailFragment;

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private SurfaceCallback surfaceCallback;

	private ImageView imageView;

	private Thread receiveThread;
	private static final int MSG_SUCCESS = 0;
	private static final int MSG_FAILED = 1;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) { // handler处理线程返回的结果，并更新UI

			switch (msg.what) {
			case MSG_SUCCESS:
				imageView.setImageBitmap((Bitmap) msg.obj);
				imageView.setScaleType(ScaleType.FIT_XY);
				break;

			case MSG_FAILED:
				imageView.setBackgroundResource(R.drawable.pic1);
				// Toast.makeText(getApplicationContext(), "MSG_FAILED!",
				// Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	/**
	 * @param isWifiP2pEnabled
	 *            the isWifiP2pEnabled to set
	 */
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	/**
	 * 发送视频的方法，设置部分控件的显示和隐藏，以及调用surfaceHolder的addCallback为当前surfaceView添加一个回调方法
	 * 
	 * @param ipaddr
	 */
	public void transfer(String ipaddr) {
		findViewById(R.id.imageView).setVisibility(View.GONE);
		findViewById(R.id.surfaceview).setVisibility(View.VISIBLE);
		surfaceCallback = new SurfaceCallback(surfaceHolder,
				ipaddr.substring(1));
		surfaceHolder.addCallback(surfaceCallback);
	}

	/**
	 * 接收视频方法，设置部分控件的隐藏和显示，并开始接收线程
	 */
	public void receive() {
		findViewById(R.id.imageView).setVisibility(View.VISIBLE);
		findViewById(R.id.surfaceview).setVisibility(View.GONE);
		receiveThread = new ReceiveThread(handler);
		receiveThread.start();
	}

	/**
	 * 寻找附近开启WiFi direct的设备
	 */
	public void searchPeers() {
		if (!isWifiP2pEnabled) {
			Toast.makeText(TransferActivity.this, "请开启P2P", Toast.LENGTH_LONG)
					.show();
			return;
		}
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				Toast.makeText(TransferActivity.this, "Discovery Initiated",
						Toast.LENGTH_LONG).show();

			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(TransferActivity.this,
						"Discovery Failed : " + reason, Toast.LENGTH_LONG)
						.show();
				return;
			}
		});
		listFragment.onInitiateDiscovery();
	}

	/**
	 * onCreate
	 * activity的生命周期，当activity创建的时候，需要执行的步骤
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer);
		init();
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.setKeepScreenOn(true);
		surfaceHolder.setFixedSize(640, 480);
		
		//对broadcast监听动作的过滤
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);

	}

	/**
	 * 初始化
	 * 寻找activity中的每个控件，并初始化
	 */
	public void init() {
		imageView = (ImageView) findViewById(R.id.imageView);
		listFragment = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.fra_list);
		detailFragment = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.fra_detail);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
	}

	@Override
	protected void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this); // 实例化broadcastReceiver
		registerReceiver(receiver, intentFilter); // 注册监听器
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver); // 注销监听器
	}

	@Override
	public void onChannelDisconnected() {

	}

	@Override
	public void showConnBtn(WifiP2pDevice device) {
		DeviceDetailFragment detailFragment = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.fra_detail);
		detailFragment.showConnBtn(device);

	}

	@Override
	public void cancelDisconnect() {

	}

	@Override
	public void connect(WifiP2pConfig config) {
		manager.connect(channel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				Toast.makeText(TransferActivity.this, "connect success",
						Toast.LENGTH_LONG).show();

			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(TransferActivity.this,
						"reason" + reason + "connect fail", Toast.LENGTH_LONG)
						.show();
			}
		});

	}

	@Override
	public void disconnect() {
		detailFragment.resetViews();
		resetData();
		manager.removeGroup(channel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d("P2P", "Disconnect failed. Reason :" + reasonCode);

			}

			@Override
			public void onSuccess() {
				detailFragment.getView().setVisibility(View.GONE);
				Log.i("P2P", "disconnect success");
			}

		});

	}

	/**
	 * activity状态一：
	 * 初始状态：
	 * 1、显示imageView，消失surfaceView
	 * 2、DeviceDetailFragment消失
	 * 3、清空peersList列表
	 */
	public void resetData() {
		DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.fra_detail);
		surfaceView.setVisibility(View.INVISIBLE);
		surfaceView.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);
		imageView.setImageBitmap(null);
		imageView.setBackgroundResource(R.drawable.pic1);
		if (listFragment != null) {
			listFragment.clearPeers();
		}
		if (fragmentDetails != null) {
			fragmentDetails.resetViews();
		}
	}
	
}
