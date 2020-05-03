package com.octo.tools.crud;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.octo.tools.crudweb.CrudGenerator;

/**
 * Goal which generates an Angular JS Administration application from the JPA entities of the model
 *
 */
@Mojo( name = "crudweb", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class CrudWebGeneratorMojo
    extends AbstractGeneratorMojo
{
	
	/**
     * Name of the persistent unit defined in the persistence.xml
     */
	@Parameter( property = "persistentUnitName", required = true )
    private String persistentUnitName;
	
    /**
     * Location of the generated files
     */
    @Parameter( defaultValue = "${project.build.directory}/classes/static/admin", property = "outputDir", required = false )
    private String outputDirectory;
    
    /**
     * URL of the Rest API (Public URL)
     */
    @Parameter( property = "restApiUrl", required = true )
    private String restApiUrl;
    
    /**
     * URL of the local API - used in case of the APIs are behind a proxy
     */
    @Parameter( property = "localApiUrl", required = false )
    private String localApiUrl;
    
    /**
     * Local path of the logo image file displayed in the admin GUI
     */
    @Parameter( property = "logoPath", defaultValue = "admin/img/logo.png", required = false )
    private String logoPath;
    
    /**
     * Welcome message displayed in the welcome page of the admin GUI
     */
    @Parameter( property = "welcomeMsg", defaultValue = "<h3>Welcome to the CRUD Administration application<h3>", required = false )
    private String welcomeMsg;
    
    
    /**
     * Prefix used to deploy the application
     */
    @Parameter( property = "contextPath", defaultValue = "admin", required = false )
    private String contextPath;
    
    

    public void execute()
        throws MojoExecutionException
    {
        
        
        try {
			new CrudGenerator().generate(persistentUnitName, outputDirectory, restApiUrl, logoPath, contextPath, welcomeMsg, localApiUrl);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception during generation", e);
		}
       
    }
}
