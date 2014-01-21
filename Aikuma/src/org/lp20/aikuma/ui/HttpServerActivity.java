package org.lp20.aikuma.ui;

import java.util.Map;

import org.lp20.aikuma.R;
import org.lp20.aikuma.http.Server;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HttpServerActivity extends AikumaActivity {
	private String hostname;
	private int port;
	private TextView log;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		// read default port number set in ui
		port = Integer.parseInt(((TextView) findViewById(R.id.text_port)).getText().toString());
		
		log = (TextView) findViewById(R.id.text_server_log);
		
		Server.setAssetManager(getAssets());
		
		Spinner hosts = (Spinner) findViewById(R.id.spinner_ifs);
		hosts.removeAllViewsInLayout();
		final Map<String,String> addrs = Server.getIpAddresses();
		String[] ifs = addrs.keySet().toArray(new String[0]);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, ifs);
		hosts.setAdapter(aa);
		
		hosts.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View view, int pos, long id) {
				TextView t = (TextView) view;
				hostname = addrs.get(t.getText());
				TextView ip = (TextView) findViewById(R.id.ip_address);
				ip.setText(hostname);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});
		
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

	public void toggleServerStatus(View view) {
		if (((ToggleButton) view).isChecked()) {
			String msg;
			Resources res = this.getResources();
			try {
				Server.setHost(hostname);
				Server.setPort(port);
				Server.getServer().start();
				msg = String.format(res.getString(R.string.http_dialog_success, port));
			}
			catch (java.io.IOException e) {
				((ToggleButton) view).setChecked(false);
				msg = res.getString(R.string.http_dialog_failure);
				log.append("error: ");
				log.append(e.getMessage());
				log.append("\n");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.http_dialog_title).setMessage(msg);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else {
			Server.getServer().stop();
		}
	}
}
