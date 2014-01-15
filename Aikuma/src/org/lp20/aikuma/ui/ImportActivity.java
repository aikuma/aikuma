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
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
//import android.view.View;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.lp20.aikuma.R;
import org.lp20.aikuma.model.Recording;
//import org.lp20.aikuma.util.FileIO;
//import org.lp20.aikuma.util.UsageUtils;

/** 
 * Allows the user to import an audio file that was not created with Aikuma.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public class ImportActivity extends AikumaActivity {

	private String[] mFileList;
	private File mPath = Environment.getExternalStorageDirectory();
	private String mChosenFile;
	private static final String FILE_TYPE = ".wav";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importaudio);
		loadFileList(mPath, FILE_TYPE);
		showAudioFilebrowserDialog();
	}

	/**
	 * Presents the dialog for choosing audio files to the user.
	 */
	private void showAudioFilebrowserDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		FilebrowserDialogFragment fbdf = new FilebrowserDialogFragment();
		fbdf.show(ft, "dialog");
	}

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
					Log.i("importfile", "mChosenFile: " + mChosenFile);
					mPath = new File(mPath, mChosenFile);
					if (mPath.isDirectory()) {
						loadFileList(mPath, ".wav");
						showAudioFilebrowserDialog();
					} else {
						//Then it must be a .wav file.
						UUID uuid = UUID.randomUUID();
						long sampleRate = -1;
						try {
							sampleRate = sampleRateOfWavFile(mPath);
						} catch (IOException e) {
							Toast.makeText(getActivity(),
									"Failed to read the sampleRate of the WAV file",
									Toast.LENGTH_LONG).show();
							getActivity().finish();
						}
						int duration = -1;

						try {
							FileUtils.moveFile(mPath,
									new File(Recording.getNoSyncRecordingsPath(),
									uuid.toString() + ".wav"));
						} catch (IOException e) {
							Toast.makeText(getActivity(),
									"Failed to import the recording.",
									Toast.LENGTH_LONG).show();
							getActivity().finish();
						}

						Intent intent = new Intent(getActivity(),
								RecordingMetadataActivity.class);
						intent.putExtra("uuidString", uuid.toString());
						intent.putExtra("sampleRate", sampleRate);
						intent.putExtra("durationMsec", duration);
						startActivity(intent);
						getActivity().finish();
					}
				}
			});
			dialog = builder.show();
			return dialog;
		}
	}

	private long sampleRateOfWavFile(File wavFile) throws IOException {
		byte[] bytes = FileUtils.readFileToByteArray(wavFile);
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 24; i < 28; i++) {
			byte b = bytes[i];
			sb.append(String.format("%02X ", b));
		}
		Log.i("bytes", sb.toString());
		return -1;
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}
}
