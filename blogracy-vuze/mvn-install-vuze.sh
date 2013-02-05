#!/bin/bash
mvn install:install-file -DgroupId=vuze -DartifactId=azureus -Dpackaging=jar -Dversion=2 -DgeneratePom=true -Dfile=Azureus2.jar
