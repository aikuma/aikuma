/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.ServerCredentials;
import org.lp20.aikuma.service.FTPServerService;
import org.lp20.aikuma.service.GoogleCloudService;
import org.lp20.aikuma.service.WifiStateReceiver;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.util.SyncUtil;
import org.lp20.aikuma2.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * The activity dealing with wifi p2p sync using FTPServerService and WifiStateReceiver
 * 
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 * 
 */
public class WifiSyncActivity extends AikumaListActivity implements ChannelListener {
	
	private static final String TAG = WifiSyncActivity.class.getCanonicalName();
	
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiStateReceiver mReceiver;
	private IntentFilter mIntentFilter;
	private BroadcastReceiver mServerStatusReceiver;
	private IntentFilter mLocalIntentFilter;
	private Button mSearchButton;
	private Button mSyncButton;
	private Button mDisconnectButton;
	private TextView mStateView;
	
	private List<WifiP2pDevice> mPeers;
	private PeerListListener mPeerListListener; 
	private WifiPeerListAdapter mPeerListAdapter;
	
	private ConnectionInfoListener mConnectionListener;
	
	private String mServerIP;
	private boolean mRetryChannel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_sync_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mSyncButton = (Button) findViewById(R.id.wifiSyncButton);
		mDisconnectButton = (Button) findViewById(R.id.wifiDisconnectButton);
		initializeButtons();
		
		mPeers = new ArrayList<WifiP2pDevice>();
		mPeerListAdapter = new WifiPeerListAdapter(this, mPeers);
		setListAdapter(mPeerListAdapter);
		mPeerListListener = new PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peers) {
				mPeers.clear();
				mPeers.addAll(peers.getDeviceList());
				
				mPeerListAdapter.notifyDataSetChanged();
				//Toast.makeText(WifiSyncActivity.this, mPeers.size() + " found", Toast.LENGTH_LONG).show();
				
			}
			
		};
		mConnectionListener = new ConnectionInfoListener() {
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				/*
				String ownerName = info.groupOwnerAddress.getHostName();
				String ownerAddress = info.groupOwnerAddress.getHostAddress();
				String msg = ownerName + "(" + ownerAddress +") connected";
				
				Toast.makeText(WifiTestActivity.this, msg, Toast.LENGTH_LONG).show();
				*/
				mDisconnectButton.setEnabled(true);
				if(info.groupFormed) {
					if(info.isGroupOwner) {
						Log.i(TAG, "I'm an owner");
						mStateView.setText("State: server (connected)");
						Intent serviceIntent = new Intent(WifiSyncActivity.this, FTPServerService.class);
						serviceIntent.putExtra(FTPServerService.ACTION_KEY, "server");
						startService(serviceIntent);
						
						/*
						Intent wifiSyncIntent = new Intent(WifiTestActivity.this, WifiTestActivity2.class);
						wifiSyncIntent.putExtra("server", true);
						startActivity(wifiSyncIntent);
						*/
					} else {
						Log.i(TAG, "I'm a client");
						mStateView.setText("State: client (connected)");
						mServerIP = info.groupOwnerAddress.getHostAddress();
						mSyncButton.setEnabled(true);
						
						/*
						Intent wifiSyncIntent = new Intent(WifiTestActivity.this, WifiTestActivity2.class);
						wifiSyncIntent.putExtra("serverIP", serverIP);
						startActivity(wifiSyncIntent);
						*/
						
						//SyncUtil.startSyncLoop(WifiTestActivity.this, info.groupOwnerAddress);
						/*
						Intent serviceIntent = new Intent(WifiTestActivity.this, FTPServerService.class);
						serviceIntent.putExtra(FTPServerService.ACTION_KEY, "client");
						serviceIntent.putExtra(FTPServerService.SERVER_ADDR_KEY, serverIP);
						startService(serviceIntent);
						*/
					}
				}
				
			}
		};
		
		TextView deviceInfoView = (TextView) findViewById(R.id.deviceInfo);
		deviceInfoView.setText("ID: " + Aikuma.getAndroidID());
		mStateView = (TextView) findViewById(R.id.wifiState);
		/*
		mSearchButton = (Button) findViewById(R.id.wifiPeerSearchButton);
		mSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						Toast.makeText(WifiTestActivity.this, 
								"Searching...", Toast.LENGTH_SHORT).show();
					}
	
					@Override
					public void onFailure(int reason) {
						// TODO Auto-generated method stub
					}
				});
			}
		});*/
		/*
		mSyncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SyncUtil.startSyncLoop(WifiTestActivity.this, mServerIP);
			}
		});*/
		
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), this);
		mReceiver = new WifiStateReceiver(mManager, mChannel, 
				mPeerListListener, mConnectionListener, this);
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		mServerStatusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String status = intent.getStringExtra(FTPServerService.SERVER_STATUS);
				
				if(status.matches("start")) {
					mStateView.setText("Server started");
				} else {
					
				}
				
			}
			
		};
		
		mLocalIntentFilter = new IntentFilter(FTPServerService.SERVER_RESULT);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
		//LocalBroadcastManager.getInstance(this).registerReceiver(mServerStatusReceiver, mLocalIntentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
		//LocalBroadcastManager.getInstance(this).unregisterReceiver(mServerStatusReceiver);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Intent serviceIntent = new Intent(WifiSyncActivity.this, FTPServerService.class);
		serviceIntent.putExtra(FTPServerService.ACTION_KEY, "stop");
		startService(serviceIntent);
		disconnect();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		WifiP2pDevice device = mPeers.get(position);
		// start wifitestactivity2 and make it as an interface for 'connect', 'disconnect', 'sync'
		// Are we gonna use server-credentials??? (app crashes when there is no credential file)
		connect(device);
	}
	
	/**
	 * Show the status string in an activity
	 * (This can be called by WifiStateReceiver)
	 * @param str	the status string
	 */
	public void setStateString(String str) {
		mStateView.setText(str);
	}
	
	private void connect(WifiP2pDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		//Toast.makeText(WifiSyncActivity.this, "connect", Toast.LENGTH_LONG).show();
		
		mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {	
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Toast.makeText(WifiSyncActivity.this, "Connected", Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Toast.makeText(WifiSyncActivity.this, "Connection failed", Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void disconnect() {
		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(WifiSyncActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
				mStateView.setText("Disconnected");
				initializeButtons();
			}

			@Override
			public void onFailure(int reason) {
				//Toast.makeText(WifiSyncActivity.this, "Disconnection failed", Toast.LENGTH_LONG).show();
			}
			
		});
	}
	
	@Override
	public void onChannelDisconnected() {
        if (mManager != null && mRetryChannel) {
            Toast.makeText(this, "Channel is lost. Trying again", Toast.LENGTH_LONG).show();
            mManager.initialize(this, getMainLooper(), this);
            mRetryChannel = false;
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }
	
	/**
	 * Initialize the button enability
	 * (This can be called by WifiStateReceiver)
	 */
	public void initializeButtons() {
		mDisconnectButton.setEnabled(false);
		mSyncButton.setEnabled(false);
	}
	
	/**
	 * Called when the user searches the other peers in wifi range
	 *
	 * @param	v	the search button
	 */
	public void onSearchButton(View v) {
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Toast.makeText(WifiSyncActivity.this, 
						"Searching...", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * Called when the user decides to sync his files with the server peer
	 * (The corresponding is only enabled in a client device)
	 *
	 * @param	v	the sync button.
	 */
	public void onSyncButton(View v) {
		SyncUtil.setSyncSettingsActivity(WifiSyncActivity.this);
		SyncUtil.startSyncLoop(WifiSyncActivity.this, mServerIP);
	}
	
	/**
	 * Called when the user disconnects the wifi p2p connection
	 *
	 * @param	v	the disconnect button.
	 */
	public void onDisconnectButton(View v) {
		Intent serviceIntent = new Intent(WifiSyncActivity.this, FTPServerService.class);
		serviceIntent.putExtra(FTPServerService.ACTION_KEY, "stop");
		startService(serviceIntent);
		disconnect();
	}

	/**
     * Listview adapter to show a list of peer devices in wifi range
     * 
     * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
     *
     */
	private class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

		private static final int LIST_ITEM_LAYOUT = R.layout.wifi_peer_list_item;
		private LayoutInflater mInflater;
		
		private List<WifiP2pDevice> mPeers;
		
		public WifiPeerListAdapter(Context context, List<WifiP2pDevice> peers) {
			super(context, LIST_ITEM_LAYOUT, peers);
			this.mInflater = (LayoutInflater)
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mPeers = peers;
		}
		
		@Override
		public View getView(int position, View _, ViewGroup parent) {
			LinearLayout layout = (LinearLayout) 
					mInflater.inflate(LIST_ITEM_LAYOUT, parent, false);
			
			TextView textView1 = (TextView) layout.findViewById(R.id.text1);
			
			WifiP2pDevice device = mPeers.get(position);
			StringBuilder sb = new StringBuilder();
			
			//textView1.setText(device.toString());
			
			sb.append("Device: " + device.deviceName + "\n");
			sb.append("Address: " + device.deviceAddress + "\n");
			sb.append("State: " + getStatusString(device.status));
			textView1.setText(sb.toString());
			
			return layout;
		}
		
		public String getStatusString(int status) {
			switch(status) {
			case WifiP2pDevice.AVAILABLE:
				return "Available";
			case WifiP2pDevice.CONNECTED:
				return "Connected";
			case WifiP2pDevice.FAILED:
				return "Failed";
			case WifiP2pDevice.INVITED:
				return "Invited";
			case WifiP2pDevice.UNAVAILABLE:
				return "Unavailable";
			}
			return "";
		}
				
	}
	
}
