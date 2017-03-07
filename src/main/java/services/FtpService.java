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

@Path("/ftp")
public class FtpService {

	private FtpClient ftpClient;
	
	@GET
	@Path("/connect/{url}/{port}")
	@Produces("text/html")
	public String connect(@PathParam("url") String url, @PathParam("port") String port){
		
		ConsoleLogger.log(LogType.INFO, "Tentative de connection au serveur '" + url + ":"+port+"'");
		try {
			ftpClient = new FtpClient(url, port);
			ConsoleLogger.log(LogType.INFO, "Connecté au serveur");
			ConsoleLogger.log(LogType.INFO, "En attente d'une authentification");
		} catch (IOException e) {
			ConsoleLogger.log(LogType.ERROR, "Impossible de se connecter au serveur");
			return "KO";
		}
		
		return "OK";
		
	}
	
	@GET
	@Path("/login/{user}/{password}")
	@Produces("text/html")
	public String login(@PathParam("user") String user, @PathParam("password") String password){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.connectToServer(user, password)){
				return "Login OK";
			} else {
				return "Failed to Login";
			}
		} else {
			return "Not connected with server, please try again";
		}
		
	}
	
	
	@GET
	@Path("/list")
	@Produces("text/html")
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
