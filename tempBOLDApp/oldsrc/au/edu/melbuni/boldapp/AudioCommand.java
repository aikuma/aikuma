package au.edu.melbuni.boldapp;

/*
 * Remembers a number of buffers and decides whether a given
 * pattern has been recognized.
 */
public class AudioCommand {
	
	short[][] pattern;
	short[] lastValues;
	int currentBuffer;
	
	/*
	 * Pass in a pattern. A pattern is an
	 * array of min, max short values.
	 */
	public AudioCommand(short[][] pattern) {
		this.pattern = pattern;
		
		/*
		 * We remember buffers as long as the pattern.
		 */
		this.lastValues = new short[pattern.length];
		this.currentBuffer = 0;
	}
	
	/*
	 * Given the latest buffer, is this
	 * audio command recognized?
	 */
	public boolean isRecognized(short value) {
		add(value);
		return compareWithPattern();
	}
	
	/*
	 * Adds and if necessary, shifts the buffers.
	 */
	protected void add(short value) {
		lastValues[currentBuffer % lastValues.length] = value;
		currentBuffer += 1;
	}
	
	/*
	 * Compares the current buffers with
	 * the given pattern.
	 */
	protected boolean compareWithPattern() {
		for (int i = 0; i < lastValues.length; i++) {
			short currentValue = lastValues[((i + currentBuffer) % lastValues.length)];
			if (currentValue < pattern[i][0] || currentValue > pattern[i][1]) {
				return false;
			}
		}
		return true;
	}
	
}
