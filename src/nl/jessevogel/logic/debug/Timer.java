package nl.jessevogel.logic.debug;

public class Timer {
	public long start;
	
	public void start() {
		start = System.currentTimeMillis();
	}
	
	public long stop() {
		return System.currentTimeMillis() - start;
	}
}
