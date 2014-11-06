package com.github.chrisregnier.closurecompiler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

/**
 * 
 * @author cregnier
 *
 */
public class PluginOptions {

	private CompilerOptions compilerOptions = null;
	
	public CompilerOptions getCompilerOptions() throws MojoFailureException {
		if (compilerOptions == null) {
			compilerOptions = new CompilerOptions();
			compilerOptions.setPrettyPrint(prettyPrint);
			setLanguageIn(languageIn);
			setLanguageOut(languageOut);
		}
		
		return compilerOptions;
	}
	
	/**
	 * @throws MojoFailureException 
	 */
	public void setLanguageIn(String languageIn) throws MojoFailureException {
		LanguageMode mode;
		try {
			mode = LanguageMode.valueOf(languageIn);
			compilerOptions.setLanguageIn(mode);
		}
		catch (IllegalArgumentException e) {
			throw new MojoFailureException("Invalid Language In. Possible values: [" + Arrays.asList(LanguageMode.values()) + "]" , e);
		}
	}
	
	/**
	 * @throws MojoFailureException 
	 */
	public void setLanguageOut(String languageOut) throws MojoFailureException {
		LanguageMode mode;
		try {
			mode = LanguageMode.valueOf(languageOut);
			compilerOptions.setLanguageOut(mode);
		}
		catch (IllegalArgumentException e) {
			throw new MojoFailureException("Invalid Language Out. Possible values: [" + Arrays.asList(LanguageMode.values()) + "]" , e);
		}
	}
	
	
	//-----------------------------------------------------

	/**
	 * Forces a recompile of code even if nothing has changed.
	 * Useful to use with a property that can be set on the command line.
	 */
	@Parameter (property="closure.forceRecompile", defaultValue="false")
	public boolean forceRecompile;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="ADVANCED_OPTIMIZATIONS", required=true)
	public String compilationLevel;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="DEFAULT")
	public String warningLevel;
	
	
	/**
	 * 
	 */
	@Parameter(defaultValue="true")
	public boolean prettyPrint;
	
	
	@Parameter
	public String languageIn;
	
	
	@Parameter
	public String languageOut;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="${project.build.directory}/$project.artifactId}-${project.version}-min.js/", required=true )
	public File outputFile;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="true")
	public boolean addDefaultExterns;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="false")
	public boolean failOnWarnings;
	
	/**
	 * 
	 */
	@Parameter(defaultValue="true")
	public boolean failOnErrors;
	
	
	/**
	 * A list of filesets to be used for externs.
	 */
	@Parameter
	public List<FileSet> externs;

	/**
	 * A list of filesets to be used for sources.
	 */
	@Parameter
	public List<FileSet> sources;
	
}
