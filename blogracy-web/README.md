# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.
Essentially Blogracy is composed by two main modules:

1. a Vuze plugin
2. a web application

This information regards the second module, the Web application.

### Compile ###

To compile this sub-project, it is sufficient to issue

```
mvn compile
```

in the sub-project folder. This compiles all the Java sources.
The classes, the resource files (those in src/resources) and the
configuration files (in src/config) are copied in the "target" folder.

### Maven and the IDE ###

Maven pom files are simply a description of a project. Such description can
be easily interpreted by an IDE and can be used to build the project.

Suppose that Blogracy sources have been cloned to "~/src/blogracy/".
The first step, in the "~/src/blogracy/blogracy-web/" folder, is just:

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
and indicate the "~/src/blogracy/blogracy-web/" folder.

Feel free to read more documentation regarding integrating Eclipse with
Maven. There is also an excellent plugin that can be installed in Eclipse.
We however found that this first project setup phase is easier this way.

With IntelliJ things mostly work out of the box.

## Run ##

Issue the following command:

```
mvn jetty:run
```

Alternatively, you can deploy the webapp into any Java web container.

### Executing Blogracy inside the IDE ###

Use the information above to create the appropriate configuration in the IDE.
