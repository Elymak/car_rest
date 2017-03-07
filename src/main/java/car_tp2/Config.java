package car_tp2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import services.FtpService;

/**
 * Configuration de l'application.
 * Declaration des classes de ressource REST.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */
@Configuration
public class Config {

	/**
	 * Declaration des classes de ressource REST.
	 * Ajouter une ligne par classe de ressource REST.
	 */
	protected void addResources( List<Object> resources ) {
		 resources.add( new FtpService() );
	}
	
	/**
	 * Configuration au demarrage.
	 */
	@Bean @DependsOn( "cxf" )
	public Server jaxRsServer() {

		JAXRSServerFactoryBean factory =
			RuntimeDelegate.getInstance().createEndpoint(
				new JaxRsApiApplication(), JAXRSServerFactoryBean.class );
		
		List<Object> serviceBeans = new ArrayList<Object>();
		addResources(serviceBeans);
		
		factory.setServiceBeans(serviceBeans);
		factory.setAddress( "/" + factory.getAddress() );
		factory.setProviders( Arrays.<Object>asList( new JacksonJsonProvider() ) );
		return factory.create();
	}

	/**
	 * Operations a effectuer lors de l'arret.
	 */
	@Bean( destroyMethod = "shutdown" )
	public SpringBus cxf() {
		return new SpringBus();
	}	
}

@ApplicationPath("tp2")
class JaxRsApiApplication extends Application {
}
