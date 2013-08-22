package org.lp20.aikuma.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.R;
import org.lp20.aikuma.util.FileIO;
import org.lp20.aikuma.model.Language;

public class LanguageFilterList extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_list);
		menuBehaviour = new MenuBehaviour(this);
		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);
		List<Language> languages;
		languages = Aikuma.getLanguages();
		adapter = new ArrayAdapter<Language>(this,
				android.R.layout.simple_list_item_1, languages);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBehaviour.onOptionsItemSelected(item,
				"This will discard the recording. Are you sure?",
				"Discard", "Cancel");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.putExtra("language", (Language)l.getItemAtPosition(position));
		setResult(RESULT_OK, intent);
		LanguageFilterList.this.finish();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		filterText.removeTextChangedListener(filterTextWatcher);
	}
	private ArrayAdapter<Language> adapter;
	private EditText filterText = null;
	private MenuBehaviour menuBehaviour;
}
