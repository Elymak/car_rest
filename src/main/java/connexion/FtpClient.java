package connexion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import log.ConsoleLogger;
import log.LogType;

public class FtpClient {

	private ServerSocket ftp_socket;
	private Socket commandes;
	private DataOutputStream out;
	private BufferedReader buf;
	
	private boolean isConnectedWithServer;
	private boolean passiveMode;
	
	private String addr;
	private int port;
	
	public FtpClient(String url, String port) throws IOException {
		this.isConnectedWithServer = false;
		this.passiveMode = false;

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
				ConsoleLogger.log(LogType.INFO, "Disconnecting...");
				out.write("QUIT".getBytes());
				out.write("\n".getBytes());
				ConsoleLogger.log(LogType.INFO, "Checking disconnection");
				String isLogout = buf.readLine();

				if ("221".equals(isLogout.substring(0, 3))) {
					isConnectedWithServer = false;
					out.close();
					buf.close();
					ConsoleLogger.log(LogType.INFO, "Disconnected");
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
	
	public boolean port(){
		if(isConnectedWithServer()){
			Inet4Address addr;
			try {
				addr = (Inet4Address) InetAddress.getLocalHost();
				String Ipv4 = addr.getHostAddress().replace(".", ",");
				int port = ftp_socket.getLocalPort();
				ConsoleLogger.log(LogType.INFO, "socket data : " + Ipv4 + "," + port );
				
				
				out.write(("PORT " + Ipv4 + "," + port/256 + "," + port%256 + "\n").getBytes());
				
				String response = buf.readLine();
				
				if( "200".equals(response.substring(0, 3))){
					passiveMode = false;
					return true;
				} else{
					return false;
				}
				
				
			} catch (UnknownHostException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot get localhost InetAddress");
				return false;
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot send port command's datas to the ftp server");
				return false;
			}
			
		}
		return false;
	}
	
	
	

	public boolean isPassiveMode() {
		return passiveMode;
	}

	public void setPassiveMode(boolean passiveMode) {
		this.passiveMode = passiveMode;
	}

	public boolean isConnectedWithServer() {
		return isConnectedWithServer;
	}

	public void setConnectedWithServer(boolean isConnectedWithServer) {
		this.isConnectedWithServer = isConnectedWithServer;
	}
	
}
