#!/bin/bash
cd blogracy-vuze
wget http://downloads.sourceforge.net/project/azureus/vuze/Vuze_4702/Vuze_4702.jar?ts=1336079441 -O Vuze_4702.jar
mvn install:install-file -DgroupId=vuze -DartifactId=vuze -Dpackaging=jar -Dversion=4702 -DgeneratePom=true -Dfile=Vuze_4702.jar
mvn clean install compile package
mvn eclipse:eclipse
mvn exec:exec &
cd ../blogracy-web
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/cpabe.jar
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/jpbc-api-1.2.1.jar
wget http://shibboleth.googlecode.com/svn/trunk/lib/WebRoot/WEB-INF/lib/ABE/jpbc-plaf-1.2.1.jar
mvn install:install-file -DgroupId=cpabe -DartifactId=cpabe -Dpackaging=jar -Dversion=1.0 -DgeneratePom=true -Dfile=cpabe.jar
mvn install:install-file -DgroupId=it.unisa.dia.gas.jpbc -DartifactId=jpbc-api -Dpackaging=jar -Dversion=1.2.1 -DgeneratePom=true -Dfile=jpbc-api-1.2.1.jar
mvn install:install-file -DgroupId=it.unisa.dia.gas.plaf.jpbc -DartifactId=jpbc-plaf -Dpackaging=jar -Dversion=1.2.1 -DgeneratePom=true -Dfile=jpbc-plaf-1.2.1.jar
mvn clean install compile
mvn eclipse:eclipse
mvn exec:exec &

