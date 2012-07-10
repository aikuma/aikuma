package au.edu.melbuni.boldapp.behaviors;


public interface Behavior<T> {

	public void configureView(T activity);
	public void installBehavior(T activity);
	
}
