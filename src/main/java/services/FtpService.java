package services;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import connexion.FtpClient;
import log.ConsoleLogger;
import log.LogType;

/**
 * 
 * Service principal des différentes commandes FTP
 * Les commandes s'appelent avec le pattern suivant : "/{commande}/{0 à X paramètres}"
 * 
 * @author Serial
 *
 */
@Path("/ftp")
public class FtpService {

	/**
	 * object qui permet l'interaction avec le serveur FTP
	 */
	private FtpClient ftpClient;
	
	/**
	 * service qui permet la connection à un serveur ftp
	 * en précisant dans l'url l'addresse du serveur (127.0.0.1 ou ftp.univ-lille1.fr par exemple)
	 * et le port (21 par défaut)
	 * 
	 * @param url
	 * @param port
	 * @return OK si connecté, KO sinon
	 */
	@GET
	@Path("/connect/{url}/{port}")
	@Produces("text/html")
	public String connect(@PathParam("url") String url, @PathParam("port") String port){
		
		ConsoleLogger.log(LogType.INFO, "Tentative de connection au serveur '" + url + ":"+port+"'");
		try {
			ftpClient = new FtpClient(url, port);
			ConsoleLogger.log(LogType.INFO, "ConnectÃ© au serveur");
			ConsoleLogger.log(LogType.INFO, "En attente d'une authentification");
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Impossible de se connecter au serveur");
			return "KO";
		}
		
		return "OK";
		
	}
	
	/**
	 * 
	 * service qui permet l'authentification d'un utilisateur
	 * à l'actuel serveur connecté
	 * le service demande le nom d'utilisateur et le mot de passe
	 * 
	 * 
	 * @param user
	 * @param password
	 * @return l'affichage du répertoire courant de l'utilisateur si login ok,
	 * "Failed to Login" si mot de passe incorrect,
	 * "Not connected with server, please try again" si on est connecté à aucun serveur 
	 */
	@GET
	@Path("/login/{user}/{password}")
	@Produces("text/html")
	public String login(@PathParam("user") String user, @PathParam("password") String password){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.connectToServer(user, password)){
				return list();
			} else {
				return "Failed to Login";
			}
		} else {
			return "Not connected with server, please try again";
		}
		
	}
	
	/**
	 * Liste le répertoire courtant de l'utilisateur
	 * @return le répertoire courant si OK,
	 * "KO" si l'utilsateur n'a pas les droits
	 * "KO : You are not connected with the FTP server" si pas connecté au serveur
	 */
	public String list(){
		if(ftpClient.isConnectedWithServer()){
			String dir = ftpClient.pwd();
			if(!"KO".equals(dir.substring(0, 2)))
				return ftpClient.list(dir);
			else
				return "KO";
		} else {
			return "KO : You are not connected with the FTP server";
		}
	}
	
	/**
	 * Service qui permet de descendre dans l'arborescence du serveur
	 * @param dir
	 * @return affiche le nouveau répertoire courant, "KO" si pas les permissions pour 
	 * naviguer dans le répertoire demandé, "You are not connected with the server" si déconnecté du serveur
	 */
	@Path("/cwd/{dir}")
	@GET
	@Produces("text/html")
	public String cwd(@PathParam("dir") String dir){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.cwd(dir)){
				return list();
			} else {
				return "KO";
			}
		} else {
			return "You are not connected with the server";
		}
	}
	
	/**
	 * Service qui permet de remonter l'arboresence du serveur
	 * @return
	 */
	@Path("/cdup")
	@GET
	@Produces
	public String cdup(){
		return cwd("..");
	}
	
	/**
	 * Service qui permet d'upload un fichier sur le serveur
	 * 
	 * @param name = le nom du fichier
	 * @return
	 */
	@POST
	@Path("/store/{name}")
	public String store(@PathParam("name") String name){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.isPassiveMode()){
				//TODO passive mode
				return "KO need passive mode";
			} else {
				if(ftpClient.store(name)){
					return "STORE OK";
				}
			}
		}
		return "KO";
	}
	
	/**
	 * Service qui permet la deconnexion du serveur
	 * 
	 * @return "Disconnected from ftp server" si OK, "Error happenned during deconnection" si KO,
	 *  "You are already disconnected" si déjà déconnecté
	 */
	@GET
	@Path("/disconnect")
	@Produces("text/html")
	public String disconnect(){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.disconnect()){
				return "Disconnected from ftp server";
			} else {
				return "Error happenned during deconnection";
			}
		} else {
			return "You are already disconnected";
		}
	}
}
