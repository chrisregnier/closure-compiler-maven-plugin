closure-compiler-maven-plugin
=============================

Maven plugin to run the closure compiler on your javascript code base

## Current options:

### forceRecompile (boolean)
Forces the code to be recompiled even if things aren't dirty.
true or false. can also use property 'closure.forceRecompile'

### compilationLevel (string)
"ADVANCED\_OPTIMIZATIONS", "SIMPLE\_OPTIMIZATIONS", "WHITESPACE\_ONLY"

### warningLevel (string)
"DEFAULT", "VERBOSE", "QUIET"

### prettyPrint (boolean)
true or false

### languageIn (string)
"ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5\_STRICT", "ECMASCRIPT6", "ECMASCRIPT6\_STRICT", "NO\_TRANSPILE"

### languageOut (string)
"ECMASCRIPT3", "ECMASCRIPT5", "ECMASCRIPT5\_STRICT", "ECMASCRIPT6", "ECMASCRIPT6\_STRICT", "NO\_TRANSPILE"

### outputFile (File)
The output file to write everything to.
defaults to: `"${project.build.directory}/$project.artifactId}-${project.version}-min.js/`

### addDefaultExterns (boolean)
defaults to true

### failOnWarnings (boolean)
defaults to false

### failOnErrors (boolean)
defaults to true

### externs (List<FileSet>)
a bunch of files that should be treated as externs

### sources (List<FileSet>)
a bunch of files that should be treated as the source

