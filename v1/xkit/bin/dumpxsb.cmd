@rem XSB file dumper
@rem
@rem Prints the contents of an xsb file in human-readable form

@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% com.bea.xbean.tool.XsbDumper %*

:done
