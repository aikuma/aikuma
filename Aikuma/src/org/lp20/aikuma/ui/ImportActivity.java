/*
	Copyright (C) 2013, The Aikuma Project
	AUTHORS: Oliver Adams and Florian Hanke
*/
package org.lp20.aikuma.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
//import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
//import android.view.View;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
//import java.io.IOException;
import org.lp20.aikuma.R;
//import org.lp20.aikuma.util.FileIO;
//import org.lp20.aikuma.util.UsageUtils;

/** 
 * Allows the user to import an audio file that was not created with Aikuma.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ImportActivity extends AikumaActivity {

	private FilebrowserDialogFragment fbdf;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importaudio);
		loadFileList();
		showDialog();
	}

	private void showDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		fbdf = new FilebrowserDialogFragment();
		fbdf.show(ft, "dialog");
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory() + "//");
	private String mChosenFile;
	private static final String FTYPE = ".txt";
	private static final int DIALOG_LOAD_FILE = 1000;

	private void loadFileList() {
		try {
			mPath.mkdirs();
		}
		catch(SecurityException e) {
			Log.e("importfile", "unable to write on the sd card " + e.toString());
		}
		if(mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.contains(FTYPE) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		}
		else {
			mFileList= new String[0];
		}
	}

	/**
	 * Used to display audio files that the user can choose to load from.
	 */
	public class FilebrowserDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Log.i("importfile", "files: " + mFileList);
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
					//you can do stuff with the file here too
				}
			});
			dialog = builder.show();
			return dialog;
		}
	}
}
