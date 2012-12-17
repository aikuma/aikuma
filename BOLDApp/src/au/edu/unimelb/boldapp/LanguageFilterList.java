package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.google.common.base.Charsets;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class LanguageFilterList extends ListActivity {

	private EditText filterText = null;
	private ArrayAdapter<Language> adapter = null;
	private Map langCodeMap = null;
	private List<String> names = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_list);
		List list = new ArrayList<String>();
		langCodeMap = GlobalState.getLangCodeMap(getResources());
		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);
		//Log.i("sick", " " + new ArrayList<String>(langCodeMap.values()).size() + 
		//		new ArrayList<String>(new HashSet<String>(langCodeMap.values())).size());
		names = new ArrayList<String>(langCodeMap.keySet());
		List<String> codes = new ArrayList<String>(langCodeMap.values());
		List<Language> languages = new ArrayList<Language>();
		for (int i = 0; i < names.size(); i++) {
			languages.add(new Language(names.get(i), codes.get(i)));
		}
		adapter = new ArrayAdapter<Language>(this,
				android.R.layout.simple_list_item_1,
				languages);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//Intent intent = new Intent(this, SaveActivity.class);
		Intent intent = new Intent();
		intent.putExtra("language", (Language)l.getItemAtPosition(position));
		Log.i("positions", position + " " + names.get(position) );
		setResult(RESULT_OK, intent);
		this.finish();
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}
		
		public void beforeTextChanged(CharSequence s, int start, int count, int
				after) {
		}
		public void onTextChanged(CharSequence s, int start, int before, int
				count) {
			adapter.getFilter().filter(s);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		filterText.removeTextChangedListener(filterTextWatcher);
	}
}
