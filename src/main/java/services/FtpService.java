package services;

import java.io.IOException;

import javax.ws.rs.GET;
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
	
<<<<<<< HEAD
	/** 
	 * sera appelé dans ftp client au lieu de l'url
	 * 
	 * */
	@GET
	@Path("/port")
	@Produces("text/html")
	public String port(){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.port()){
				return "Port has been configured";
			} else {
				return "Error happenned during PORT configuration";
			}
		} else {
			return "Not connected with server, please try again";
		}
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
=======
	@GET
	@Path("/disconnect")
	public String disconnect(){
		if(ftpClient.isConnectedWithServer()){
			if(ftpClient.disconnect()){
				return "Disconnected";
			} else {
				return "Failed to disconnect";
>>>>>>> a1d7a2335412cbb96e4ed256175d8e865d0b4099
			}
		} else {
			return "You are already disconnected";
		}
	}
	
<<<<<<< HEAD
	
	
=======
>>>>>>> a1d7a2335412cbb96e4ed256175d8e865d0b4099
}
