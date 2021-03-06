package com.github.chrisregnier.closurecompiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSModule;
import com.google.javascript.jscomp.MessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;

/**
 * 
 * @author cregnier
 * 
 * 
 * 
 * 
 */
@Mojo( name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
@Execute( goal = "compile", phase = LifecyclePhase.GENERATE_SOURCES)
public class ClosureCompilerMojo extends AbstractMojo {

	/**
	 * 
	 */
	@Parameter
	private PluginOptions options = null;
	
	
	private Map<String, ModuleOptions> moduleOptionsByName = Maps.newHashMap();
	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		
		if (options == null) {
			options = new PluginOptions();
		}
		
		Compiler.setLoggingLevel(Level.OFF);
		
		if (options.skip) {
			return;
		}
		
		List<SourceFile> externs = getExterns();
		List<SourceFile> sources = getSources();
		List<JSModule> modules = getModules();
		
		if (options.forceRecompile || isStale()) {
			CompilerOptions compilerOptions = new CompilerOptions();
			
			CompilationLevel compilationLevel = getCompilationLevel(options.compilationLevel);
			compilationLevel.setOptionsForCompilationLevel(compilerOptions);
			if (options.debug) {
				compilationLevel.setDebugOptionsForCompilationLevel(compilerOptions);
			}

			WarningLevel warningLevel = getWarningLevel(options.warningLevel);
			warningLevel.setOptionsForWarningLevel(compilerOptions);

			//override any of the options with ones set in the configuration
			options.setOptionsForPlugin(compilerOptions);
			
	        Compiler compiler = new Compiler();
	        MessageFormatter formatter = compilerOptions.errorFormat.toFormatter(compiler, false);
	        MavenErrorManager errorManager = new MavenErrorManager(formatter, log);
	        compiler.setErrorManager(errorManager);

	        Result result;
	        try {
		        if (modules.size() > 0) {
		        	if (sources.size() > 0) {
		        		log.warn("Found " +  sources.size() + " sources outside of module definitions that will not be compiled. Please put sources inside another module.");
		        	}
	
		        	log.info("Compiling " + modules.size() + " module(s) with " + externs.size() + " extern(s)");
		        	result = compiler.compileModules(externs, modules, compilerOptions);
		        }
		        else {
					log.info("Compiling " + sources.size() + " file(s) with " + externs.size() + " extern(s)");
		        	result = compiler.compile(externs, sources, compilerOptions);
		        }
	        }
	        catch (Exception e) {
	        	throw new MojoExecutionException("Error running closure compiler", e);
	        }
	        
		
            if (options.failOnWarnings && result.warnings.length > 0) {
            	throw new MojoFailureException("Failing on " + result.warnings.length + " warnings during closure compiler.");
            }
            if (options.failOnErrors && result.errors.length > 0) {
            	throw new MojoFailureException("Failing on " + result.errors.length + " errors during closure compiler.");
            }
	        
	        if (result.success) {
	        	try {
	        		if (modules.size() > 0) {
	        			for (JSModule mod : modules) {
	        				ModuleOptions mopts = moduleOptionsByName.get(mod.getName());
	        				File outputFile = null;
	        				if (mopts.outputFile != null) {
	        					outputFile = new File(mopts.outputFile);
	        				}
	        				
	        				if (outputFile == null) {
	        					outputFile = new File(options.modulesOutputDirectory, mod.getName() + ".js");
	        				}
	        				else if (!outputFile.isAbsolute()) {
	        					outputFile = new File(options.modulesOutputDirectory, outputFile.getPath());
	        				}
	        				outputFile.getParentFile().mkdirs();
	        				Files.write(compiler.toSource(mod), outputFile, Charsets.UTF_8);
	        			}
	        		}
	        		else {
	        			options.outputFile.getParentFile().mkdirs();
	        			Files.write(compiler.toSource(), options.outputFile, Charsets.UTF_8);
	        		}
	        	}
	        	catch (IOException e) {
	        		throw new MojoFailureException("Couldn't write output file: '" + options.outputFile.getAbsolutePath() + "'", e);
	        	}
	        }
	        else {
	        	throw new MojoFailureException("Failed to compile javascript.");
	        }
		}
		

	}
	
	/**
	 * 
	 * @param options
	 * @return
	 * @throws MojoFailureException
	 */
	private List<SourceFile> getExterns() throws MojoFailureException {
		Log log = getLog();
		List<SourceFile> externs = new ArrayList<SourceFile>();
		if (options.addDefaultExterns) {
			log.info("Adding default externs");
			try {
				externs.addAll(CommandLineRunner.getDefaultExterns());
			}
			catch (IOException e) {
				throw new MojoFailureException("Failed to get the default externs", e);
			}
		}
		
		externs.addAll(getSourceFiles(options.externs, "externs"));
		
		return externs;
	}
	
	private List<SourceFile> getSources() throws MojoFailureException {
		return getSourceFiles(options.sources, "sources");
	}

	
	private List<SourceFile> getSourceFiles(List<FileSet> filesets, String locationType) throws MojoFailureException {
		Log log = getLog();
		List<SourceFile> sources = new ArrayList<SourceFile>();
		
		if (filesets != null) {
			FileSetManager fsManager = new FileSetManager(log, true);
			for (FileSet fileset : filesets) {
				String[] paths;
				
				if (options.fileOrderMatters) {
					//if file order matters then we only use the direct included files in the exact order
					paths = fileset.getIncludesArray();
					String[] excludes = fileset.getExcludesArray();
					if (excludes != null && excludes.length > 0) {
						throw new MojoFailureException("In 'fileOrderMatters' mode you cannot use excludes");
					}
				}
				else {
					paths = fsManager.getIncludedFiles(fileset);
				}
				if (paths != null && paths.length > 0) {
					for (String path : paths) {
						File file = new File(fileset.getDirectory(), path);
						if (file.exists()) {
							log.info("Adding " + locationType + " path '" + file.getAbsolutePath() + "'");
							sources.add(SourceFile.fromFile(file));
						}
						else {
							log.warn("Ignoring " + locationType + " path '" + file.getAbsolutePath() + "' because it doesn't exist.");
						}
					}
				}
				else {
					log.warn("Fileset for directory '" + fileset.getDirectory() + "' doesn't contain any included files.");
				}
			}
		}
		return sources;
	}
	
	private List<JSModule> getModules() throws MojoFailureException {
		Map<String, JSModule> modulesByName = Maps.newLinkedHashMap();
		
		if (options.modules != null) {
			for (ModuleOptions mopts : options.modules) {
				if (modulesByName.containsKey(mopts.id)) {
					throw new MojoFailureException("Duplicate module name: " + mopts.id);
				}
				moduleOptionsByName.put(mopts.id, mopts);
				
				JSModule module = new JSModule(mopts.id);

				//add the source files to the module
				for (SourceFile inputFile : getSourceFiles(mopts.sources, "module " + mopts.id + " sources")) {
					module.add(inputFile);
				}
				
				//add the other dependencies if any 
				if (mopts.dependencies != null) {
					for (String depId : mopts.dependencies) {
						JSModule dep = modulesByName.get(depId);
						if (dep == null) {
							throw new MojoFailureException("Module dependency '" + depId + "' was not found");
						}
						module.addDependency(dep);
					}
				}
				
				modulesByName.put(mopts.id, module);
			}
		}
		
		return Lists.newArrayList(modulesByName.values());
	}
	
	/**
	 * @param level
	 *   The name of the {@link CompilationLevel} to use. Defaults to "ADVANCED_OPTIMIZATIONS".
	 * @return {@link CompilationLevel}
	 * @throws MojoFailureException
	 */
	private CompilationLevel getCompilationLevel(String level) throws MojoFailureException {
		if (level == null)
			level = CompilationLevel.ADVANCED_OPTIMIZATIONS.toString();
		
		CompilationLevel compilationLevel = null;
		try {
			compilationLevel = CompilationLevel.valueOf(level);
		}
		catch (IllegalArgumentException e) {
			throw new MojoFailureException("Invalid compilation level. Possible values: [" + Arrays.asList(CompilationLevel.values()) + "]" , e);
		}
		return compilationLevel;
	}
	
	/**
	 * @param warningLevel
	 *   The name of the {@link WarningLevel} to use. Defaults to "DEFAULT".
	 * @return {@link WarningLevel}
	 * @throws MojoFailureException 
	 */
	private WarningLevel getWarningLevel(String warningLevel) throws MojoFailureException {
		if (warningLevel == null)
			warningLevel = WarningLevel.DEFAULT.toString();
		
		WarningLevel level = null;
		try {
			level = WarningLevel.valueOf(warningLevel);
			
		}
		catch (IllegalArgumentException e) {
			throw new MojoFailureException("Invalid warning level. Possible values: [" + Arrays.asList(WarningLevel.values()) + "]", e);
		}
		return level;
	}
	

	/**
	 * Determines if the code files have changed since the last compile or not.
	 * @return
	 *   Returns true if the code is dirty and needs to be recompiled.
	 */
	private boolean isStale() {
		if (options.outputFile == null || !options.outputFile.exists()) {
			return true;
		}
		long lastCompileTime = options.outputFile.lastModified();
		return hasChangesSince(options.sources, lastCompileTime) || hasChangesSince(options.externs, lastCompileTime) || modulesHaveChangesSince(options.modules, lastCompileTime);
	}

	private boolean hasChangesSince(List<FileSet> filesets, long sinceTime) {
		for (FileSet fileset : filesets) {
			if (hasChangesSince(fileset, sinceTime)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean modulesHaveChangesSince(List<ModuleOptions> modules, long sinceTime) {
		for (ModuleOptions mopts : modules) {
			if (hasChangesSince(mopts.sources, sinceTime)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasChangesSince(FileSet fileset, long sinceTime) {
		FileSetManager fsManager = new FileSetManager();
		String[] paths = fsManager.getIncludedFiles(fileset);
		for (String path : paths) {
			File file = new File(path);
			if (file.lastModified() > sinceTime) {
				return true;
			}
		}
		return false;
	}
	
}
