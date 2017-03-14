package services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import log.ConsoleLogger;
import log.LogType;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import connexion.FtpClient;
import exceptions.AccessDeniedException;
import exceptions.FileTransfertException;

/**
 * 
 * Service principal des diffï¿½rentes commandes FTP
 * Les commandes s'appelent avec le pattern suivant : "/{commande}/{0 ï¿½ X paramï¿½tres}"
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
	 * service qui permet la connection ï¿½ un serveur ftp
	 * en prï¿½cisant dans l'url l'addresse du serveur (127.0.0.1 ou ftp.univ-lille1.fr par exemple)
	 * et le port (21 par dï¿½faut)
	 * 
	 * @param url
	 * @param port
	 * @return OK si connectï¿½, KO sinon
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
	 * ï¿½ l'actuel serveur connectï¿½
	 * le service demande le nom d'utilisateur et le mot de passe
	 * 
	 * 
	 * @param user
	 * @param password
	 * @return l'affichage du rï¿½pertoire courant de l'utilisateur si login ok,
	 * "Failed to Login" si mot de passe incorrect,
	 * "Not connected with server, please try again" si on est connectï¿½ ï¿½ aucun serveur 
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
	 * Liste le rï¿½pertoire courtant de l'utilisateur
	 * @return le rï¿½pertoire courant si OK,
	 * "KO" si l'utilsateur n'a pas les droits
	 * "KO : You are not connected with the FTP server" si pas connectï¿½ au serveur
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
	 * @return affiche le nouveau rï¿½pertoire courant, "KO" si pas les permissions pour 
	 * naviguer dans le rï¿½pertoire demandï¿½, "You are not connected with the server" si dï¿½connectï¿½ du serveur
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
	 * @return Response
	 */
	@POST
	@Path("/store")
	@Consumes("multipart/form-data")
	public Response store(MultipartFormDataInput input) {
		if (ftpClient.isConnectedWithServer()) {
		
			String fileName = "";

			Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
			List<InputPart> inputParts = uploadForm.get("fileForm");

			for (InputPart inputPart : inputParts) {

			 try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);
				InputStream inputStream = inputPart.getBody(InputStream.class,null);
				byte [] bytes = IOUtils.toByteArray(inputStream);
				File f = writeFile(bytes,fileName);
				
				try {
					if(ftpClient.store(f)){
						return Response.ok("OK").build();
					}
				} catch (AccessDeniedException e) {
					// TODO consolelogerer
				}
				list();

			  } catch (IOException e) {
				e.printStackTrace();
			  }

			}
			
		}
		return Response.ok("KO").build();
	}
	
	/**
	 * 
	 * Methode qui permet de récupérer le nom d'un fichier récupéré du formulaire d'envoi
	 * @param header
	 * @return
	 */
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}
	
	/**
	 * Methode qui créer un fichier récupéré depuis un formulaire
	 * @param content
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private File writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);

		FileOutputStream fop = new FileOutputStream(file);
		fop.write(content);
		fop.flush();
		fop.close();

		return file;
	}
	
	/**
	 * service qui permet le téléchargement d'un fichier texte depuis un répertoire distant
	 * @param name
	 * @return
	 */
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
	 *  "You are already disconnected" si dï¿½jï¿½ dï¿½connectï¿½
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
