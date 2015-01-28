#!/bin/bash
cd blogracy-vuze
wget http://downloads.sourceforge.net/project/azureus/vuze/Vuze_4702/Vuze_4702.jar?ts=1336079441 -O Vuze_4702.jar
mvn install:install-file -DgroupId=vuze -DartifactId=vuze -Dpackaging=jar -Dversion=4702 -DgeneratePom=true -Dfile=Vuze_4702.jar
mvn clean install compile package
mvn eclipse:eclipse
mvn exec:exec &
cd ../blogracy-web
mvn clean install compile
mvn eclipse:eclipse
mvn exec:exec &

