#/bin/sh

java -Xbootclasspath/p:$XMLBEANS_PERFROOT/3rdparty/xerces/xerces-2_6_2/xml-apis.jar:$XMLBEANS_PERFROOT/3rdparty/xerces/xerces-2_6_2/xercesImpl.jar -Xmx64m -classpath $XMLBEANS_PERFROOT/build -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.xerces.$* 
