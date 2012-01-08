package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import au.edu.melbuni.boldapp.Player;
import au.edu.melbuni.boldapp.Recorder;
import au.edu.melbuni.boldapp.adapters.SegmentItemAdapter;
import au.edu.melbuni.boldapp.listeners.OnCompletionListener;
import au.edu.melbuni.boldapp.persisters.JSONPersister;
import au.edu.melbuni.boldapp.persisters.Persister;
import au.edu.melbuni.boldapp.views.HorizontalListView;

public class Segments implements Iterable<Segment>, Collection<Segment> {
	
	// Stored.
	// 
	ArrayList<Segment> segments;
	
	// Volatile.
	//
	int segmentCounter = 0; // Move up?
	private Segment selectedForPlaying;
	private Segment selectedForRecording;
	
	// TODO Remove.
	//
	SegmentItemAdapter adapter;

	public Segments() {
		this(new ArrayList<Segment>());
	}
	
	public Segments(ArrayList<Segment> segments) {
		this.segments = segments;
	}
	
	public List<String> getIds() {
		List<String> segmentIds = new ArrayList<String>();
		for (Segment segment : segments) {
			segmentIds.add(segment.getIdentifier());
		}
		return segmentIds;
	}
	
	// Persistence.
	//
	
	public static Segments load(Persister persister, UUID uuid) {
		return persister.loadSegments(uuid.toString());
	}

	public void saveEach(Persister persister, String timelineIdentifier) {
		for (Segment segment : segments) {
			persister.save(timelineIdentifier, segment);
		}
	}

	public static Segments fromHash(Persister persister, String timelineIdentifier, List<String> segmentIds) {
		ArrayList<Segment> segments = new ArrayList<Segment>();
		if (segmentIds != null) {
			for (String segmentId : segmentIds) {
				Segment segment = Segment.load(persister, timelineIdentifier, segmentId);
				segments.add(segment);
			}
		}
		
		return new Segments(segments);
	}

	public Map<String, Object> toHash() {
		Map<String, Object> hash = new LinkedHashMap<String, Object>();
		
		ArrayList<String> segmentIds = new ArrayList<String>();
		for (Segment segment : segments) {
			segmentIds.add(segment.getIdentifier());
		}
		hash.put("segments", segmentIds);
		
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

	@Override
	public int size() {
		return segments.size();
	}

	@Override
	public boolean add(Segment segment) {
		boolean result = segments.add(segment);
		// TODO Use result?
		if (adapter != null) { adapter.notifyDataSetChanged(); }
		selectedForRecording = segment;
		return result;
	}

	protected void remove(int position) {
		remove(segments.get(position));
	}
	
	public void remove(Segment segment) {
		segments.remove(segment);
		if (adapter != null) { adapter.notifyDataSetChanged(); }
		if (selectedForPlaying == segment) {
			deselectPlaying();
		}
		if (selectedForRecording != null) {
			deselectRecording();
		}
	}
	
	public boolean removeLast() {
		int size = segments.size();
		if (size > 0) {
			remove(size - 1);
			return true;
		}
		return false;
	}

	public void startPlaying(Player player, String timelineIdentifier, OnCompletionListener listener, boolean lastByDefault) {
		System.out.println("SaP");

		Segment segment = getSelectedForPlaying(lastByDefault);

		if (segment == null) {
			return;
		}
		
		Persister persister = new JSONPersister();
		segment.startPlaying(player, persister.dirForSegments(timelineIdentifier), listener);
	}
	
	public void startPlaying(Player player, String timelineIdentifier, OnCompletionListener listener) {
		startPlaying(player, timelineIdentifier, listener, false);
	}
	
	public void startPlaying(Player player, String timelineIdentifier, boolean lastByDefault) {
		startPlaying(player, timelineIdentifier, null, lastByDefault);
	}
	
	public void startPlaying(Player player, String timelineIdentifier) {
		startPlaying(player, timelineIdentifier, null, false);
	}

	public void stopPlaying(Player player, boolean lastByDefault) {
		System.out.println("SoP");

		Segment segment = getSelectedForPlaying(lastByDefault);

		if (segment == null) {
			return;
		}

		segment.stopPlaying(player);
	}
	
	public void stopPlaying(Player player) {
		stopPlaying(player, false);
	}

	public void startRecording(Recorder recorder, String timelineIdentifier) {
		System.out.println("SaR");
		
		Persister persister = new JSONPersister();
		getSelectedForRecording().startRecording(recorder, persister.dirForSegments(timelineIdentifier));
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
	
	public void deselectPlaying() {
		if (selectedForPlaying != null) {
			selectedForPlaying.deselect();
			selectedForPlaying = null;
		}
	}
	public void deselectRecording() {
		if (selectedForRecording != null) {
			selectedForRecording.deselect();
			selectedForRecording = null;
		}
	}
	
	public boolean selectLastForPlaying() {
		int size = segments.size();
		if (size > 0) {
			select(size - 1);
			return true;
		} else {
			deselectPlaying();
		}
		return false;
	}

	public void setSelectedForPlaying(Segment segment) {
		if (selectedForPlaying!= null) {
			this.selectedForPlaying.deselect();
		}
		if (selectedForPlaying == segment) {
			this.selectedForPlaying = null;
		} else {
			this.selectedForPlaying = segment;
			this.selectedForPlaying.select();
		}
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
	// Note: Will not return the last anymore by default.
	//
	protected Segment getSelectedForPlaying(boolean lastByDefault) {
		if (lastByDefault && selectedForPlaying == null) {
			if (segments.size() > 0) {
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
			add(new Segment(new Integer(segmentCounter++).toString()));
		}
		
		selectedForRecording.select();

		return selectedForRecording;
	}
	
	public Segment find(String identifier) {
		for (Segment segment : segments) {
			if (segment.getIdentifier().equals(identifier)) {
				return segment;
			}
		}
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return segments.isEmpty();
	}
	
	public boolean contains(Segment segment) {
		return segments.contains(segment);
	}

	@Override
	public Iterator<Segment> iterator() {
		return segments.iterator();
	}

	public boolean selectNext() {
		Segment selected = getSelectedForPlaying(false);
		int selectedIndex = segments.indexOf(selected);
		if (selectedIndex != -1 && selectedIndex < segments.size()-1) {
			setSelectedForPlaying(segments.get(selectedIndex + 1));
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Segment> collection) {
		return segments.addAll(collection);
	}

	@Override
	public void clear() {
		segments.clear();
	}

	@Override
	public boolean contains(Object object) {
		return segments.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return containsAll(collection);
	}

	@Override
	public boolean remove(Object object) {
		return segments.remove(object);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return removeAll(collection);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return segments.retainAll(collection);
	}

	@Override
	public Object[] toArray() {
		return segments.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return segments.toArray(array);
	}

}
