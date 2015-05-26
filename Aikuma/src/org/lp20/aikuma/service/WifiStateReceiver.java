/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.service;

import java.util.ArrayList;
import java.util.List;

import org.lp20.aikuma.ui.WifiSyncActivity;
import org.lp20.aikuma2.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
/**
 * The BroadcastReceiver of wifi events
 *
 * @author	Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 */
public class WifiStateReceiver extends BroadcastReceiver {

	private static final String TAG = WifiStateReceiver.class.getCanonicalName();
	
	private WifiP2pManager mWifiManager;
	private Channel mChannel;
	private Activity mActivity;
	private PeerListListener mPeerListListener;
	private ConnectionInfoListener mConnectionListener;
	
	/**
	 * Constructor for WifiStateReceiver
	 * 
	 * @param wifiManager			Wifi system manager
	 * @param channel				Channel used for wifi p2p connection
	 * @param peerListListener		Listener for wifi-peer-related events
	 * @param connectionListener	Listener for connection-related events
	 * @param activity				Activity creating this WifiStateReceiver				
	 */
	public WifiStateReceiver(WifiP2pManager wifiManager, Channel channel, 
			PeerListListener peerListListener, ConnectionInfoListener connectionListener, 
			Activity activity) {
		super();
		this.mWifiManager = wifiManager;
		this.mChannel = channel;
		this.mPeerListListener = peerListListener;
		this.mConnectionListener = connectionListener;
		this.mActivity = activity;
		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            	((WifiSyncActivity) mActivity).setStateString("State: wifi enabled");
            } else {
                // Wi-Fi P2P is not enabled
            	((WifiSyncActivity) mActivity).setStateString("State: wifi disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        	
        	if(mWifiManager != null) {
        		Log.i("HI", "Request peers");
        		mWifiManager.requestPeers(mChannel, mPeerListListener);
        	}
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        	NetworkInfo info = (NetworkInfo) 
        			intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	Log.i(TAG, "connection changed: " + (mWifiManager == null) + ", " + info.isConnected());
            if(mWifiManager != null && info.isConnected()) {
            	//((WifiSyncActivity) mActivity).setStateString("State: connected");
            	mWifiManager.requestConnectionInfo(mChannel, mConnectionListener);
            } else if(!info.isConnected()) {
            	((WifiSyncActivity) mActivity).setStateString("State: disconnected");
            	((WifiSyncActivity) mActivity).initializeButtons();
            	
            	Intent serviceIntent = new Intent(mActivity, FTPServerService.class);
        		serviceIntent.putExtra(FTPServerService.ACTION_KEY, "stop");
        		mActivity.startService(serviceIntent);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
	}

}
