package com.github.chrisregnier.closurecompiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
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
	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		
		if (options == null) {
			options = new PluginOptions();
		}
		
		Compiler.setLoggingLevel(Level.OFF);
		
		CompilerOptions compilerOptions = options.getCompilerOptions();
		if (options.skip) {
			return;
		}
		
		List<SourceFile> externs = getExterns();
		List<SourceFile> sources = getSources();
		
		if (options.forceRecompile || isStale()) {
			log.info("Compiling " + sources.size() + " file(s) with " + externs.size() + " extern(s)");
			
			CompilationLevel compilationLevel = getCompilationLevel(options.compilationLevel);
			compilationLevel.setOptionsForCompilationLevel(compilerOptions);
			if (options.debug) {
				compilationLevel.setDebugOptionsForCompilationLevel(compilerOptions);
			}

			WarningLevel warningLevel = getWarningLevel(options.warningLevel);
			warningLevel.setOptionsForWarningLevel(compilerOptions);
			
	        Compiler compiler = new Compiler();
	        MessageFormatter formatter = compilerOptions.errorFormat.toFormatter(compiler, false);
	        MavenErrorManager errorManager = new MavenErrorManager(formatter, log);
	        compiler.setErrorManager(errorManager);
	        
	        Result result = compiler.compile(externs, sources, compilerOptions);
		
            if (options.failOnWarnings && result.warnings.length > 0) {
            	throw new MojoFailureException("Failing on " + result.warnings.length + " warnings.");
            }
            if (options.failOnErrors && result.errors.length > 0) {
            	throw new MojoFailureException("Failing on " + result.errors.length + " errors.");
            }
	        
	        if (result.success) {
	        	try {
	        		options.outputFile.getParentFile().mkdirs();
        			Files.write(compiler.toSource(), options.outputFile, Charsets.UTF_8);
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
		
		FileSetManager fsManager = new FileSetManager(log, true);
		for (FileSet fileset : options.externs) {
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
						log.info("Adding extern path '" + file.getAbsolutePath() + "'");
						externs.add(SourceFile.fromFile(file));
					}
					else {
						log.warn("Ignoring extern path '" + file.getAbsolutePath() + "' because it doesn't exist.");
					}
				}
			}
			else {
				log.warn("Fileset for directory '" + fileset.getDirectory() + "' doesn't contain any included files.");
			}
		}
		
		return externs;
	}
	
	private List<SourceFile> getSources() throws MojoFailureException {
		Log log = getLog();
		List<SourceFile> sources = new ArrayList<SourceFile>();
		
		FileSetManager fsManager = new FileSetManager(log, true);
		for (FileSet fileset : options.sources) {
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
						log.info("Adding source path '" + file.getAbsolutePath() + "'");
						sources.add(SourceFile.fromFile(file));
					}
					else {
						log.warn("Ignoring source path '" + file.getAbsolutePath() + "' because it doesn't exist.");
					}
				}
			}
			else {
				log.warn("Fileset for directory '" + fileset.getDirectory() + "' doesn't contain any included files.");
			}
		}
		
		return sources;
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
		return hasChangesSince(options.sources, lastCompileTime) || hasChangesSince(options.externs, lastCompileTime);
	}

	private boolean hasChangesSince(List<FileSet> filesets, long sinceTime) {
		for (FileSet fileset : filesets) {
			if (hasChangesSince(fileset, sinceTime)) {
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
