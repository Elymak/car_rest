package car_tp2;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import services.FtpService;

public class ConfigTest {
	
	private Config config;

	@Before
	public void init() {
		this.config = new Config();
	}
	
	@Test
	public void testAddResources() {
		List<Object> resources = new ArrayList<Object>();
		assertTrue(resources.isEmpty());
		this.config.addResources(resources);
		assertEquals(1, resources.size());
		assertTrue(resources.get(0) instanceof FtpService);
	}

}
