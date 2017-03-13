package connexion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import exceptions.AccessDeniedException;
import exceptions.FileTransfertException;
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
	 * Ce constructeur permet d'instancier un nouveau FtpClient en se connectant directement � un serveur FTP
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
		ConsoleLogger.log(LogType.INFO, "Connection acceptée avec le serveur");
		String _220 = buf.readLine();
		ConsoleLogger.log(LogType.INFO, "Réponse du serveur : " + _220);

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
	
	/**
	 * Permet la d�connexion du serveur
	 * 
	 * @return true si d�connect�, false si erreurs ou d�j� d�connect�
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
					buf.reset();
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
	 * Permet de savoir quel est le r�pertoire courant de l'utilisateur
	 * 
	 * @return le path du r�pertoire courant (/path/another_path)
	 */
	public String pwd(){
		if(isConnectedWithServer){
			try {
				ConsoleLogger.log(LogType.INFO, "PWD");
				out.write("PWD\n".getBytes());
				String res = buf.readLine();
				ConsoleLogger.log(LogType.INFO, "Reponse du serveur " + res);
				
				if("257 ".equals(res.substring(0, 4))){
					ConsoleLogger.log(LogType.INFO, res.substring(4));
					buf.reset();
					return res.substring(4);
				} else{
					buf.reset();
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
	 * Permet de lister le r�pertoire courant
	 * 
	 * @return le contenu du r�pertoire courant, "Error happened during transfert" ou "Error during list" si erreur pendant le traitement
	 * @throws AccessDeniedException 
	 */
	public String list() throws AccessDeniedException{
		
		try {
			if(pasv()){
				ConsoleLogger.log(LogType.INFO, "LIST");
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
						String[] dir1 = list.split(" ");
						res += list + "<a href=\"/rest/tp2/ftp/retr/"+dir1[dir1.length-1]+"\">DOWNLOAD</a><br />";
					}
					list = flux_buffer.readLine();
				}
				
				flux_buffer.close();
				data_socket.close();
				
//				res += "<br/><form action=\"/rest/tp2/ftp/store\" method=\"post\"><input type=\"submit\"></form>";
				res += 	"<form action=\"/rest/tp2/ftp/store\" method=\"post\" enctype=\"multipart/form-data\">"
						+ 	"<p>Select a file in this directory : <input type=\"file\" name=\"file\" size=\"45\" /></p>"
						+ 	"<input type=\"submit\" value=\"Upload It\" />"
						+ "</form>";
				
				if("226".equals(buf.readLine().substring(0, 3))){
					ConsoleLogger.log(LogType.INFO, "List OK");
					return res;
				} else {
					buf.reset();
					return "Error happened during transfert";
				}
			} else {
				buf.reset();
				return "Error during list";
			}
			
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Cannot list ");
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

				ConsoleLogger.log(LogType.INFO, "PORT " + Ipv4 + "," + port/256 + "," + port%256);
				out.write(("PORT " + Ipv4 + "," + port/256 + "," + port%256 + "\n").getBytes());
				
				String response = buf.readLine();
				ConsoleLogger.log(LogType.INFO, response);
				
				if( "200".equals(response.substring(0, 3))){
					passiveMode = false;
					ConsoleLogger.log(LogType.INFO, "PORT OK");
					buf.reset();
					return true;
				} else{
					ConsoleLogger.log(LogType.ERROR, "PORT KO");
					buf.reset();
					return false;
				}
				
				
			} catch (UnknownHostException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot get Localhost InetAddress");
				return false;
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot send PORT command's datas to the FTP server");
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

	public boolean pasv() throws AccessDeniedException{
		if(isConnectedWithServer){
			
			try {
				ConsoleLogger.log(LogType.INFO, "PASV");
				out.write("PASV\n".getBytes());
				String a = buf.readLine();
				if ("227".equals(a.substring(0, 3))) {
					ConsoleLogger.log(LogType.INFO, "Reponse du serveur : " + a);
					String infos[] = analyseAddress(a);
					String host1 = infos[0] + "." + infos[1] + "." + infos[2] + "." + infos[3];
					int port1 = Integer.valueOf(infos[4]) * 256 + Integer.valueOf(infos[5]);

					ConsoleLogger.log(LogType.INFO, "Connecting to : " + host1 + ":" + port1 + " ...");
					data_socket = new Socket(host1, port1);

					ConsoleLogger.log(LogType.INFO, "PASV OK");
					passiveMode = true;
					return true;
				} else {
					ConsoleLogger.log(LogType.ERROR, "Access denied");
					this.buf = new BufferedReader(new InputStreamReader(commandes_socket.getInputStream()));
					//buf.reset();
					//TODO
					throw new AccessDeniedException();
				}
				
			} catch (IOException e) {
				ConsoleLogger.log(LogType.ERROR, "Cannot send pasv command's to the ftp server");
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	/**
	 * Permet d'analyser et de retourner les informations de connexion � une socket du serveur FTP
	 * 
	 * @param s : phrase � analyser
	 * @return les informations de connexions (ex: "127,0,0,1,256,256")
	 */
	public static String[] analyseAddress(String s){
		String[] a = s.split(",");
		
		while(!Pattern.matches("\\d{1,}+", a[0]))
			a[0] = a[0].substring(1, a[0].length());
		
		while(!Pattern.matches("\\d{1,}+", a[5]))
			a[5] = a[5].substring(0, a[5].length()-1);
		
		return a;
	}
	
	/**
	 * Permet l'upload d'un fichier sur le serveur FTP
	 * 
	 * @param name : le nom du fichier � upload
	 * @return true si upload OK, false si erreurs
	 */
	public boolean store(File f){
		
		/* flux du fichier */
		FileReader reader;
		BufferedReader file_buf = null;
		try {
			reader = new FileReader(f);
			file_buf = new BufferedReader(reader);

			if(port()){
				try {
					out.write(("STOR " + f.getName() + "\n").getBytes());
					try {
						String string;
						string = file_buf.readLine(); //warning, String s = buf.readLine() has no effect
						Socket s = flux_socket.accept();
						DataOutputStream flux_out = new DataOutputStream(s.getOutputStream());
						
						while (string != null && file_buf != null) {
							flux_out.write((string+"\r\n").getBytes());
							string = file_buf.readLine();
						}
						
						flux_out.write("\n".getBytes());
						flux_out.close();
						
						file_buf.close();
						return true;

					} catch (IOException e) {
						return false;
					}
				} catch (IOException e) {
				}
			}
			//TODO PASV
		} catch (FileNotFoundException e) {
			ConsoleLogger.log(LogType.ERROR, "File not found");
		}
		return false;
	}
	
	public File retrieve(String name) throws FileTransfertException, AccessDeniedException{
		File f = new File(name);
		BufferedReader socket_input_flux = null;
//		PrintWriter pw = null;
		FileWriter fw = null;
		
		try {
			pasv();
			
//			pw = new PrintWriter (new BufferedWriter (new FileWriter (f)));
			fw = new FileWriter(f);
			socket_input_flux = new BufferedReader(new InputStreamReader(data_socket.getInputStream()));
			
			ConsoleLogger.log(LogType.INFO, "RETR " + name);
			out.write(("RETR " + name + "\n").getBytes());
			
			String res = buf.readLine();
			ConsoleLogger.log(LogType.INFO, "Reponse du serveur " + res);
			
			if("125".equals(res.substring(0, 3))){
				 String tmp = socket_input_flux.readLine();

				while (tmp != null) {
//					pw.println(tmp);
					fw.write(tmp+"\r\n");
					tmp = socket_input_flux.readLine();
				}
				
//				pw.close();
				fw.close();
				
				res = buf.readLine();
				if("226".equals(res.substring(0, 3)))
					return f;
				else
					throw new FileTransfertException();
			} else {
//				pw.close();
				fw.close();
				throw new FileTransfertException();
			}
			
		} catch (IOException e) {
			//TODO Consolelogger
			ConsoleLogger.log(LogType.ERROR, "Erreur while creating file");
		}
		throw new FileTransfertException();
	}
	
	/**
	 * Permet de descendre l'arborescence du serveur
	 * 
	 * @param dir : r�pertoire demand�
	 * @return true si descente OK, false si erreurs ou pas de droits d'acc�s au r�pertoire
	 */
	public boolean cwd(String dir){
		
		try {
			ConsoleLogger.log(LogType.INFO, "CWD " + dir);
			out.write(("CWD " + dir + "\n").getBytes());
			
			String _250 = buf.readLine();
			ConsoleLogger.log(LogType.INFO, "Reponse du serveur " + _250);
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
