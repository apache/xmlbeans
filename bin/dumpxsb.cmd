@rem XSB file dumper
@rem
@rem Prints the contents of an xsb file in human-readable form
@echo off

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.XsbDumper %*

:done
