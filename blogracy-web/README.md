# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.
Essentially Blogracy is composed by two main modules:

1. a Vuze plugin
2. a Web application

This information regards the second module, the Web application.

### Download and Install CP-ABE ###

Blogracy uses the CPABE library, so it's necessary to download and install it to the Maven repository.

```
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/cpabe.jar

mvn install:install-file -DgroupId=cpabe -DartifactId=cpabe -Dpackaging=jar -Dversion=1.0 -DgeneratePom=true -Dfile=cpabe.jar
```

CPABE requiers two additional components: JPBC-API and JPBC-PLAF.
To download and install, run the following commands, in 'blogracy-web' folder:

```
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/jpbc-api-1.2.1.jar
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/jpbc-plaf-1.2.1.jar

mvn install:install-file -DgroupId=it.unisa.dia.gas.jpbc -DartifactId=jpbc-api -Dpackaging=jar -Dversion=1.2.1 -DgeneratePom=true -Dfile=jpbc-api-1.2.1.jar
mvn install:install-file -DgroupId=it.unisa.dia.gas.plaf.jpbc -DartifactId=jpbc-plaf -Dpackaging=jar -Dversion=1.2.1 -DgeneratePom=true -Dfile=jpbc-plaf-1.2.1.jar
```

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
mvn exec:exec
```

Alternatively, you can deploy the webapp into any Java web container.

Blogracy web interface is available at: http://localhost:8181/

### Executing Blogracy inside the IDE ###

Use the information above to create the appropriate configuration in the IDE.
