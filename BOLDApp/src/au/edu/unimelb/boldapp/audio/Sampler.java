package au.edu.unimelb.aikuma.audio;

/**
 * The interface for samplers in the package.
 *
 * @author	Oliver Adams	<oliver.adams@gmail.com>
 * @author	Florian Hanke	<florian.hanke@gmail.com>
 */
public interface Sampler {

	/**
	 * Returns the current sample.
	 *
	 * @return	The current sample.
	 */
	long getCurrentSample();

}
