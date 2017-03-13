package services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import log.ConsoleLogger;
import log.LogType;
import connexion.FtpClient;
import exceptions.AccessDeniedException;
import exceptions.FileTransfertException;

/**
 * 
 * Service principal des diff�rentes commandes FTP
 * Les commandes s'appelent avec le pattern suivant : "/{commande}/{0 � X param�tres}"
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
	
	@GET
	@Produces("text/html")
	public String start(){
		return "Please connect to a server";
	}
	
	
	/**
	 * service qui permet la connection � un serveur ftp
	 * en pr�cisant dans l'url l'addresse du serveur (127.0.0.1 ou ftp.univ-lille1.fr par exemple)
	 * et le port (21 par d�faut)
	 * 
	 * @param url
	 * @param port
	 * @return OK si connect�, KO sinon
	 */
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
	
	/**
	 * 
	 * service qui permet l'authentification d'un utilisateur
	 * � l'actuel serveur connect�
	 * le service demande le nom d'utilisateur et le mot de passe
	 * 
	 * 
	 * @param user
	 * @param password
	 * @return l'affichage du r�pertoire courant de l'utilisateur si login ok,
	 * "Failed to Login" si mot de passe incorrect,
	 * "Not connected with server, please try again" si on est connect� � aucun serveur 
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
	 * Liste le r�pertoire courtant de l'utilisateur
	 * @return le r�pertoire courant si OK,
	 * "KO" si l'utilsateur n'a pas les droits
	 * "KO : You are not connected with the FTP server" si pas connect� au serveur
	 */
	public String list() {
		if (ftpClient.isConnectedWithServer()) {
			try {
				return ftpClient.list();
			} catch (AccessDeniedException e) {
				return "<h2 style=\"color:red;\">ACCESS DENIED</h2><br /><a href=\"/rest/tp2/ftp/cdup\">Go Back</a>";
			}
		} else {
			return "KO : You are not connected with the FTP server";
		}
	}
	
	/**
	 * Service qui permet de descendre dans l'arborescence du serveur
	 * @param dir
	 * @return affiche le nouveau r�pertoire courant, "KO" si pas les permissions pour 
	 * naviguer dans le r�pertoire demand�, "You are not connected with the server" si d�connect� du serveur
	 */
	@Path("/cwd/{dir}")
	@GET
	@Produces("text/html")
	public String cwd(@PathParam("dir") String dir){
		
		
		if(ftpClient.isConnectedWithServer()){
			ConsoleLogger.log(LogType.INFO, "Current dir is " + ftpClient.pwd());
			if(ftpClient.cwd(dir)){
				return list();
			} else {
				return "KO, Cannot Go down in hierarchy";
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
	@Path("/store")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response store(@FormDataParam("file") InputStream uploadedInputStream,
            			@FormDataParam("file") FormDataContentDisposition fileDetail){
		if(ftpClient.isConnectedWithServer()){
			//TODO ftpClient.store()
			ConsoleLogger.log(LogType.INFO, "Store service");
			File f = new File("toto");
			
			ftpClient.store(f);
		}
		return Response.ok("KO").build();
	}
	
	@GET
	@Path("/retr/{name}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public StreamingOutput retrieve(@PathParam("name") String name){
		ConsoleLogger.log(LogType.INFO, "patate " + name);
		
		if(ftpClient.isConnectedWithServer()){
			File file1;
			try {
				file1 = ftpClient.retrieve(name);
				
				return new StreamingOutput() {
					
					@Override
					public void write(OutputStream arg0) throws IOException, WebApplicationException {
						BufferedOutputStream bus = new BufferedOutputStream(arg0);
						try {
							String home = System.getProperty("user.home");
							if(file1.renameTo(new File(home + "/Downloads/" + file1.getName())))
								ConsoleLogger.log(LogType.INFO, "Changing path OK");
							else
								ConsoleLogger.log(LogType.ERROR, "Bad Changing path");
							FileInputStream fizip = new FileInputStream(file1);
							byte[] buffer2 = IOUtils.toByteArray(fizip);
							bus.write(buffer2);
							
							bus.close();
						} catch (Exception e) {
							//TODO
							ConsoleLogger.log(LogType.INFO, "patate2");
						}
					}
				};
			} catch (FileTransfertException e1) {
				// TODO Auto-generated catch block
				ConsoleLogger.log(LogType.INFO, "patate3");
			} catch (AccessDeniedException e1) {
				// TODO Auto-generated catch block
				ConsoleLogger.log(LogType.INFO, "patate4");
			}
		}
		return null;
	}
	
	/**
	 * Service qui permet la deconnexion du serveur
	 * 
	 * @return "Disconnected from ftp server" si OK, "Error happenned during deconnection" si KO,
	 *  "You are already disconnected" si d�j� d�connect�
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
