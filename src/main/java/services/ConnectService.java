package services;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import connexion.FtpClient;

@Path("/ftp")
public class ConnectService {

	private FtpClient ftpClient;
	
	@Path("/connect/{url}/{port}/{user}/{password}")
	public String connect(@PathParam("url") String url, @PathParam("port") String port, @PathParam("user") String user, @PathParam("password") String password){
		
		
		
		return null;
	}
	
}
