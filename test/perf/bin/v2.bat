@echo off
REM SUPPORTED PARAMS
REM TODO: Make command line arg parsing cleaner
set MEM=64
set FLAVOR=
set FILENAME=

REM SCAN FIRST PARAM
if not "%2"=="" goto %2
goto default
goto end
:memory
set MEM=%3
goto end
:flavor
set FLAVOR=%3
goto end
:filename
set FILENAME=%3
goto end
:default
goto end
:end

REM SCAN SECOND PARAM
if not "%4"=="" goto %4
goto default
goto end
:memory
set MEM=%5
goto end
:flavor
set FLAVOR=%5
goto end
:filename
set FILENAME=%5
goto end
:default
goto end
:end

REM SCAN THIRD PARAM
if not "%6"=="" goto %6
goto default
goto end
:memory
set MEM=%7
goto end
:flavor
set FLAVOR=%7
goto end
:filename
set FILENAME=%7
goto end
:default
goto end
:end

echo on
java -Xmx%MEM%m -classpath %XMLBEANS_PERFROOT%\build;%XMLBEANS_PERFROOT%\schema_build\v2-purchase-order.jar;%XMLBEANS_PERFROOT%\schema_build\v2-primitives.jar;%XMLBEANS_PERFROOT%\schema_build\v2-non-primitives.jar;%XMLBEANS_HOME%\build\lib\xbean.jar;%XMLBEANS_HOME%\build\lib\jsr173_api.jar;%XMLBEANS_HOME%\external\lib\piccolo_apache_dist_20040629_v2.jar -DPERF_ROOT=%XMLBEANS_PERFROOT% org.apache.xmlbeans.test.performance.v2.%1 %FLAVOR% %FILENAME%
