@echo off

@rem DRT
@rem
@rem Invokes Random test

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar
set cp=%cp%;%XMLBEANS_HOME%\build\private\lib\random.jar
set cp=%cp%;%XMLBEANS_HOME%\build\private\lib\easypo.jar

rem java -ea -Dtreeasserts=true  -Dxbean.rootdir=%XMLBEANS_HOME% -classpath %cp% Random -noquery %*
    java -ea -Dtreeasserts=false -Dxbean.rootdir=%XMLBEANS_HOME% -classpath %cp% Random -noquery %*
