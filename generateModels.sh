#!/bin/sh -e

mkdir -p bin/classes

echo javac -cp daolibs/freemarker.jar:daolibs/greenDAO-generator.jar -d bin/classes -sourcepath src daolibs/DAOGenerator.java
javac -cp daolibs/freemarker.jar:daolibs/greenDAO-generator.jar -d bin/classes -sourcepath src daolibs/DAOGenerator.java

echo java -cp bin/classes:daolibs/freemarker.jar:daolibs/greenDAO-generator.jar com.stephenr.gekkobooks.dao.generator.DAOGenerator
java -cp bin/classes:daolibs/freemarker.jar:daolibs/greenDAO-generator.jar com.stephenr.gekkobooks.dao.generator.DAOGenerator