package services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import log.ConsoleLogger;

public class FtpServiceTest {

	private FtpService ftpService;
	
	@Before
	public void init() {
		this.ftpService = new FtpService();
	}
	
	/*@Test
	public void testConnect() {
		FtpService service = Mockito.spy(ftpService);
		ConsoleLogger cl = Mockito.mock(ConsoleLogger.class);
		String url = "127.0.0.1";
		String port = "2048";
		this.ftpService.connect(url, port);
		Mockito.verify(cl.log(type, message);)
	}*/

}
