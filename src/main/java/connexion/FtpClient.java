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
	
	
	public boolean connectToServer(String url, int port, String user, String password) {
		
		try {
			this.ftp_socket = new ServerSocket(0);
			this.commandes = new Socket(InetAddress.getByName(url), port);
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Something went wrong in Sockets initializations");
			return false;
		}
		
		try {
			DataOutputStream out = new DataOutputStream(commandes.getOutputStream());
			BufferedReader buf = new BufferedReader(new InputStreamReader(commandes.getInputStream()));
			
			
			String _220 = buf.readLine();
			
			if("220".equals(_220.substring(0,3))){
				out.write(("USER " + user).getBytes());
				String _330 = buf.readLine();
				
				if("330".equals(_330.substring(0, 3))){
					out.write(("PASS " + password).getBytes());
					
					String _230 = buf.readLine(); 
					
					if("230".equals(_230.substring(0, 3))){
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
				
			} else {
				out.close();
				buf.close();
				return false;
			}
			
			
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Something went wrong in streams initializations");
			return false;
		}
		
	}
	
	public boolean disconnect(){
		
		try {
			DataOutputStream out = new DataOutputStream(commandes.getOutputStream());
			BufferedReader buf = new BufferedReader(new InputStreamReader(commandes.getInputStream()));
			
			out.write("QUIT".getBytes());
			String isLogout = buf.readLine();
			
			out.close();
			buf.close();
			
			return "221".equals(isLogout.substring(0, 3));
			
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Something went wrong in streams initializations");
			return false;
		}
		
		
		
	}
	
}
