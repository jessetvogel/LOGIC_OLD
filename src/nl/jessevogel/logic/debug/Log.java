package nl.jessevogel.logic.debug;

public class Log {

	public static void message(String message) {
		System.out.println("[LOG] " + message);
	}
	
	public static void error(String message) {
		System.err.println("[ERROR] " + message);
	}

	public static void debug(String message) {
		System.out.println("[DEBUG] " + message);
	}
	
}
