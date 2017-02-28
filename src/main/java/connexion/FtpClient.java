package connexion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import log.ConsoleLogger;
import log.LogType;

public class FtpClient {

	private ServerSocket ftp_socket;
	private Socket commandes;
	private DataOutputStream out;
	private BufferedReader buf;
	
	private boolean isConnectedWithServer;
	
	public FtpClient(String url, String port) throws IOException {
		this.isConnectedWithServer = false;

		this.ftp_socket = new ServerSocket(0);
		this.commandes = new Socket(InetAddress.getByName(url), Integer.parseInt(port));
		this.out = new DataOutputStream(commandes.getOutputStream());
		this.buf = new BufferedReader(new InputStreamReader(commandes.getInputStream()));
		ConsoleLogger.log(LogType.INFO, "Connection acceptée avec le serveur");
		String _220 = buf.readLine();
		ConsoleLogger.log(LogType.INFO, "Réponse du serveur : " + _220);

		if ("220".equals(_220.substring(0, 3))) {
			this.isConnectedWithServer = true;
		}

	}
	
	public boolean connectToServer(String user, String password) {

		if (isConnectedWithServer) {

			try {

				out.write(("USER " + user + "\n").getBytes());
				ConsoleLogger.log(LogType.INFO, "Envoi au serveur de la commande : USER " + user);

				String _331 = buf.readLine();
				ConsoleLogger.log(LogType.INFO, "Réponse du serveur : " + _331);

				if ("331".equals(_331.substring(0, 3))) {
					out.write(("PASS " + password + "\n").getBytes());
					ConsoleLogger.log(LogType.INFO, "Envoi au serveur de la commande : PASS " + password);

					String _230 = buf.readLine();
					ConsoleLogger.log(LogType.INFO, "Réponse du serveur : " + _230);

					if ("230".equals(_230.substring(0, 3))) {
						out.close();
						buf.close();
						return true;
					} else {
						out.close();
						buf.close();
						return false;
					}
				} else {
					out.close();
					buf.close();
					return false;
				}
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Something went wrong in streams initializations");
				return false;
			}
		} else {
			return false;
		}

	}
	
	public boolean disconnect(){
		
		if (isConnectedWithServer) {
			try {
				out.write("QUIT".getBytes());
				String isLogout = buf.readLine();

				out.close();
				buf.close();

				if ("221".equals(isLogout.substring(0, 3))) {
					isConnectedWithServer = false;
					return true;
				} else {
					return false;
				}

			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Something went wrong in streams initializations");
				return false;
			}
		} else {
			return false;
		}
		
	}

	public boolean isConnectedWithServer() {
		return isConnectedWithServer;
	}

	public void setConnectedWithServer(boolean isConnectedWithServer) {
		this.isConnectedWithServer = isConnectedWithServer;
	}
	
}
