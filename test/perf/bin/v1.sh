V1_LIB=$XMLBEANS_PERFROOT/3rdparty/v1/xmlbeans-1.0.3/lib
export V1_LIB

java -Xmx64m -classpath $XMLBEANS_PERFROOT/build:$XMLBEANS_PERFROOT/schema_build/v1-purchase-order.jar:$XMLBEANS_PERFROOT/schema_build/v1-primitives.jar:$XMLBEANS_PERFROOT/schema_build/v1-non-primitives.jar:$V1_LIB/xbean.jar:$XMLBEANS_HOME/external/lib/piccolo_apache_dist_20040629_v2.jar -DPERF_ROOT=$XMLBEANS_PERFROOT org.apache.xmlbeans.test.performance.v1.$* 
