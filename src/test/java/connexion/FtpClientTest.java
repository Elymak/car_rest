package connexion;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Test;
import org.mockito.Mockito;

public class FtpClientTest {
	
	// trouver un moyen de mocker l'IOException
	
	// TROUVER UN MOYEN DE MOCKER UN NEW OBJECT (ServerSocket, Socket, DataOutputStream, BufferedReader, etc...)

	@Test
	public void testConnectToServer() throws IOException {
		String url = "127.0.0.1";
		String port = "12345";
		String user = "user";
		String password = "password";
		
		FtpClient ftpClient = new FtpClient(url, port);
		
		DataOutputStream out = Mockito.mock(DataOutputStream.class);
		BufferedReader buf = Mockito.mock(BufferedReader.class);
		
		boolean result = ftpClient.connectToServer(user, password);
	}

}
