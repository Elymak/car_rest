package connexion;

import java.io.IOException;

public class FtpClientFactory {

	public FtpClient createFtpClient(String url, String port) throws IOException {
		return new FtpClient(url, port);
	}
	
}
