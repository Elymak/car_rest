package log;

public class ConsoleLogger {

	public static void log(LogType type, String message) {
		System.out.println("[" + type.toString() + "] " + message);
	}
	
}
