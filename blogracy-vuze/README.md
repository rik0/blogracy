# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.

## Install ##

The installation process is not yet streamlined. Essentially Blogracy is composed
by two main modules:

1. a Vuze plugin
2. a web application

The build system uses Maven. If you have a working  maven
installation things should be rather smooth.

### Getting Vuze ###

Vuze is not packaged for Maven right now. This may change in the future.
Right now, it is necessary to:

1. Download Vuze jar (and sources) from main Vuze site: http://dev.vuze.com/
   A link which is working at the present time is:
   http://sourceforge.net/projects/azureus/files/vuze/Vuze_4702/Vuze_4702.jar/download
2. In the same directory where the files have been downloaded, execute
   the "mvn-install-vuze.sh" script.

### Getting Blogracy ###

The preferred way to obtain blogracy sources is with git. Blogracy main
repository is hosted at git://github.com/rik0/blogracy.git. However,
if you intend to contribute to blogracy, we strongly encourage you to make a
fork and then pull request the modifications.

#### Fork ####

1. Create a fork through GitHub repository
2. The fork repository will be at git@github.com:\<USERNAME\>/blogracy.git
   where \<USERNAME\> is your actual username
3. Clone the new git fork:

```
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

in the project folder. This compiles all the Java sources.
The classes, the resource files (those in src/resources) and the
configuration files (in src/config) are copied in the "target" folder.

The following command, instead, creates a JAR archive in the target folder,
including Vuze classes and all other dependencies.

```
mvn package
```

### Maven and the IDE ###

Maven pom files are simply a description of a project. Such description can
be easily interpreted by an IDE and can be used to build the project.

Suppose that Blogracy sources have been cloned to "~/src/blogracy/".
The first step, in the "~/src/blogracy/blogracy-vuze/" folder, is just:

```
mvn eclipse:eclipse
```

that updates the eclipse project with stuff coming from maven.
Repeat this step when something changes in the pom file.

Then, start Eclipse using blogracy as workspace:

```
eclipse -data ~/src/blogracy/
```

In Eclipse, choose "Import Existing Maven Project" from the "File" menu, 
and indicate the "~/src/blogracy/blogracy-vuze/" folder.

Feel free to read more documentation regarding integrating Eclipse with
Maven. There is also an excellent plugin that can be installed in Eclipse.
We however found that this first project setup phase is easier this way.

With IntelliJ things mostly work out of the box.

## Run ##

Either run:

```
java -jar target/blogracy-vuze-1.0-SNAPSHOT-jar-with-dependencies.jar
```

or:

```
mvn exec:exec
```

We do not use GUI or SWT. Consequently we run the Vuze console version.
We do not depend from STW, as a consequence.

Alternatively, you can put the blogracy-vuze...jar file into the plugins folder
of Vuze, and start Vuze normally. This way, you may also need to start ActiveMQ
by hand, separately.

### Executing Blogracy inside the IDE ###

Use the information above to create the appropriate configuration in the IDE.
The Blogracy class implements a static "main" method for starting ActiveMQ,
Vuze and Blogracy-Vuze plugin.

## Authors ##

The code of the present versions is mostly written by Enrico Franchi and Michele Tomaiuolo.

The project is currently maintained by Enrico Franchi and Michele Tomaiuolo.

## Contributors ##

* Enrico Franchi (efranchi@ce.unipr.it) [ core ]
* Michele Tomaiuolo (tomamic@ce.unipr.it) [ core ]
* Alan Nonnato () [ old core ]
