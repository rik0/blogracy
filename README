# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.

## Install ##

The installation process is not yet streamlined. Essentially Blogracy runs as a Vuze
plugin. We do not distribute Jars anymore.

The build system uses Ant and Maven. If you have a working ant and maven
installation things should be rather smooth. We basically use maven just for
dependency management and ant for everything else.


### Maven Ant Tasks ###
In order to have ant and maven work together, we use Maven Ant Tasks. Documentation
is not extremely exhaustive. In principle you should not have to download Maven
Ant Tasks: we have an ant target (`mvn-ant-tasks-download`)
which downloads it automagically. Our system works with version 2.1.3.

We have been reported cases where the download process did not succeed. A 
maven-ant-tasks-2.1.3.jar is downloaded in the lib directory, but the file is
broken. This is easy to veryfy with 

```unzip -t lib/maven-ant-tasks-2.1.3.jar```

If the file is broken, an error will occur. Manually downloading the file and
placing it in the lib directory fixes the issue.

### Maven ###
Maven should just work out of the box (if installed). Essentially we use maven
ant tasks to have ant call the maven tasks necessary to download the relevant
libraries (at the present moment `velocity-engine`, `commons-cli`, `junit`, `log4j`).
The actual set of dependencies may change without updating this document. 
Refer to the `pom.xml` file and the `build.xml` ant script for information.

Maven does not put stuff in the lib directory, but places them in its own
local repository. See maven docs for more information. However, in Ant we
have the proper value of the classpath to use which takes this into account.

### Blogracy.bat and Blogracy.sh ###

Consequently, we can generate the `blogracy.bat` and `blogracy.sh` which are the
preferred way to run the system. These files should be regenerated if maven
upgrades its libraries versions or when something does not seem to work 
properly. Therefore, they should not be edited by hand. It is ok to do it
to try things, but remember, they will be re-generated. Please do not put
them under version control, either.

### Vuze ###

We use ant to download Vuze. We use the `Vuze_4700.jar` from the devs page.
Again, should it be corrupted, feel free to manually download that and
the sources package and place it in the lib directory. 

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
\#!/bin/sh

java -Xmx128m -Dazureus.config.path=build/config        \
            -cp SOME_CLASSPATH                          \
            org.gudy.azureus2.ui.common.Main            \
                --ui=console
```

The former is not a specification. Is just an example.

It is still possible to have the thing run with a GUI. We do not have time
to make it automated, though. Moreover, the plugin is meant to be used headless
through a web-browser, and is consequently not necessary.

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
Enrico Franchi (efranchi@ce.unipr.it) [ core ]
Michele Tomaiuolo (tomamic@ce.unipr.it) [ core ]
Alan Nonnato () [ old core ]
...
