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

package org.apache.xmlbeans.impl.jaxb.compiler;

import java.util.*;
import java.io.*;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;

public final class JaxbCodePrinter
{
    Writer _writer;
    int _indent;
    static final String LINE_SEPARATOR = System.getProperty("line.separator") == null ? "\n" : System.getProperty("line.separator");

    static final int MAX_INDENT = 40;
    static final String MAX_SPACES = "                                        ";
    static final int INDENT_INCREMENT = 4;

    public JaxbCodePrinter(Writer writer)
    {
        _writer = writer;
        _indent = 0;
    }

    void indent()
    {
        _indent += INDENT_INCREMENT;
    }

    void outdent()
    {
        _indent -= INDENT_INCREMENT;
    }

    void startBlock() throws IOException
    {
        emit("{");
        indent();
    }
    void endBlock() throws IOException
    {
        outdent();
        emit("}");
    }

    void emit(String s) throws IOException
    {
        int indent = _indent;
        if (indent > MAX_INDENT / 2)
            indent = MAX_INDENT / 4 + indent / 2;
        if (indent > MAX_INDENT)
            indent = MAX_INDENT;
        _writer.write(MAX_SPACES.substring(0, indent));
        _writer.write(s);
        _writer.write(LINE_SEPARATOR);
    }

    public static void printPackageProperties(Writer w, PackageInfo pi) throws IOException
    {
        JaxbCodePrinter p = new JaxbCodePrinter(w);
        p.printPackageProperties(pi);
    }

    public static void printJavaInterface(Writer w, SchemaComponent sc, ComponentInfo ci)
        throws IOException
    {
        JaxbCodePrinter p = new JaxbCodePrinter(w);

        switch (sc.getComponentType())
        {
            case SchemaComponent.ELEMENT:
                p.printElement((SchemaGlobalElement)sc, (ElementInfo)ci);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    public static void printJavaImpl(Writer w, SchemaComponent sc, ComponentInfo ci)
        throws IOException
    {
        JaxbCodePrinter p = new JaxbCodePrinter(w);

        switch (sc.getComponentType())
        {
            case SchemaComponent.ELEMENT:
                p.printElementImpl((SchemaGlobalElement)sc, (ElementInfo)ci);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    void printPackageProperties(PackageInfo pi) throws IOException
    {
        emit("#" + new Date());
        emit("javax.xml.bind.context.factory=org.apache.xmlbeans.impl.jaxb.runtime.ContextImpl");
        emit("org.apache.xmlbeans.impl.jaxb.TypeSystemHolder=" + pi.getIndexClassName());
    }

    void printElement(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        printTopComment(elt, ei, true);
        printPackage(elt, ei, true);
        printElementBody(elt, ei);
    }

    void printElementImpl(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        printTopComment(elt, ei, false);
        printPackage(elt, ei, false);
        printElementImplBody(elt, ei);
    }

    void printTopComment(SchemaComponent sc, ComponentInfo ci, boolean intf)
        throws IOException
    {
        emit("/*");
        emit(" * XML Type:  " + sc.getName().getLocalPart());
        emit(" * Namespace: " + sc.getName().getNamespaceURI());
        emit(" * Java type: " + ( intf ? ci.getFullJavaIntfName() : ci.getFullJavaImplName()));
        emit(" *");
        emit(" * Automatically generated - do not modify.");
        emit(" */");
        emit("");
    }

    void printPackage(SchemaComponent sc, ComponentInfo ci, boolean intf)
        throws IOException
    {
        String fqjn;
        if (intf)
            fqjn = ci.getFullJavaIntfName();
        else
            fqjn = ci.getFullJavaImplName();

        int lastdot = fqjn.lastIndexOf('.');
        assert lastdot >= 0;

        String pkg = fqjn.substring(0, lastdot);
        emit("package " + pkg + ";");
        emit("");
    }

    void printElementBody(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        printInnerElementJavaDoc(elt, ei);
        startInterface(elt, ei);
        startBlock();

        if (ei.getValueType() != ElementInfo.NO_VALUE)
            printProperty("Value", javaTypeForCode(ei.getValueType()), null, false, elt.getDefaultText());

        endBlock();

    }

    void printElementImplBody(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        emit("public class " + ei.getShortJavaImplName() + " extends " + ei.getBaseImplName());
        indent();
        emit("implements " + ei.getFullJavaIntfName() + ", org.apache.xmlbeans.XmlObject");
        outdent();
        startBlock();
        printElementConstructor(elt, ei);

        if (ei.getValueType() != ElementInfo.NO_VALUE)
            printPropertyImpl("Value", javaTypeForCode(ei.getValueType()), ei.getValueType(), null, false, elt.getDefaultText(), true);

        endBlock();
    }

    static String prettyQName(QName qname)
    {
        String result = qname.getLocalPart();
        if (qname.getNamespaceURI() != null)
            result += "(@" + qname.getNamespaceURI() + ")";
        return result;
    }

    void printJavaDoc(String comment)
        throws IOException
    {
        emit("");
        emit("/**");
        emit(" * " + comment);
        emit(" */");
    }

    void printInnerElementJavaDoc(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        emit("/**");
        emit(" * Java content class for a root element declaration");
        emit(" * containing one " + prettyQName(elt.getName()) + " element.");
        emit(" */");
    }

    void startInterface(SchemaComponent sc, ComponentInfo ci)
        throws IOException
    {
        String shortName = ci.getShortJavaIntfName();
        String extend = null;

        if (sc.getComponentType() == SchemaComponent.ELEMENT)
            extend = "javax.xml.bind.Element";

        if (ci.getBaseIntfName() != null)
        {
            if (extend == null)
                extend = ci.getBaseIntfName();
            else
                extend += ", " + ci.getBaseIntfName();
        }

        emit("public interface " + shortName + (extend != null ? " extends " + extend : ""));
    }

    void printProperty(String propname, String javatype, String collectiontype, boolean nillable, String defaulttext)
        throws IOException
    {
        printJavaDoc("Gets the " + propname + " property");
        emit(javatype + " get" + propname + "();");

        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propname));

        printJavaDoc("Sets the " + propname + " property");
        emit("void set" + propname + "(" + javatype + " " + safeVarName + ");");
    }

    void printPropertyImpl(String propname, String javatype, int javaTypeCode, String collectiontype, boolean nillable, String defaulttext, boolean value)
        throws IOException
    {
        emit("public " + javatype + " get" + propname + "()");
        startBlock();
        if (value)
            emit("return " + jgetMethodNameForJavaTypeCode(javaTypeCode) + ";");
        else
            ; // TODO
        endBlock();

        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propname));

        emit("public void set" + propname + "(" + javatype + " " + safeVarName + ")");
        startBlock();
        if (value)
            emit("set(" + safeVarName + ");");
        else
            ; // TODO
        endBlock();
    }

    void printElementConstructor(SchemaGlobalElement elt, ElementInfo ei)
        throws IOException
    {
        /*
        String superargs = "";
        if (ei.getSuperCtrArgCount() == 1)
            superargs = "sType";
        else if (ei.getSuperCtrArgCount() == 2)
            superargs = "sType, false";
            */

        emit("public " + ei.getShortJavaImplName() + "()");
        startBlock();
        // emit("super(" + superargs + ");");
        endBlock();
    }

    String jgetMethodNameForJavaTypeCode(int javaType) throws IOException {
        switch (javaType)
        {
            case SchemaProperty.JAVA_BOOLEAN:
                return "booleanValue()";

            case SchemaProperty.JAVA_FLOAT:
                return "floatValue()";

            case SchemaProperty.JAVA_DOUBLE:
                return "doubleValue()";

            case SchemaProperty.JAVA_BYTE:
                return "byteValue()";
                                   
            case SchemaProperty.JAVA_SHORT:
                return "shortValue()";

            case SchemaProperty.JAVA_INT:
                return "intValue()";

            case SchemaProperty.JAVA_LONG:
                return "longValue()";

            case SchemaProperty.JAVA_BIG_DECIMAL:
                return "bigDecimalValue()";

            case SchemaProperty.JAVA_BIG_INTEGER:
                return "bigIntegerValue()";

            case SchemaProperty.JAVA_STRING:
                return "stringValue()";

            case SchemaProperty.JAVA_BYTE_ARRAY:
                return "byteArrayValue()";

            case SchemaProperty.JAVA_CALENDAR:
                return "calendarValue()";

            case SchemaProperty.JAVA_QNAME:
                return "qNameValue()";

            default:
                throw new IllegalStateException();
                                  
                /*
            case SchemaProperty.JAVA_LIST:
                emit("return target.listValue();"); break;

            case SchemaProperty.JAVA_ENUM:
                emit("return (" + type + ")target.enumValue();"); break;
                */

        }
    }

    static String javaTypeForCode(int javaTypeCode)
    {
        switch (javaTypeCode)
        {
            case SchemaProperty.JAVA_BOOLEAN:
                return "boolean";
            case SchemaProperty.JAVA_FLOAT:
                return "float";
            case SchemaProperty.JAVA_DOUBLE:
                return "double";
            case SchemaProperty.JAVA_BYTE:
                return "byte";
            case SchemaProperty.JAVA_SHORT:
                return "short";
            case SchemaProperty.JAVA_INT:
                return "int";
            case SchemaProperty.JAVA_LONG:
                return "long";

            case SchemaProperty.JAVA_BIG_DECIMAL:
                return "java.math.BigDecimal";
            case SchemaProperty.JAVA_BIG_INTEGER:
                return "java.math.BigInteger";
            case SchemaProperty.JAVA_STRING:
                return "java.lang.String";
            case SchemaProperty.JAVA_BYTE_ARRAY:
                return "byte[]";
            case SchemaProperty.JAVA_QNAME:
                return "javax.xml.namespace.QName";
            case SchemaProperty.JAVA_CALENDAR:
                return "java.util.Calendar";

            default:
                assert(false);
                throw new IllegalStateException();
        }
    }
}
