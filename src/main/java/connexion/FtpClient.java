package connexion;

import java.io.BufferedReader;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import log.ConsoleLogger;
import log.LogType;

/**
 * Object permettant l'interaction entre un serveur FTP et un navigateur Web
 * 
 * @author Serial
 */
public class FtpClient {

	private ServerSocket flux_socket;
	private Socket data_socket;
	private Socket commandes_socket;
	private DataOutputStream out;
	private BufferedReader buf;
	
	private boolean isConnectedWithServer;
	private boolean passiveMode;
	
	private String addr;
	private int port;
	
	/**
	 * Ce constructeur permet d'instancier un nouveau FtpClient en se connectant directement à un serveur FTP
	 * 
	 * @param url : addresse du serveur (ex : 127.0.0.1 ou ftp.univ-lille1.fr)
	 * @param port : port du serveur
	 * @throws IOException
	 */
	public FtpClient(String url, String port) throws IOException {
		this.isConnectedWithServer = false;
		this.passiveMode = false;

		this.flux_socket = new ServerSocket(0);
		this.commandes_socket = new Socket(InetAddress.getByName(url), Integer.parseInt(port));
		this.out = new DataOutputStream(commandes_socket.getOutputStream());
		this.buf = new BufferedReader(new InputStreamReader(commandes_socket.getInputStream()));
		ConsoleLogger.log(LogType.INFO, "Connection acceptÃ©e avec le serveur");
		String _220 = buf.readLine();
		ConsoleLogger.log(LogType.INFO, "RÃ©ponse du serveur : " + _220);

		if ("220".equals(_220.substring(0, 3))) {
			this.isConnectedWithServer = true;
		}

	}
	
	/**
	 * Permet l'authentification d'un  utilisateur au serveur FTP 
	 * 
	 * @param user : nom d'utilisateur
	 * @param password : mot de passe de l'utilisateur
	 * @return true si login OK, false si erreurs ou mot de passe incorrect
	 */
	public boolean connectToServer(String user, String password) {

		if (isConnectedWithServer) {

			try {

				out.write(("USER " + user + "\n").getBytes());
				ConsoleLogger.log(LogType.INFO, "Envoi au serveur de la commande : USER " + user);

				String _331 = buf.readLine();
				ConsoleLogger.log(LogType.INFO, "RÃ©ponse du serveur : " + _331);

				if ("331".equals(_331.substring(0, 3))) {
					out.write(("PASS " + password + "\n").getBytes());
					ConsoleLogger.log(LogType.INFO, "Envoi au serveur de la commande : PASS " + password);

					String _230 = buf.readLine();
					ConsoleLogger.log(LogType.INFO, "RÃ©ponse du serveur : " + _230);

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
	
	/**
	 * Permet la déconnexion du serveur
	 * 
	 * @return true si déconnecté, false si erreurs ou déjà déconnecté
	 */
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
	
	/**
	 * Permet de savoir quel est le répertoire courant de l'utilisateur
	 * 
	 * @return le path du répertoire courant (/path/another_path)
	 */
	public String pwd(){
		if(isConnectedWithServer){
			try {
				out.write("PWD\n".getBytes());
				String res = buf.readLine();
				
				if("257 ".equals(res.substring(0, 4))){
					ConsoleLogger.log(LogType.INFO, res.substring(4));
					return res.substring(4);
				} else{
					return "KO";
				}
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot write PWD command");
				return "KO";
			}
		} else {
			return "KO not connected";
		}
	}
	
	/**
	 * Permet de lister un répertoire
	 * 
	 * @param dir : répertoire demandé;
	 * @return le contenu du répertoire demandé, "Error happened during transfert" ou "Error during list" si erreur pendant le traitement
	 */
	public String list(String dir){
		
		try {
			if(!pasv()){
				out.write("LIST\n".getBytes());
			}else{
				port();
				out.write("LIST\n".getBytes());
				data_socket = flux_socket.accept();
			}
			if("150".equals(buf.readLine().substring(0, 3))){
				BufferedReader flux_buffer = new BufferedReader(new InputStreamReader(data_socket.getInputStream())); 
				
				String list = flux_buffer.readLine();
				String res = "<a href=\"/rest/tp2/ftp/cdup\">..</a><br />";
				while(list != null){
					if(list.charAt(0) == 'd'){
						String[] dir1 = list.split(" ");
						res+="<a href=\"/rest/tp2/ftp/cwd/"+ dir1[dir1.length-1] +"\" >"+list+"</a><br />";
					}
					else{
						res += list + "<br />";
					}
					list = flux_buffer.readLine();
				}
				
				flux_buffer.close();
				data_socket.close();
				
				if("226".equals(buf.readLine().substring(0, 3))){
					ConsoleLogger.log(LogType.INFO, "List Success");
					return res;
				} else {
					return "Error happened during transfert";
				}
			} else {
				return "Error during list";
			}
			
			
			
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Cannot list " + dir);
		}
		return "KO";
	}
	
	/**
	 * Permet de configurer le serveur en mode actif
	 * 
	 * @return
	 */
	public boolean port(){
		if(isConnectedWithServer()){
			Inet4Address addr;
			try {
				addr = (Inet4Address) InetAddress.getLocalHost();
				String Ipv4 = addr.getHostAddress().replace(".", ",");
				int port = flux_socket.getLocalPort();
				ConsoleLogger.log(LogType.INFO, "socket data : " + Ipv4 + "," + port/256 + "," + port%256 );
				
				
				out.write(("PORT " + Ipv4 + "," + port/256 + "," + port%256 + "\n").getBytes());
				
				String response = buf.readLine();
				ConsoleLogger.log(LogType.INFO, response);
				
				if( "200".equals(response.substring(0, 3))){
					passiveMode = false;
					ConsoleLogger.log(LogType.INFO, "PORT Successful");
					return true;
				} else{
					ConsoleLogger.log(LogType.ERROR, "Failed to configure PORT");
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
	
	/**
	 * Permet de configurer le serveur en mode passif
	 * 
	 * @return
	 */
	public boolean pasv(){
		if(isConnectedWithServer){
			
			try {
				out.write("PASV\n".getBytes());
				//TODO
				String a = buf.readLine();
				ConsoleLogger.log(LogType.INFO, "Reponse du serveur : " +a);
				String infos[] = analyseAddress(a);
				
				String host = (infos[0] + "," + infos[1] + "," + infos[2] + "," + infos[3]).replace(",", ".");
				int port = Integer.valueOf(infos[4])*256 + Integer.valueOf(infos[5]);
				ConsoleLogger.log(LogType.INFO, "Connecting to : " + host + ":" + port +" ...");
				
				data_socket = new Socket(host, port);
				
				ConsoleLogger.log(LogType.INFO, "PASV command DONE");
				passiveMode = true;
				
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot send pasv command's to the ftp server");
			}
			return false;
			
		} else {
			return false;
		}
	}
	
	/**
	 * Permet d'analyser et de retourner les informations de connexion à une socket du serveur FTP
	 * 
	 * @param s : phrase à analyser
	 * @return les informations de connexions (ex: "127,0,0,1,256,256")
	 */
	public static String[] analyseAddress(String s){
		String[] a = s.split(",");
		Pattern p = Pattern.compile("\\d{1,}+");
		
		while(!Pattern.matches("\\d{1,}+", a[0]))
			a[0] = a[0].substring(1, a[0].length());
		
		while(!Pattern.matches("\\d{1,}+", a[5]))
			a[5] = a[5].substring(0, a[0].length()-1);
		
		return a;
	}
	
	/**
	 * Permet l'upload d'un fichier sur le serveur FTP
	 * 
	 * @param name : le nom du fichier à upload
	 * @return true si upload OK, false si erreurs
	 */
	public boolean store(String name){
		
		/* flux du fichier */
		File f = new File(name);
		FileReader reader;
		BufferedReader buf = null;
		try {
			reader = new FileReader(f);
			buf = new BufferedReader(reader);

			if(port()){
				try {
					out.write(("STOR " + name + "\n").getBytes());
					try {
						String string;
						string = buf.readLine(); //warning, String s = buf.readLine() has no effect
						Socket s = flux_socket.accept();
						DataOutputStream flux_out = new DataOutputStream(s.getOutputStream());
						
						while (string != null && buf != null) {
							flux_out.write((string+"\r\n").getBytes());
							string = buf.readLine();
						}
						
						flux_out.write("\n".getBytes());
						flux_out.close();
						
						buf.close();
						return true;

					} catch (IOException e) {
						return false;
					}
				} catch (IOException e) {
				}
			}
		} catch (FileNotFoundException e) {
			ConsoleLogger.log(LogType.ERROR, "File not found");
		}
		return false;
	}
	
	/**
	 * Permet de descendre l'arborescence du serveur
	 * 
	 * @param dir : répertoire demandé
	 * @return true si descente OK, false si erreurs ou pas de droits d'accès au répertoire
	 */
	public boolean cwd(String dir){
		
		try {
			out.write(("CWD " + dir + "\n").getBytes());
			
			String _250 = buf.readLine();
			ConsoleLogger.log(LogType.INFO, "CWD Reponse du serveur " + _250);
			return "250".equals(_250.substring(0, 3));
				
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Cannot send CWD command");
			return false;
		}
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

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
