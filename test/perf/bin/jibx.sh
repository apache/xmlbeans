#/bin/sh

java -Xmx64m -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/jibx-purchase-order.jar:$XMLBEANS_PERFROOT/3rdparty/jibx/jibx//lib/jibx-run.jar:$XMLBEANS_PERFROOT/3rdparty/jibx/jibx/lib/xpp3.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.jibx.$* 
