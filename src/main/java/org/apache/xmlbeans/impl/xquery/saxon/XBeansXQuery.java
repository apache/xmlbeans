/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.xquery.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.*;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlTokenSource;
import org.apache.xmlbeans.impl.store.QueryDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class XBeansXQuery
    implements QueryDelegate.QueryInterface {
    private XQueryExpression xquery;
    private String contextVar;
    private Configuration config;

    /**
     * Construct given an XQuery expression string.
     *
     * @param query      The XQuery expression
     * @param contextVar The name of the context variable
     * @param boundary   The offset of the end of the prolog
     */
    public XBeansXQuery(final String query, String contextVar, Integer boundary, XmlOptions xmlOptions) {
        config = new Configuration();
        StaticQueryContext sc = config.newStaticQueryContext();
        @SuppressWarnings("unchecked")
        Map<String,String> nsMap = (Map<String,String>)xmlOptions.get(XmlOptions.LOAD_ADDITIONAL_NAMESPACES);
        if (nsMap != null) {
            nsMap.forEach(sc::declareNamespace);
        }
        this.contextVar = contextVar;
        //Saxon requires external variables at the end of the prolog...
        try {
            xquery = sc.compileQuery(
                query.substring(0, boundary) + " declare variable $" + contextVar + " external;" + query.substring(boundary)
            );
        } catch (TransformerException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public List execQuery(Object node, Map variableBindings) {
        try {
            Node contextNode = (Node) node;

            Document dom = (contextNode.getNodeType() == Node.DOCUMENT_NODE)
                ? (Document) contextNode : contextNode.getOwnerDocument();

            DocumentWrapper docWrapper = new DocumentWrapper(dom, null, config);
            NodeInfo root =  docWrapper.wrap(contextNode);

            DynamicQueryContext dc = new DynamicQueryContext(config);
            dc.setContextItem(root);
            dc.setParameter(new StructuredQName("", null, contextVar), root);
            // Set the other variables
            if (variableBindings != null) {
                for (Map.Entry<String, Object> me : ((Map<String,Object>)variableBindings).entrySet()) {
                    StructuredQName key = new StructuredQName("", null, me.getKey());
                    Object value = me.getValue();
                    if (value instanceof XmlTokenSource) {
                        Node paramObject = ((XmlTokenSource) value).getDomNode();
                        dc.setParameter(key, docWrapper.wrap(paramObject));
                    } else {
                        try {
                            dc.setParameter(key, objectToItem(value, config));
                        } catch (XPathException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            List<Object> saxonNodes = xquery.evaluate(dc);
            for (ListIterator<Object> it = saxonNodes.listIterator(); it.hasNext(); ) {
                Object o = it.next();
                if (o instanceof NodeInfo) {
                    Node n = NodeOverNodeInfo.wrap((NodeInfo) o);
                    it.set(n);
                }
            }
            return saxonNodes;
        } catch (TransformerException e) {
            throw new RuntimeException("Error binding " + contextVar, e);
        }
    }


    private static Item objectToItem(Object value, Configuration config) throws XPathException, net.sf.saxon.trans.XPathException {
        if (value == null) {
            return null;
        }

        // convert to switch..
        if (value instanceof Boolean) {
            return BooleanValue.get((Boolean) value);
        } else if (value instanceof byte[]) {
            return new HexBinaryValue((byte[])value);
        } else if (value instanceof Byte) {
            return new Int64Value((Byte) value, BuiltInAtomicType.BYTE, false);
        } else if (value instanceof Float) {
            return new FloatValue((Float) value);
        } else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        } else if (value instanceof Integer) {
            return new Int64Value((Integer) value, BuiltInAtomicType.INT, false);
        } else if (value instanceof Long) {
            return new Int64Value((Long) value, BuiltInAtomicType.LONG, false);
        } else if (value instanceof Short) {
            return new Int64Value((Short) value, BuiltInAtomicType.SHORT, false);
        } else if (value instanceof String) {
            return new StringValue((String)value);
        } else if (value instanceof BigDecimal) {
            return new BigDecimalValue((BigDecimal)value);
        } else if (value instanceof BigInteger) {
            return new BigIntegerValue((BigInteger)value);
        } else if (value instanceof SaxonDuration) {
            return ((SaxonDuration)value).getDurationValue();
        } else if (value instanceof Duration) {
            // this is simpler and safer (but perhaps slower) than extracting all the components
            //return DurationValue.makeDuration(value.toString()).asAtomic();
            Duration dv = (Duration) value;
            return new DurationValue(dv.getSign() >= 0, dv.getYears(), dv.getMonths(), dv.getDays(),
                dv.getHours(), dv.getMinutes(), dv.getSeconds(), 0); // take correct millis..
        } else if (value instanceof SaxonXMLGregorianCalendar) {
            return ((SaxonXMLGregorianCalendar)value).toCalendarValue();
        } else if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar g = (XMLGregorianCalendar)value;
            QName gtype = g.getXMLSchemaType();
            if (gtype.equals(DatatypeConstants.DATETIME)) {
                return DateTimeValue.makeDateTimeValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.DATE)) {
                return DateValue.makeDateValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.TIME)) {
                return TimeValue.makeTimeValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEAR)) {
                return GYearValue.makeGYearValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEARMONTH)) {
                return GYearMonthValue.makeGYearMonthValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTH)) {
                // a workaround for W3C schema bug
                String val = value.toString();
                if (val.endsWith("--")) {
                    val = val.substring(0, val.length() - 2);
                }
                return GMonthValue.makeGMonthValue(val).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTHDAY)) {
                return GMonthDayValue.makeGMonthDayValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GDAY)) {
                return GDayValue.makeGDayValue(value.toString()).asAtomic();
            } else {
                throw new AssertionError("Unknown Gregorian date type");
            }
        } else if (value instanceof QName) {
            QName q = (QName)value;
            return new QNameValue(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()); //BuiltInAtomicType.QNAME, null);
        } else if (value instanceof URI) {
            return new AnyURIValue(value.toString());
        } else if (value instanceof Map) {
            HashTrieMap htm = new HashTrieMap();
            for (Map.Entry<?,?> me : ((Map<?,?>)value).entrySet()) {
                htm.initialPut(
                    (AtomicValue)objectToItem(me.getKey(), config),
                    objectToItem(me.getValue(), config));
            }
            return htm;
        } else {
            return new ObjectValue(value);
        }
    }}
