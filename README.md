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
```
mvn install:install-file -DgroupId=vuze \
    -DartifactId=vuze                   \
    -Dpackaging=jar                     \
    -Dversion=4700                      \
    -DgeneratePom=true                  \
    -Dfile=Vuze_4700.jar
```
3. To install also the sources:
```
mvn install:install-file -DgroupId=vuze \
    -DartifactId=vuze                   \
    -Dpackaging=jar                     \
    -Dversion=4700                      \
    -DgeneratePom=true                  \
    -Dfile=Vuze_4700_sources.jar        \
    -Dclassifier=sources
```

### Blogracy.bat and Blogracy.sh ###

Consequently, we can generate the `blogracy.bat` and `blogracy.sh` which are the
preferred way to run the system. These files should be regenerated if maven
upgrades its libraries versions or when something does not seem to work 
properly. Therefore, they should not be edited by hand. It is ok to do it
to try things, but remember, they will be re-generated. Please do not put
them under version control, either.

## Compile and Build  ##

Should be taken care of by ant. Classfiles are placed in `build/config/plugins/blogracy`.
We also place there relevant files (the `messages` package and the `plugin.properties`
file which is used by Vuze to load our plugin).

It is not necessary to create a jar file, the classfiles suffice. However, the plugin.properties
file must be there. Essentially the config directory must be something like

```
+- build/config
   +- plugins
      +- blogracy
         - plugin.properties
         - it/unipr/aotlab/blogracy/*.class
         - messages/Messages.properties
```

## Run ##
We do not use GUI or SWT. Consequently we run the Vuze console version
(see the `--ui=console` option in blogracy run scripts). We do not depend from
STW, as a consequence. You do not need to have those installed. Running such 
scripts from the GUI may not work. Use the Terminal, Luke!

It is however important to tell Vuze that the config directory it shall use is
`build/config` (because we deploy there our plugin). This is done with the
`-Dazureus.config.path=build/config` option of the VM.

The main class to run Vuze is `org.gudy.azureus2.ui.common.Main`. To have an idea,
the command which is inside the blogracy.sh file built for my system is, at the
present moment:

```
#!/bin/sh

java -Xmx128m -Dazureus.config.path=build/config        \
            -cp SOME_CLASSPATH                          \
            org.gudy.azureus2.ui.common.Main            \
                --ui=console
```

The former is not a specification. Is just an example.

It is still possible to have the thing run with a GUI. We do not have time
to make it automated, though. Moreover, the plugin is meant to be used headless
through a it.unipr.aotlab.it.unipr.aotlab.blogracy.web-browser, and is consequently not necessary.

Some information about the old GUI version may be found in old/README.
It is utterly incomplete.

## IDE ##
Most IDEs should play nice with Ant. Just pay attention to the notion of 
Working Directory. Please do not commit your IDE specific stuff.


## Authors ##

The original core was written by Alan Nonnato as his master's theses project.

The code of the present versions is mostly written by Enrico Franchi and Michele Tomaiuolo.

The project is currently maintained by Enrico Franchi and Michele Tomaiuolo.

## Contributors ##
* Enrico Franchi (efranchi@ce.unipr.it) [ core ]
* Michele Tomaiuolo (tomamic@ce.unipr.it) [ core ]
* Alan Nonnato () [ old core ]
...
