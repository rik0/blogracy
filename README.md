# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.

## Install ##

The installation process is not yet streamlined. Essentially Blogracy runs as a Vuze
plugin. We do not distribute Jars anymore.

The build system uses Maven. If you have a working  maven
installation things should be rather smooth.

### Getting Vuze ###

Vuze is not packaged for Maven right now. This may change in the future.
Right now, it is necessary to:

1. Download Vuze jar (and sources) from main Vuze site.
   A link which is working at the present time is:
   http://sourceforge.net/projects/azureus/files/vuze/Vuze_4700.jar
   for the classes and:
   http://sourceforge.net/projects/azureus/files/vuze/Vuze_4700_sources.jar
   for the sources.
2. In the same directory where the files have been downloaded, execute
   the following command (notice that it should be placed on just one line).
   The trailing \ are the standard way unix shells allow a command to
   be split on more lines (easier to cut and paste)

```bash
mvn install:install-file -DgroupId=vuze \
    -DartifactId=vuze                   \
    -Dpackaging=jar                     \
    -Dversion=4700                      \
    -DgeneratePom=true                  \
    -Dfile=Vuze_4700.jar
```

3. To install also the sources:

```bash
mvn install:install-file -DgroupId=vuze \
    -DartifactId=vuze                   \
    -Dpackaging=jar                     \
    -Dversion=4700                      \
    -DgeneratePom=true                  \
    -Dfile=Vuze_4700_sources.jar        \
    -Dclassifier=sources
```

If you are using windows copy and paste the following commands instead:

```bat
mvn install:install-file -DgroupId=vuze ^
    -DartifactId=vuze                   ^
    -Dpackaging=jar                     ^
    -Dversion=4700                      ^
    -DgeneratePom=true                  ^
    -Dfile=Vuze_4700.jar
```

and

```bat
mvn install:install-file -DgroupId=vuze ^
    -DartifactId=vuze                   ^
    -Dpackaging=jar                     ^
    -Dversion=4700                      ^
    -DgeneratePom=true                  ^
    -Dfile=Vuze_4700_sources.jar        ^
    -Dclassifier=sources
```

### Getting Blogracy ###

The preferred way to obtain blogracy sources is with git. Blogracy main
repository is hosted at git://github.com/rik0/blogracy.git. However,
if you intend to contribute to blogracy, we strongly encourage you to make a
fork and then pull request the modifications.

#### Fork ####

1. Create a fork through GitHub repository
2. The fork repository will be at git@github.com:<USERNAME>/blogracy.git
   where <USERNAME> is your actual username
3. ```
git clone git@github.com:<USERNAME>/blogracy.git
```

If you are using Eclipse, issue the command directly in the workspace or
move the sources afterwards.

#### Read-Only ####

To get the sources "read only" simply:
```
git://github.com/rik0/blogracy.git
```

### Compile ###

To compile the project, it is sufficient to issue
```
mvn compile
```
in the project directory. This compiles all the Java sources and moves them
to the build directory (by default build/config/plugins/blogracy).
The classes, the resource files (those in src/resources) and the
configuration files (in src/config) are copied in the build directory.

That is a subdirectory of Vuze config directory (build/config). Vuze build
directory can be specified setting the property azureus.config.path. It can
be done from the command line with -Dazureus.config.path=build/config.

For blogracy to work the the plugin.properties file must be in
build/config/plugins/blogracy. Essentially the config directory must be
something like:

```
+- build/config
   +- plugins
      +- blogracy
         - plugin.properties
         - it/unipr/aotlab/blogracy/*.class
         - messages/Messages.properties
         - static
         - templates
         - blogracyUrl.config
         - blogracyPaths.properties
         - ...
```

### Maven and the IDE ###

Maven pom files are simply a description of a project. Such description can
be easily interpreted by an IDE and can be used to build the project.

Suppose that Blogracy sources have been cloned to ~/src/blogracy.
Create a new Eclipse project in the same directory from scratch. The
project is not working at this point: two more steps are needed.

Close Eclipse and then move in the project directory to issue:
```
mvn -Declipse.workspace=/<PATH OF YOUR WORKSPACE> eclipse:configure-workspace
```
This sets correctly the classpath of the workspace so that the maven
repository is recognized.

The last step is just:
```
mvn eclipse:eclipse
```

that updates the eclipse project with stuff coming from maven. Repeat this
last step when something changes in the pom file.

Feel free to read more documentation regarding integrating Eclipse with
Maven. There is also an excellent plugin that can be installed in Eclipse.
We however found that this first project setup phase is easier
this way.

With IntelliJ things mostly work out of the box.

## Run ##

We do not use GUI or SWT. Consequently we run the Vuze console version
(`--ui=console` option). We do not depend from
STW, as a consequence. You do not need to have those installed. Running such
scripts from the GUI may not work. Use the Terminal, Luke!

It is however important to tell Vuze that the config directory it shall use is
`build/config` (because we deploy there our plugin). This is done with the
`-Dazureus.config.path=build/config` option of the VM.

The main class to run Vuze is `org.gudy.azureus2.ui.common.Main`.

### Executing Blogracy inside the IDE ###

Use the information above to create the appropriate configuration in the IDE.

### Executables ###
Run from the terminal:
```
mvn package appassembler:assemble
```

This will create a bin directory where a shell script and a bat file exist.
```
sh bin/blogracy
```
works on Unix systems (cygwin included). The bat file works for windows.
Do not absolutely put those scripts under version control. 

Sometimes issues with running the project within an IDE arise. In this case, please build these scripts and try running them.

## Authors ##

The code of the present versions is mostly written by Enrico Franchi and Michele Tomaiuolo.

The project is currently maintained by Enrico Franchi and Michele Tomaiuolo.

## Contributors ##
* Enrico Franchi (efranchi@ce.unipr.it) [ core ]
* Michele Tomaiuolo (tomamic@ce.unipr.it) [ core ]
* Alan Nonnato () [ old core ]
...
