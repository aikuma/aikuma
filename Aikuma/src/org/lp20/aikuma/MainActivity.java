/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lp20.aikuma.Aikuma;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.ui.ListenActivity;
import org.lp20.aikuma.ui.MenuBehaviour;
import org.lp20.aikuma.ui.RecordActivity;
import org.lp20.aikuma.ui.RecordingArrayAdapter;
import org.lp20.aikuma.ui.RecordingMetadataActivity;
import org.lp20.aikuma.ui.SettingsActivity;
import org.lp20.aikuma.util.SyncUtil;

// For audio imports
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.R;
import org.lp20.aikuma.model.Recording;
import org.lp20.aikuma.model.WaveFile;

/**
 * The primary activity that lists existing recordings and allows you to select
 * them for listening and subsequent respeaking.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class MainActivity extends ListActivity {

	// Helps us store how far down the list we are when MainActivity gets
	// stopped.
	private Parcelable listViewState;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		menuBehaviour = new MenuBehaviour(this);
		SyncUtil.startSyncLoop();
		List<Recording> recordings = Recording.readAll();
		ArrayAdapter adapter = new RecordingArrayAdapter(this, recordings);
		setListAdapter(adapter);
		Aikuma.loadLanguages();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuBehaviour.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBehaviour.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
		listViewState = getListView().onSaveInstanceState();
	}

	@Override
	public void onResume() {
		super.onResume();
		List<Recording> recordings = Recording.readAll();
		ArrayAdapter adapter = new RecordingArrayAdapter(this, recordings);
		setListAdapter(adapter);
		if (listViewState != null) {
			getListView().onRestoreInstanceState(listViewState);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Recording recording = (Recording) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListenActivity.class);
		intent.putExtra("filenamePrefix", recording.getFilenamePrefix());
		startActivity(intent);
	}

	MenuBehaviour menuBehaviour;

	////////////////////////////////////////////
	////                                   /////
	//// Things pertaining to AudioImport. /////
	////                                   /////
	////////////////////////////////////////////

	/**
	 * Called when the import button is pressed; starts the import process.
	 *
	 * @param	_view	the audio import button.
	 */
	public void audioImport(View _view) {
		mPath = Environment.getExternalStorageDirectory();
		loadFileList(mPath, FILE_TYPE);
		showAudioFilebrowserDialog();
	}

	/**
	 * Loads the list of files in the specified directory into mFileList
	 *
	 * @param	dir	The directory to scan.
	 * @param	fileType	The type of file (other than directories) to look
	 * for.
	 */
	private void loadFileList(File dir, final String fileType) {
		if(dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.contains(fileType) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		}
		else {
			mFileList= new String[0];
		}
	}

	/**
	 * Presents the dialog for choosing audio files to the user.
	 */
	private void showAudioFilebrowserDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		FilebrowserDialogFragment fbdf = new FilebrowserDialogFragment();
		fbdf.show(ft, "dialog");
	}

	/**
	 * Used to display audio files that the user can choose to load from.
	 */
	public class FilebrowserDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle("Choose your file");
			if(mFileList == null) {
				Log.e("importfile", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = mFileList[which];
					Log.i("importfile", "mChosenFile: " + mChosenFile);
					mPath = new File(mPath, mChosenFile);
					if (mPath.isDirectory()) {
						loadFileList(mPath, ".wav");
						showAudioFilebrowserDialog();
					} else {
						//Then it must be a .wav file.

						UUID uuid = UUID.randomUUID();
						long sampleRate = -1;
						int durationMsec = -1;

						// Determine sample rate and duration from the actual
						// file.
						try {
							WaveFile waveFile = new WaveFile(mPath);
							sampleRate = (long) waveFile.getSampleRate();
							durationMsec = (int) (waveFile.getDuration() * 1000);
						} catch (IOException e) {
							Toast.makeText(getActivity(),
									"Failed to read the WAVE file.",
									Toast.LENGTH_LONG).show();
						}

						//Copy the file to the no-sync directory.
						try {
							FileUtils.copyFile(mPath,
									new File(Recording.getNoSyncRecordingsPath(),
									uuid.toString() + ".wav"));
						} catch (IOException e) {
							Toast.makeText(getActivity(),
									"Failed to import the recording.",
									Toast.LENGTH_LONG).show();
						}

						// Pass the info along to RecordingMetadataActivity.
						Intent intent = new Intent(getActivity(),
								RecordingMetadataActivity.class);
						intent.putExtra("uuidString", uuid.toString());
						intent.putExtra("sampleRate", sampleRate);
						intent.putExtra("durationMsec", durationMsec);
						startActivity(intent);
					}
				}
			});
			dialog = builder.show();
			return dialog;
		}
	}

	private String[] mFileList;
	private File mPath;
	private String mChosenFile;
	private static final String FILE_TYPE = ".wav";

}
