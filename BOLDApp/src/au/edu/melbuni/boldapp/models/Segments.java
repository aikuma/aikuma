package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import au.edu.melbuni.boldapp.HorizontalListView;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.SegmentItemAdapter;
import au.edu.melbuni.boldapp.persisters.Persister;

public class Segments {
	
	// Stored.
	// 
	String prefix;
	ArrayList<Segment> segments;
	
	// Volatile.
	//
	int segmentCounter = 0; // Move up?
	private Segment selectedForPlaying;
	private Segment selectedForRecording;
	
	// TODO Remove.
	//
	SegmentItemAdapter adapter;

	public Segments(String prefix) {
		this(prefix, new ArrayList<Segment>());
	}
	
	public Segments(String prefix, ArrayList<Segment> segments) {
		this.prefix = prefix;
		this.segments = segments;
	}
	
	// Persistence.
	//

	public void saveEach(Persister persister) {
		for (Segment segment : segments) {
			persister.save(segment);
		}
	}

	@SuppressWarnings("unchecked")
	public static Segments fromHash(Persister persister, Map<String, Object> hash) {
		String prefix = hash.get("prefix") == null ? "" : (String) hash.get("prefix"); // TODO throw?

		ArrayList<String> segmentIds = (ArrayList<String>) hash.get("segments");
		ArrayList<Segment> segments = new ArrayList<Segment>();
		if (segmentIds != null) {
			for (String segmentId : segmentIds) {
				Segment segment = Segment.load(persister, segmentId);
				segments.add(segment);
			}
		}
		
		return new Segments(prefix);
	}

	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		hash.put("prefix", this.prefix);
		
		
		
		return hash;
	}
	
	
	public void installOn(Activity activity, int viewId) {
		adapter = new SegmentItemAdapter(activity, this);

		HorizontalListView view = (HorizontalListView) activity
				.findViewById(viewId);
		view.setAdapter(adapter);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Segment segment = segments.get(position);
				setSelectedForPlaying(segment);
				setSelectedForRecording(segment);
			}
		});
		view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				new AlertDialog.Builder(parent.getContext())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage("Delete?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Segments.this.remove(position);
									}
								}).setNegativeButton("No", null).show();

				return false;
			}
		});
	}

	public int size() {
		return segments.size();
	}

	public void add(Segment segment) {
		segments.add(segment);
		adapter.notifyDataSetChanged();
		selectedForRecording = segment;
	}

	protected void remove(int position) {
		remove(segments.get(position));
	}
	
	public void remove(Segment segment) {
		segments.remove(segment);
		adapter.notifyDataSetChanged();
		if (selectedForRecording != null) {
			selectedForRecording.deselect();
		}
		selectedForRecording = null;
	}

	public void startPlaying(Player player) {
		System.out.println("SaP");

		Segment segment = getSelectedForPlaying();

		if (segment == null) {
			return;
		}

		segment.startPlaying(player);
	}

	public void stopPlaying(Player player) {
		System.out.println("SoP");

		Segment segment = getSelectedForPlaying();

		if (segment == null) {
			return;
		}

		segment.stopPlaying(player);
	}

	public void startRecording(Recorder recorder) {
		System.out.println("SaR");

		getSelectedForRecording().startRecording(recorder);
	}

	public void stopRecording(Recorder recorder) {
		System.out.println("SoR");

		getSelectedForRecording().stopRecording(recorder);

		// Last recorded will be the next playing.
		//
		this.selectedForPlaying = this.selectedForRecording;

		this.selectedForRecording.deselect();
		this.selectedForRecording = null;
	}

	public void select(int position) {
		Segment segment = get(position);
		setSelectedForPlaying(segment);
		setSelectedForRecording(segment);
	}

	public void setSelectedForPlaying(Segment segment) {
		if (selectedForPlaying == segment) {
			this.selectedForPlaying = null;
		}
		this.selectedForPlaying = segment;
	}

	public void setSelectedForRecording(Segment segment) {
		if (selectedForRecording != null) {
			this.selectedForRecording.deselect();
		}
		if (selectedForRecording == segment) {
			this.selectedForRecording = null;
		} else {
			this.selectedForRecording = segment;
			this.selectedForRecording.select();
		}
	}
	
	public Segment get(int position) {
		return segments.get(position);
	}

	// Returns the selected segment.
	//
	protected Segment getSelectedForPlaying() {
		if (selectedForPlaying == null) {
			if (segments.size() != 0) {
				return segments.get(segments.size() - 1);
			}
		}

		return selectedForPlaying;
	}

	// Returns the selected segment
	// and if there is none, creates a new one.
	//
	protected Segment getSelectedForRecording() {
		if (selectedForRecording == null) {
			add(new Segment(prefix + new Integer(segmentCounter++).toString()));
		}
		
		selectedForRecording.select();

		return selectedForRecording;
	}

}
