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

package org.apache.xmlbeans.impl.binding.bts;


/**
 * Represents builtin bindings in JAX-RPC style
 */
public class JaxRpcBuiltinBindingLoader extends BuiltinBindingLoader
{

    // ========================================================================
    // Constants
    private static final BuiltinBindingLoader INSTANCE = new JaxRpcBuiltinBindingLoader();

    // ========================================================================
    // Factory

    public static BindingLoader getInstance()
    {
        return INSTANCE;
    }

    // ========================================================================
    // Private methods

    private JaxRpcBuiltinBindingLoader()
    {
        // todo: should each builtin binding type know about it's print/parse methods?

        addPojoXml("anySimpleType", "java.lang.String");

        addPojoTwoWay("anyType", "java.lang.Object");

        addPojoTwoWay("string", "java.lang.String");
        addPojoXml("normalizedString", "java.lang.String");
        addPojoXml("token", "java.lang.String");
        addPojoXml("language", "java.lang.String");
        addPojoXml("Name", "java.lang.String");
        addPojoXml("NCName", "java.lang.String");
        addPojoXml("NMTOKEN", "java.lang.String");
        addPojoXml("NMTOKENS", "java.lang.String[]");
        addPojoXml("ID", "java.lang.String");
        addPojoXml("IDREF", "java.lang.String");
        addPojoXml("IDREFS", "java.lang.String[]");
        addPojoXml("ENTITY", "java.lang.String");
        addPojoXml("ENTITIES", "java.lang.String[]");

        addPojoXml("duration", "java.lang.String");
        addPojoJava("duration", "org.apache.xmlbeans.GDuration");

        addPojoTwoWay("dateTime", "java.util.Calendar");
        addPojoJava("dateTime", "java.util.Date");
        addPojoXml("time", "java.util.Calendar");
        addPojoXml("date", "java.util.Calendar");
        addPojo("date", "java.util.Date");
        addPojoXml("gYearMonth", "java.lang.String");
        addPojo("gYearMonth", "java.util.Calendar");
        addPojoXml("gYear", "java.lang.String");
        addPojo("gYear", "java.util.Calendar");
        addPojo("gYear", "int");
        addPojoXml("gMonthDay", "java.lang.String");
        addPojo("gMonthDay", "java.util.Calendar");
        addPojoXml("gMonth", "java.lang.String");
        addPojo("gMonth", "java.util.Calendar");
        addPojo("gMonth", "int");
        addPojoXml("gDay", "java.lang.String");
        addPojo("gDay", "java.util.Calendar");
        addPojo("gDay", "int");

        addPojoTwoWay("boolean", "boolean");
        addPojoTwoWay("base64Binary", "byte[]");
        addPojoJava("base64Binary", "java.io.InputStream");
        addPojoXml("hexBinary", "byte[]");
        addPojo("hexBinary", "java.io.InputStream");
        addPojoTwoWay("float", "float");
        addPojoTwoWay("double", "double");
        addPojoTwoWay("decimal", "java.math.BigDecimal");
        addPojoTwoWay("integer", "java.math.BigInteger");
        addPojoTwoWay("long", "long");
        addPojoTwoWay("int", "int");
        addPojoTwoWay("short", "short");
        addPojoTwoWay("byte", "byte");
        addPojoXml("nonPositiveInteger", "java.math.BigInteger");
        addPojoXml("negativeInteger", "java.math.BigInteger");
        addPojoXml("nonNegativeInteger", "java.math.BigInteger");
        addPojoXml("positiveInteger", "java.math.BigInteger");
        addPojoXml("unsignedLong", "java.math.BigInteger");
        addPojoXml("unsignedInt", "long");
        addPojoXml("unsignedShort", "int");
        addPojoXml("unsignedByte", "short");
        addPojo("anyURI", "java.lang.String");
        addPojoTwoWay("anyURI", "java.net.URI");
        addPojoTwoWay("QName", "javax.xml.namespace.QName");
        addPojoXml("NOTATION", "java.lang.String");

        addPojoJava("float", Float.class.getName());
        addPojoJava("double", Double.class.getName());
        addPojoJava("long", Long.class.getName());
        addPojoJava("int", Integer.class.getName());
        addPojoJava("short", Short.class.getName());
        addPojoJava("byte", Byte.class.getName());
        addPojoJava("boolean", Boolean.class.getName());
        addPojoJava("unsignedInt", Long.class.getName());
        addPojoJava("unsignedShort", Integer.class.getName());
        addPojoJava("unsignedByte", Short.class.getName());

        //TODO: deal with char and java.lang.Character

        //=============== SOAPENC types =================
        //basically a copy of what is above but soap types are never the default
        //for java->schema, and soap types are never mapped to java primitives
        //since they are all nillable (per JAX-RPC 1.1), though one could
        //imagine using isSet methods to allow mapping to primitives.

        addSoapPojoXml("string", "java.lang.String");
        addSoapPojoXml("normalizedString", "java.lang.String");
        addSoapPojoXml("token", "java.lang.String");
        addSoapPojoXml("language", "java.lang.String");
        addSoapPojoXml("Name", "java.lang.String");
        addSoapPojoXml("NCName", "java.lang.String");
        addSoapPojoXml("NMTOKEN", "java.lang.String");
        addSoapPojoXml("NMTOKENS", "java.lang.String[]");
        addSoapPojoXml("ID", "java.lang.String");
        addSoapPojoXml("IDREF", "java.lang.String");
        addSoapPojoXml("IDREFS", "java.lang.String[]");
        addSoapPojoXml("ENTITY", "java.lang.String");
        addSoapPojoXml("ENTITIES", "java.lang.String[]");

        addSoapPojo("anyURI", "java.lang.String");
        addSoapPojoXml("anyURI", "java.net.URI");
        addSoapPojoXml("QName", "javax.xml.namespace.QName");
        addSoapPojoXml("NOTATION", "java.lang.String");

        addSoapPojoXml("duration", "java.lang.String");
        addSoapPojo("duration", "org.apache.xmlbeans.GDuration");

        addSoapPojoXml("dateTime", "java.util.Calendar");
        addSoapPojo("dateTime", "java.util.Date");
        addSoapPojoXml("time", "java.util.Calendar");
        addSoapPojoXml("date", "java.util.Calendar");
        addSoapPojo("date", "java.util.Date");
        addSoapPojoXml("gYearMonth", "java.lang.String");
        addSoapPojo("gYearMonth", "java.util.Calendar");
        addSoapPojoXml("gYear", "java.lang.String");
        addSoapPojo("gYear", "java.util.Calendar");
        addSoapPojoXml("gMonthDay", "java.lang.String");
        addSoapPojo("gMonthDay", "java.util.Calendar");
        addSoapPojoXml("gMonth", "java.lang.String");
        addSoapPojo("gMonth", "java.util.Calendar");
        addSoapPojoXml("gDay", "java.lang.String");
        addSoapPojo("gDay", "java.util.Calendar");

        addSoapPojoXml("base64Binary", "byte[]");
        addSoapPojo("base64Binary", "java.io.InputStream");
        addSoapPojoXml("hexBinary", "byte[]");
        addSoapPojo("hexBinary", "java.io.InputStream");

        addSoapPojoXml("decimal", "java.math.BigDecimal");
        addSoapPojoXml("integer", "java.math.BigInteger");

        addSoapPojoXml("nonPositiveInteger", "java.math.BigInteger");
        addSoapPojoXml("negativeInteger", "java.math.BigInteger");
        addSoapPojoXml("nonNegativeInteger", "java.math.BigInteger");
        addSoapPojoXml("positiveInteger", "java.math.BigInteger");
        addSoapPojoXml("unsignedLong", "java.math.BigInteger");
        addSoapPojoXml("unsignedInt", Long.class.getName());
        addSoapPojoXml("unsignedShort", Integer.class.getName());
        addSoapPojoXml("unsignedByte", Short.class.getName());

        addSoapPojoXml("float", Float.class.getName());
        addSoapPojoXml("double", Double.class.getName());
        addSoapPojoXml("long", Long.class.getName());
        addSoapPojoXml("int", Integer.class.getName());
        addSoapPojoXml("short", Short.class.getName());
        addSoapPojoXml("byte", Byte.class.getName());
        addSoapPojoXml("boolean", Boolean.class.getName());
        addSoapPojoXml("unsignedInt", Long.class.getName());
        addSoapPojoXml("unsignedShort", Integer.class.getName());
        addSoapPojoXml("unsignedByte", Short.class.getName());
        addSoapPojoXml("Array", "java.lang.Object[]");

    }

}
