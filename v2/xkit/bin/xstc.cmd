@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% xml.apache.org.tool.XSTCTester %*
