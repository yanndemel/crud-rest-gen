package com.octo.tools.crud;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.octo.tools.crudweb.CrudGenerator;

/**
 * Goal which generates an Angular 
 *
 */
@Mojo( name = "crudweb", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class CrudWebGeneratorMojo
    extends AbstractMojo
{
	
	@Parameter( property = "persistentUnitName", required = true )
    private String persistentUnitName;
	
    /**
     * Location of the generated files
     */
    @Parameter( defaultValue = "${project.build.directory}/classes/static/admin", property = "outputDir", required = false )
    private String outputDirectory;
    
    /**
     * URL of the Rest API
     */
    @Parameter( property = "restApiUrl", required = true )
    private String restApiUrl;
    
    

    public void execute()
        throws MojoExecutionException
    {
        
        
        try {
			new CrudGenerator().generate(persistentUnitName, outputDirectory, restApiUrl);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception during generation", e);
		}
       
    }
}
