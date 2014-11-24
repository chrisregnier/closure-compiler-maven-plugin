package com.github.chrisregnier.closurecompiler;

import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * 
 * @author cregnier
 *
 */
public class ModuleOptions {

	/**
	 * The module's ID name
	 */
	@Parameter(required=true)
	public String id;
	
	/**
	 * The output location of the compiled module
	 */
	@Parameter
	public String outputFile;
	
	
	/**
	 * A list of filesets to be used for sources.
	 */
	@Parameter
	public List<FileSet> sources;

	/**
	 * A list of module ID's that are required as dependencies
	 */
	@Parameter
	public List<String> dependencies;
}
