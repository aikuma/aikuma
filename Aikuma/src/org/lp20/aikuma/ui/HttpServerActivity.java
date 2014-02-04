package org.lp20.aikuma.ui;

import java.util.Map;
import org.lp20.aikuma.R;
import org.lp20.aikuma.http.Server;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

public class HttpServerActivity extends AikumaActivity {
	private String hostname;
	private int port;
	private String selected_device;
	private TextView log;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		// read default port number set in ui
		port = Integer.parseInt(((TextView) findViewById(R.id.text_port)).getText().toString());
		
		log = (TextView) findViewById(R.id.text_server_log);
		
		Server.setAssetManager(getAssets());
		
		// initially set the ip address field
		Map<String,String> addrs = Server.getIpAddresses();
		if (addrs.size() > 0) {
			selected_device = (String) addrs.keySet().toArray()[0];
			((TextView) findViewById(R.id.ip_address)).setText(addrs.get(selected_device));
		}
		
		TextView t = (TextView) findViewById(R.id.text_port);
		t.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				port = Integer.parseInt(s.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
	}
	
	public void showNetworkInterfaceDialog(View view) {
		Map<String,String> addrs = Server.getIpAddresses();
		CharSequence[] menuitems = new CharSequence[addrs.size()];
		final String[] devices = new String[addrs.size()];
		final CharSequence[] ipaddrs = new CharSequence[addrs.size()];
		int count = 0;
		int checked = 0;
		for (String dev : addrs.keySet()) {
			devices[count] = dev;
			ipaddrs[count] = addrs.get(dev);
			menuitems[count] = addrs.get(dev) + " (" + dev + ")";
			if (dev.equals(selected_device))
				checked = count;
			count++;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a network interface");
		builder.setSingleChoiceItems(menuitems, checked, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selected_device = devices[which];
				((TextView) findViewById(R.id.ip_address)).setText(ipaddrs[which]);
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	public void startServer(View view) {
		String msg;
		Boolean success;
		Resources res = this.getResources();
		try {
			Server.setHost(hostname);
			Server.setPort(port);
			Server.getServer().start();
			msg = String.format(res.getString(R.string.http_dialog_success, port));
			success = true;
		}
		catch (java.io.IOException e) {
			success = false;
			msg = res.getString(R.string.http_dialog_failure);
			log.append("error: ");
			log.append(e.getMessage());
			log.append("\n");
		}
		view.setEnabled(!success);
		findViewById(R.id.button_stop_http).setEnabled(success);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.http_dialog_title).setMessage(msg);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void stopServer(View view) {
		Server.getServer().stop();
		findViewById(R.id.button_start_http).setEnabled(true);
		view.setEnabled(false);
	}
}
