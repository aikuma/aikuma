package au.edu.unimelb.boldapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class FilterList extends ListActivity {

	private EditText filterText = null;
	private ArrayAdapter<String> adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_list);
		List list = new ArrayList<String>();
		list.add("once");
		list.add("upon");
		list.add("a");
		list.add("time");
		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				list);
		setListAdapter(adapter);
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
