package log;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

public class ConsoleLoggerTest {
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	@Before
	public void init() {
		System.setOut(new PrintStream(outContent));
	}
	
	@Test
	public void testLog() throws IOException {
		String message = "message de test";
		
		LogType logType = LogType.ERROR;
		ConsoleLogger.log(logType, message);
		assertEquals("[ERROR] message de test\n", outContent.toString());
		
		outContent.reset();
		logType = LogType.INFO;
		ConsoleLogger.log(logType, message);
		assertEquals("[INFO] message de test\n", outContent.toString());
		
		outContent.reset();
		logType = LogType.WARNING;
		ConsoleLogger.log(logType, message);
		assertEquals("[WARNING] message de test\n", outContent.toString());
	}

}
