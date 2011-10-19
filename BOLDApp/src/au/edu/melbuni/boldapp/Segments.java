package au.edu.melbuni.boldapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import au.edu.melbuni.boldapp.persisters.Persister;

public class Segments {

	int segmentCounter = 0;

	private Segment selectedForPlaying;
	private Segment selectedForRecording;

	ArrayList<Segment> segments;

	Timeline timeline;
	SegmentItemAdapter adapter;

	String identifier;

	public Segments(Timeline timeline) {
		segments = new ArrayList<Segment>();

		this.timeline = timeline;
		this.identifier = timeline.identifier;
	}
	
	// Persistence.
	//

	public void saveEach(Persister persister) {
		for (Segment segment : segments) {
			persister.save(segment);
		}
	}

	public static Segments fromJSON(String data) {
		// Map user = (Map) JSONValue.parse(data);
		// String name = user.get("name") == null ? "" : (String)
		// user.get("name");
		// UUID uuid = user.get("uuid") == null ? UUID.randomUUID() :
		// UUID.fromString((String) user.get("uuid"));
		
		// TODO Find the right timeline.
		//
		return new Segments(null);
	}

	@SuppressWarnings("rawtypes")
	public String toJSON() {
		Map<String, Comparable> segments = new LinkedHashMap<String, Comparable>();
		return JSONValue.toJSONString(segments);
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
			add(new Segment(timeline.identifier + new Integer(segmentCounter++).toString()));
		}
		
		selectedForRecording.select();

		return selectedForRecording;
	}

	protected Segment get(int position) {
		return segments.get(position);
	}

}
