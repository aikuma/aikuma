package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

public class FilterList extends ListActivity {

	private EditText filterText = null;
	private ArrayAdapter<String> adapter = null;
	private Map langCodeMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_list);
		List list = new ArrayList<String>();
		try {
			InputStream langCodeStream = getResources().openRawResource(
					R.raw.iso_639_3);
			langCodeMap = FileIO.loadLangCodes(langCodeStream);
			filterText = (EditText) findViewById(R.id.search_box);
			filterText.addTextChangedListener(filterTextWatcher);
			//Log.i("sick", " " + new ArrayList<String>(langCodeMap.values()).size() + 
			//		new ArrayList<String>(new HashSet<String>(langCodeMap.values())).size());
			List<String> l = new ArrayList<String>(langCodeMap.keySet());
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1,
					l);
			setListAdapter(adapter);
		} catch (IOException e) {
			//Toast something here
		}
		//list.add("once");
		//list.add("upon");
		//list.add("a");
		//list.add("time");
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
