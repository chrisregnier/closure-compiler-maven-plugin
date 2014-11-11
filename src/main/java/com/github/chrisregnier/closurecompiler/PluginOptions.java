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
			compilerOptions.prettyPrint = prettyPrint;
			if (languageIn != null)
				configureLanguageIn(languageIn);
			if (languageOut != null)
				configureLanguageOut(languageOut);
			compilerOptions.setTrustedStrings(true);
		}
		
		return compilerOptions;
	}
	
	/**
	 * @throws MojoFailureException 
	 */
	public void configureLanguageIn(String languageIn) throws MojoFailureException {
		LanguageMode mode;
		try {
			mode = LanguageMode.fromString(languageIn);
			compilerOptions.setLanguageIn(mode);
		}
		catch (Exception e) {
			throw new MojoFailureException("Invalid Language In. Possible values: [" + Arrays.asList(LanguageMode.values()) + "]" , e);
		}
	}
	
	/**
	 * @throws MojoFailureException 
	 */
	public void configureLanguageOut(String languageOut) throws MojoFailureException {
		LanguageMode mode;
		try {
			mode = LanguageMode.fromString(languageOut);
			compilerOptions.setLanguageOut(mode);
		}
		catch (Exception e) {
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
	 * 
	 */
	@Parameter(defaultValue="false")
	public boolean debug;
	
	/**
	 * Turns on 'file order matters' mode which will add extern and source includes in the exact
	 * order that they're specified in filesets. This mode only allows includes, and does not
	 * allow excludes or glob patterns.
	 */
	@Parameter(defaultValue="false")
	public boolean fileOrderMatters;
	
	
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
