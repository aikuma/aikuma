package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;

public class LanguageFilterList extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_list);
		Map langCodeMap;
		try {
			langCodeMap = FileIO.readLangCodes(getResources());
			filterText = (EditText) findViewById(R.id.search_box);
			filterText.addTextChangedListener(filterTextWatcher);
			List<String> names = new ArrayList<String>(langCodeMap.keySet());
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, names);
			setListAdapter(adapter);
		} catch (IOException e) {
			LanguageFilterList.this.finish();
			Toast.makeText(getApplicationContext(), 
					"Error reading language codes.", Toast.LENGTH_LONG).show();
		}
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int 
				after) {
		}
		public void onTextChanged(CharSequence s, int start, int before, int
				count) {adapter.getFilter().filter(s);
		}
	};
	private ArrayAdapter<String> adapter;
	private EditText filterText = null;
}
