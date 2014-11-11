package com.github.chrisregnier.closurecompiler;

import org.apache.maven.plugin.logging.Log;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.MessageFormatter;

/**
 * Basic closure error manager to push logs through maven
 * 
 * @author cregnier
 *
 */
public class MavenErrorManager extends BasicErrorManager {
	private final MessageFormatter formatter;
	private final Log log;
	
	public MavenErrorManager(MessageFormatter formatter, Log log) {
		this.formatter = formatter;
		this.log = log;
	}

	@Override
	public void println(CheckLevel level, JSError error) {
		if (level == CheckLevel.OFF)
			return;
		if (level == CheckLevel.WARNING) {
			log.warn(error.format(level, formatter));
		}
		else {
			log.error(error.format(level, formatter));
		}
	}

	@Override
	protected void printSummary() {
		if (getErrorCount() + getWarningCount() > 0) {
			log.warn("Found " + getErrorCount() + " error(s) and " + getWarningCount() + " warning(s).");
		}
		else {
			log.info("Found " + getErrorCount() + " error(s) and " + getWarningCount() + " warning(s).");
		}
	}

}
