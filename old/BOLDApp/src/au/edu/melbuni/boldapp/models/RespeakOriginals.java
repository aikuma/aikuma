package au.edu.melbuni.boldapp.models;

import java.util.ArrayList;

public class RespeakOriginals {

	ArrayList<RespeakOriginal> originals;
	
	public RespeakOriginals() {
		this.originals = new ArrayList<RespeakOriginal>();
	}

	public int size() {
		return originals.size();
	}
	
	public RespeakOriginal get(int index) {
		return originals.get(index);
	}

	public void add(RespeakOriginal respeakOriginal) {
		originals.add(respeakOriginal);
	}
	
}
