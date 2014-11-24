package com.github.chrisregnier.closurecompiler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.AnonymousFunctionNamingPolicy;
import com.google.javascript.jscomp.PropertyRenamingPolicy;
import com.google.javascript.jscomp.VariableRenamingPolicy;

/**
 * 
 * @author cregnier
 *
 */
public class PluginOptions {

	public void setOptionsForPlugin(CompilerOptions compilerOptions) throws MojoFailureException {
		compilerOptions.prettyPrint = prettyPrint;
		if (languageIn != null)
			configureLanguageIn(compilerOptions, languageIn);
		if (languageOut != null)
			configureLanguageOut(compilerOptions, languageOut);
		compilerOptions.setTrustedStrings(true);
		if (variableRenamingPolicy != null)
			compilerOptions.setVariableRenaming(variableRenamingPolicy);
		if (propertyRenamingPolicy != null)
			compilerOptions.setPropertyRenaming(propertyRenamingPolicy);
	    
		//renaming properties
		if (aggressiveRenaming != null)
			compilerOptions.setAggressiveRenaming(aggressiveRenaming);
		if (propertyAffinity != null)
			compilerOptions.setPropertyAffinity(propertyAffinity);
		if (labelRenaming != null)
			compilerOptions.labelRenaming = labelRenaming;
		if (generatePseudoNames != null)
			compilerOptions.generatePseudoNames = generatePseudoNames;
		if (shadowVariables != null)
			compilerOptions.setShadowVariables(shadowVariables);
		if (preferStableNames != null)
			compilerOptions.setPreferStableNames(preferStableNames);
		if (renamePrefix != null)
			compilerOptions.renamePrefix = renamePrefix;
		if (aliasKeywords != null)
			compilerOptions.aliasKeywords = aliasKeywords;
		if (collapseProperties != null)
			compilerOptions.collapseProperties = collapseProperties;
		if (collapsePropertiesOnExternTypes != null)
			compilerOptions.setCollapsePropertiesOnExternTypes(collapsePropertiesOnExternTypes);
		if (collapseObjectLiterals != null)
			compilerOptions.setCollapseObjectLiterals(collapseObjectLiterals);
		if (devirtualizePrototypeMethods != null)
			compilerOptions.devirtualizePrototypeMethods = devirtualizePrototypeMethods;
		if (disambiguateProperties != null)
			compilerOptions.disambiguateProperties = disambiguateProperties;
		if (ambiguateProperties != null)
			compilerOptions.ambiguateProperties = ambiguateProperties;
		if (anonymousFunctionNaming != null)
			compilerOptions.anonymousFunctionNaming = anonymousFunctionNaming;
		if (exportTestFunctions != null)
			compilerOptions.exportTestFunctions = exportTestFunctions;
	}
	
	/**
	 * @throws MojoFailureException 
	 */
	public void configureLanguageIn(CompilerOptions compilerOptions, String languageIn) throws MojoFailureException {
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
	public void configureLanguageOut(CompilerOptions compilerOptions, String languageOut) throws MojoFailureException {
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
	@Parameter(defaultValue="${project.build.directory}/$project.artifactId}-${project.version}-min.js/", required=true )
	public File modulesOutputDirectory;
	
	
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
	 * Allows you to skip any processing by this plugin.
	 */
	@Parameter(defaultValue="false")
	public boolean skip;

	
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
	
	/**
	 * A list of modules to be used for sources.
	 */
	@Parameter
	public List<ModuleOptions> modules;

	//---------- Renaming related properties
	
	/**
	 * Renaming policies that can be used
	 * {@link VariableRenamingPolicy#ALL}
	 * {@link VariableRenamingPolicy#LOCAL}
	 * {@link VariableRenamingPolicy#UNSPECIFIED}
	 * {@link VariableRenamingPolicy#OFF}
	 */
	@Parameter
	public VariableRenamingPolicy variableRenamingPolicy;
	
	/**
	 * Renaming policies that can be used
	 * {@link PropertyRenamingPolicy#AGGRESSIVE_HEURISTIC}
	 * {@link PropertyRenamingPolicy#ALL_UNQUOTED}
	 * {@link PropertyRenamingPolicy#HEURISTIC}
	 * {@link PropertyRenamingPolicy#UNSPECIFIED}
	 * {@link PropertyRenamingPolicy#OFF}
	 */
	@Parameter
	public PropertyRenamingPolicy propertyRenamingPolicy;
	

	@Parameter
	public Boolean aggressiveRenaming;

	@Parameter
	public Boolean propertyAffinity;
	
	@Parameter
	public Boolean labelRenaming;
	
	@Parameter
	public Boolean generatePseudoNames;
	
	@Parameter
	public Boolean shadowVariables;
	
	@Parameter
	public Boolean preferStableNames;
	
	@Parameter
	public String renamePrefix;

	@Parameter
	public Boolean aliasKeywords
	;
	@Parameter
	public Boolean collapseProperties;
	
	@Parameter
	public Boolean collapsePropertiesOnExternTypes;

	@Parameter
	public Boolean collapseObjectLiterals;
	
	@Parameter
	public Boolean devirtualizePrototypeMethods;
	
	@Parameter
	public Boolean disambiguateProperties;
	
	@Parameter
	public Boolean ambiguateProperties;

	/**
	 * Possible values are:
	 * {@link AnonymousFunctionNamingPolicy#MAPPED}
	 * {@link AnonymousFunctionNamingPolicy#UNMAPPED}
	 * {@link AnonymousFunctionNamingPolicy#OFF}
	 */
	@Parameter
	public AnonymousFunctionNamingPolicy anonymousFunctionNaming;
	
	@Parameter
	public Boolean exportTestFunctions;
	
}
