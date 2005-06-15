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

package org.apache.xmlbeans.impl.schema;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.config.InterfaceExtension;
import org.apache.xmlbeans.impl.config.ExtensionHolder;
import org.apache.xmlbeans.impl.config.PrePostExtension;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.XmlObject;

/**
 * Prints the java code for a single schema type
 */
public final class SchemaTypeCodePrinter
{
    Writer _writer;
    int    _indent;

    static final String LINE_SEPARATOR =
        System.getProperty("line.separator") == null
            ? "\n"
            : System.getProperty("line.separator");

    static final String MAX_SPACES = "                                        ";
    static final int INDENT_INCREMENT = 4;

    public static final String INDEX_CLASSNAME = "TypeSystemHolder";

    public SchemaTypeCodePrinter ( Writer writer )
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

    String encodeString ( String s )
    {
        StringBuffer sb = new StringBuffer();

        sb.append( '"' );

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (ch == '"')
            {
                sb.append( '\\' );
                sb.append( '\"' );
            }
            else if (ch == '\\')
            {
                sb.append( '\\' );
                sb.append( '\\' );
            }
            else if (ch == '\r')
            {
                sb.append( '\\' );
                sb.append( 'r' );
            }
            else if (ch == '\n')
            {
                sb.append( '\\' );
                sb.append( 'n' );
            }
            else if (ch == '\t')
            {
                sb.append( '\\' );
                sb.append( 't' );
            }
            else
                sb.append( ch );
        }

        sb.append( '"' );

        return sb.toString();
    }

    void emit(String s) throws IOException
    {
        int indent = _indent;
        
        if (indent > MAX_SPACES.length() / 2)
            indent = MAX_SPACES.length() / 4 + indent / 2;
        
        if (indent > MAX_SPACES.length())
            indent = MAX_SPACES.length();
        
        _writer.write(MAX_SPACES.substring(0, indent));
        _writer.write(s);
        _writer.write(LINE_SEPARATOR);
        
        // System.out.print(MAX_SPACES.substring(0, indent));
        // System.out.println(s);
    }

    public static void printTypeImpl ( Writer writer, SchemaType sType )
        throws IOException
    {
        new SchemaTypeCodePrinter( writer ).printTypeImpl( sType, sType.getTypeSystem() );
    }

    public static void printType ( Writer writer, SchemaType sType )
        throws IOException
    {
        new SchemaTypeCodePrinter( writer ). printType( sType, sType.getTypeSystem() );
    }

    public static void printLoader ( Writer writer, SchemaTypeSystem system )
        throws IOException
    {
        new SchemaTypeCodePrinter( writer ).printIndexType( system );
    }

    void printType(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        printTopComment(sType);
        printPackage(sType, true);
        emit("");
        printInnerType(sType, system);
        _writer.flush();
    }

    void printTypeImpl(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        printTopComment(sType);
        printPackage(sType, false);
        printInnerTypeImpl(sType, system, false);
    }

    /**
     * Since not all schema types have java types, this skips
     * over any that don't and gives you the nearest java base type.
     */
    String findJavaType ( SchemaType sType )
    {
        while ( sType.getFullJavaName() == null )
            sType = sType.getBaseType();
        
        return sType.getFullJavaName();
    }

    static String prettyQName(QName qname)
    {
        String result = qname.getLocalPart();
        if (qname.getNamespaceURI() != null)
            result += "(@" + qname.getNamespaceURI() + ")";
        return result;
    }

    void printInnerTypeJavaDoc(SchemaType sType) throws IOException
    {
        QName name = sType.getName();
        if (name == null)
        {
            if (sType.isDocumentType())
                name = sType.getDocumentElementName();
            else if (sType.isAttributeType())
                name = sType.getAttributeTypeAttributeName();
            else if (sType.getContainerField() != null)
                name = sType.getContainerField().getName();
        }

        emit("/**");
        if (sType.isDocumentType())
            emit(" * A document containing one " + prettyQName(name) + " element.");
        else if (sType.isAttributeType())
            emit(" * A document containing one " + prettyQName(name) + " attribute.");
        else if (name != null)
            emit(" * An XML " + prettyQName(name) + ".");
        else
            emit(" * An anonymous inner XML type.");
        emit(" *");
        switch (sType.getSimpleVariety())
        {
            case SchemaType.NOT_SIMPLE:
                emit(" * This is a complex type.");
                break;
            case SchemaType.ATOMIC:
                emit(" * This is an atomic type that is a restriction of " + getFullJavaName(sType) + ".");

                break;
            case SchemaType.LIST:
                emit(" * This is a list type whose items are " + sType.getListItemType().getFullJavaName() + ".");
                break;
            case SchemaType.UNION:
                emit(" * This is a union type. Instances are of one of the following types:");
                SchemaType[] members = sType.getUnionConstituentTypes();
                for (int i = 0; i < members.length; i++)
                    emit(" *     " + members[i].getFullJavaName());
                break;
        }
        emit(" */");
    }

    public static String indexClassForSystem(SchemaTypeSystem system)
    {
        String name = system.getName();
        return name + "." + INDEX_CLASSNAME;
    }

    static String shortIndexClassForSystem(SchemaTypeSystem system)
    {
        return INDEX_CLASSNAME;
    }

    void printStaticTypeDeclaration(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        emit("public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)" +
                indexClassForSystem(system) +
                ".typeSystem.resolveHandle(\"" +
                ((SchemaTypeSystemImpl)system).handleForType(sType) + "\");");
    }

    void printIndexType(SchemaTypeSystem system) throws IOException
    {
        String shortName = shortIndexClassForSystem(system);
        emit("package " + system.getName() + ";");
        emit("");
        emit("public final class " + shortName);
        emit("{");
        indent();
        emit("private " + shortName + "() { }");
        emit("public static final org.apache.xmlbeans.SchemaTypeSystem typeSystem = loadTypeSystem();");
        emit("static { typeSystem.resolve(); }");
        emit("private static final org.apache.xmlbeans.SchemaTypeSystem loadTypeSystem()");
        emit("{");
        indent();
        emit("try { return (org.apache.xmlbeans.SchemaTypeSystem)Class.forName(\"org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl\", true, " + shortName + ".class.getClassLoader()).getConstructor(new Class[] { Class.class }).newInstance(new java.lang.Object[] { " + shortName + ".class }); }");
        emit("catch (ClassNotFoundException e) { throw new RuntimeException(\"Cannot load org.apache.xmlbeans.impl.SchemaTypeSystemImpl: make sure xbean.jar is on the classpath.\", e); }");
        emit("catch (Exception e) { throw new RuntimeException(\"Could not instantiate SchemaTypeSystemImpl (\" + e.toString() + \"): is the version of xbean.jar correct?\", e); }");
        outdent();
        emit("}");
        outdent();
        emit("}");
    }

    void printInnerType(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        emit("");

        printInnerTypeJavaDoc(sType);

        startInterface(sType);
        
        printStaticTypeDeclaration(sType, system);

        if (sType.isSimpleType())
        {
            if (sType.hasStringEnumValues())
                printStringEnumeration(sType);
        }
        else
        {
            SchemaProperty[] props = getDerivedProperties(sType);
            for (int i = 0; i < props.length; i++)
            {
                SchemaProperty prop = props[i];

                printPropertyGetters( sType,
                    prop.getName(),
                    prop.isAttribute(),
                    prop.getJavaPropertyName(),
                    prop.getJavaTypeCode(),
                    javaTypeForProperty(prop),
                    xmlTypeForProperty(prop),
                    prop.hasNillable() != SchemaProperty.NEVER,
                    prop.extendsJavaOption(),
                    prop.extendsJavaArray(),
                    prop.extendsJavaSingleton()
                );

                if (!prop.isReadOnly())
                {
                    printPropertySetters(
                        prop.getName(),
                        prop.isAttribute(),
                        prop.getJavaPropertyName(),
                        prop.getJavaTypeCode(),
                        javaTypeForProperty(prop),
                        xmlTypeForProperty(prop),
                        prop.hasNillable() != SchemaProperty.NEVER,
                        prop.extendsJavaOption(),
                        prop.extendsJavaArray(),
                        prop.extendsJavaSingleton()
                    );
                }
            }

        }
        
        printNestedInnerTypes(sType, system);

        printFactory(sType);

        endBlock();
    }

    void printFactory(SchemaType sType) throws IOException
    {
        // Only need full factories for top-level types
        boolean fullFactory = true;
        if (sType.isAnonymousType() && ! sType.isDocumentType() && !sType.isAttributeType())
            fullFactory = false;

        String fullName = sType.getFullJavaName().replace('$', '.');

        emit("");
        emit("/**");
        emit(" * A factory class with static methods for creating instances");
        emit(" * of this type.");
        emit(" */");
        emit("");
        // BUGBUG - Can I use the name loader here?  could it be a
        // nested type name?  It is lower case!
        emit("public static final class Factory");
        emit("{");
        indent();

        if (sType.isSimpleType())
        {
            emit("public static " + fullName + " newValue(java.lang.Object obj) {");
            emit("  return (" + fullName + ") type.newValue( obj ); }");
            emit("");
        }

        emit("public static " + fullName + " newInstance() {");
        emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }");
        emit("");

        emit("public static " + fullName + " newInstance(org.apache.xmlbeans.XmlOptions options) {");
        emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }");
        emit("");

        if (fullFactory)
        {
            emit("public static " + fullName + " parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }");
            emit("");

            emit("public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }");
            emit("");

            emit("public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }");
            emit("");
        }

        emit("private Factory() { } // No instance of this class allowed");
        outdent();
        emit("}");
    }

    void printNestedInnerTypes(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        SchemaType[] anonTypes = sType.getAnonymousTypes();
        for (int i = 0; i < anonTypes.length; i++)
        {
            if (anonTypes[i].isSkippedAnonymousType())
                printNestedInnerTypes(anonTypes[i], system);
            else
                printInnerType(anonTypes[i], system);
        }
    }

    void printTopComment(SchemaType sType) throws IOException
    {
        emit("/*");
        if (sType.getName() != null)
        {
            emit(" * XML Type:  " + sType.getName().getLocalPart());
            emit(" * Namespace: " + sType.getName().getNamespaceURI());
        }
        else
        {
            QName thename = null;

            if (sType.isDocumentType())
            {
                thename = sType.getDocumentElementName();
                emit(" * An XML document type.");
            }
            else if (sType.isAttributeType())
            {
                thename = sType.getAttributeTypeAttributeName();
                emit(" * An XML attribute type.");
            }
            else
                assert false;

            assert( thename != null );
            
            emit(" * Localname: " + thename.getLocalPart());
            emit(" * Namespace: " + thename.getNamespaceURI());
        }
        emit(" * Java type: " + sType.getFullJavaName());
        emit(" *");
        emit(" * Automatically generated - do not modify.");
        emit(" */");
    }

    void printPackage(SchemaType sType, boolean intf) throws IOException
    {
        String fqjn;
        if (intf)
            fqjn = sType.getFullJavaName();
        else
            fqjn = sType.getFullJavaImplName();

        int lastdot = fqjn.lastIndexOf('.');
        if (lastdot < 0)
            return;
        String pkg = fqjn.substring(0, lastdot);
        emit("package " + pkg + ";");
    }

    void startInterface(SchemaType sType) throws IOException
    {
        String shortName = sType.getShortJavaName();
        
        String baseInterface = findJavaType(sType.getBaseType());

        /*
        StringBuffer specializedInterfaces = new StringBuffer();

        if (sType.getSimpleVariety() == SchemaType.ATOMIC &&
            sType.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
        {
            int bits = sType.getDecimalSize();
            if (bits == SchemaType.SIZE_BIG_INTEGER)
                specializedInterfaces.append(", org.apache.xmlbeans.BigIntegerValue");
            if (bits == SchemaType.SIZE_LONG)
                specializedInterfaces.append(", org.apache.xmlbeans.LongValue");
            if (bits <= SchemaType.SIZE_INT)
                specializedInterfaces.append(", org.apache.xmlbeans.IntValue");
        }
        if (sType.getSimpleVariety() == SchemaType.LIST)
            specializedInterfaces.append(", org.apache.xmlbeans.ListValue");

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            SchemaType ctype = sType.getUnionCommonBaseType();
            String javaTypeHolder = javaTypeHolderForType(ctype);
            if (javaTypeHolder != null)
                specializedInterfaces.append(", " + javaTypeHolder);
        }
        */

        emit("public interface " + shortName + " extends " + baseInterface + getExtensionInterfaces(sType) );
        emit("{");
        indent();
        emitSpecializedAccessors(sType);
    }

    private static String getExtensionInterfaces(SchemaType sType)
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl==null)
            return "";

        StringBuffer sb = new StringBuffer();

        ExtensionHolder extHolder = sImpl.getExtensionHolder();
        if (extHolder!=null)
        {
            List exts = extHolder.getInterfaceExtensionsFor(sType.getFullJavaName());
            for (Iterator i = exts.iterator(); i.hasNext(); )
            {
                InterfaceExtension ext = (InterfaceExtension)i.next();
                sb.append(", " + ext.getInterfaceNameForJavaSource() );
            }
        }
        return sb.toString();
    }

    private static SchemaTypeImpl getImpl(SchemaType sType)
    {
        if (sType instanceof SchemaTypeImpl)
            return (SchemaTypeImpl)sType;
        else
            return null;
    }

    private void emitSpecializedAccessors(SchemaType sType) throws IOException
    {
        if (sType.getSimpleVariety() == SchemaType.ATOMIC &&
            sType.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
        {
            int bits = sType.getDecimalSize();
            int parentBits = sType.getBaseType().getDecimalSize();
            if (bits != parentBits || sType.getBaseType().getFullJavaName() == null)
            {
                if (bits == SchemaType.SIZE_BIG_INTEGER)
                {
                    emit("java.math.BigInteger getBigIntegerValue();");
                    emit("void setBigIntegerValue(java.math.BigInteger bi);");
                    emit("/** @deprecated */");
                    emit("java.math.BigInteger bigIntegerValue();");
                    emit("/** @deprecated */");
                    emit("void set(java.math.BigInteger bi);");
                }
                else if (bits == SchemaType.SIZE_LONG)
                {
                    emit("long getLongValue();");
                    emit("void setLongValue(long l);");
                    emit("/** @deprecated */");
                    emit("long longValue();");
                    emit("/** @deprecated */");
                    emit("void set(long l);");
                }
                else if (bits == SchemaType.SIZE_INT)
                {
                    emit("int getIntValue();");
                    emit("void setIntValue(int i);");
                    emit("/** @deprecated */");
                    emit("int intValue();");
                    emit("/** @deprecated */");
                    emit("void set(int i);");
                }
                else if (bits == SchemaType.SIZE_SHORT)
                {
                    emit("short getShortValue();");
                    emit("void setShortValue(short s);");
                    emit("/** @deprecated */");
                    emit("short shortValue();");
                    emit("/** @deprecated */");
                    emit("void set(short s);");
                }
                else if (bits == SchemaType.SIZE_BYTE)
                {
                    emit("byte getByteValue();");
                    emit("void setByteValue(byte b);");
                    emit("/** @deprecated */");
                    emit("byte byteValue();");
                    emit("/** @deprecated */");
                    emit("void set(byte b);");
                }
            }
        }

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            emit("java.lang.Object getObjectValue();");
            emit("void setObjectValue(java.lang.Object val);");
            emit("/** @deprecated */");
            emit("java.lang.Object objectValue();");
            emit("/** @deprecated */");
            emit("void objectSet(java.lang.Object val);");
            emit("org.apache.xmlbeans.SchemaType instanceType();");
            SchemaType ctype = sType.getUnionCommonBaseType();
            if (ctype != null && ctype.getSimpleVariety() != SchemaType.UNION);
                emitSpecializedAccessors(ctype);
        }

        if (sType.getSimpleVariety() == SchemaType.LIST)
        {
            emit("java.util.List getListValue();");
            emit("java.util.List xgetListValue();");
            emit("void setListValue(java.util.List list);");
            emit("/** @deprecated */");
            emit("java.util.List listValue();");
            emit("/** @deprecated */");
            emit("java.util.List xlistValue();");
            emit("/** @deprecated */");
            emit("void set(java.util.List list);");
        }
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

    void printJavaDoc(String sentence) throws IOException
    {
        emit("");
        emit("/**");
        emit(" * " + sentence);
        emit(" */");
    }

    void printShortJavaDoc(String sentence) throws IOException
    {
        emit("/** " + sentence + " */");
    }


    // we need to account for all of these scenarios:
    //
    // 1. 
    // BaseType
    // DerivedType extends/restricts BaseType
    // we need to return BaseType
    //
    // 2.
    // BaseType
    // RedefinedType redefines BaseType
    // we need to return RedefinedType
    //
    // 3.
    // BaseType
    // DerivedType extends/restricts BaseType
    // RedefinedType redefines DerivedType
    // we need to return BaseType
    static SchemaType findBaseEnumType(SchemaType sType) {
      SchemaType original = sType;
      SchemaTypeImpl originalImpl = (SchemaTypeImpl) sType;

      // keep going until you're at the top of the redefinition -- the original type that has been redefined. 
      // this may be an extension
      while (((SchemaTypeImpl)sType).isRedefinition())
        sType = sType.getBaseType();

      // if it is an extension, the base type will have enumeration vals
      while (sType.getBaseType().hasStringEnumValues())
        sType = sType.getBaseType();

      // if it is NOT an extension, we are at the original type that was redefined. the base will not have enum values,
      // and the fullJavaName of the original redefined type will be null. in this case, we need to use the most redefined redefine.
      if (((SchemaTypeImpl)original).isRedefinition() && sType.getFullJavaName() == null)
        sType = original;
      return sType;

    }

    public static String javaStringEscape(String str)
    {
        // forbidden: \n, \r, \", \\.
        test: {
            for (int i = 0; i < str.length(); i++)
            {
                switch (str.charAt(i))
                {
                    case '\n':
                    case '\r':
                    case '\"':
                    case '\\':
                        break test;
                }
            }
            return str;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            switch (ch)
            {
                default:
                    sb.append(ch);
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
            }
        }
        return sb.toString();
    }

    void printStringEnumeration(SchemaType sType) throws IOException
    {
        SchemaType baseEnumType = findBaseEnumType(sType);
        String baseEnumClass = baseEnumType.getFullJavaName();

        if (baseEnumType == sType)
        {
            emit("");
            emit("org.apache.xmlbeans.StringEnumAbstractBase enumValue();");
            emit("void set(org.apache.xmlbeans.StringEnumAbstractBase e);");
        }

        emit("");
        SchemaStringEnumEntry[] entries = sType.getStringEnumEntries();
        for (int i = 0; i < entries.length; i++)
        {
            String enumValue = entries[i].getString();
            String constName = entries[i].getEnumName();
            if (baseEnumType != sType)
                emit("static final " + baseEnumClass + ".Enum " + constName + " = " + baseEnumClass + "." + constName + ";");
            else
                emit("static final Enum " + constName + " = Enum.forString(\"" + javaStringEscape(enumValue) + "\");");
        }
        emit("");
        for (int i = 0; i < entries.length; i++)
        {
            String constName = "INT_" + entries[i].getEnumName();
            if (baseEnumType != sType)
                emit("static final int " + constName + " = " + baseEnumClass + "." + constName + ";");
            else
                emit("static final int " + constName + " = Enum." + constName + ";");
        }
        if (baseEnumType == sType)
        {
            emit("");
            emit("/**");
            emit(" * Enumeration value class for " + baseEnumClass + ".");
            emit(" * These enum values can be used as follows:");
            emit(" * <pre>");
            emit(" * enum.toString(); // returns the string value of the enum");
            emit(" * enum.intValue(); // returns an int value, useful for switches");
            if (entries.length > 0)
            emit(" * // e.g., case Enum.INT_" + entries[0].getEnumName());
            emit(" * Enum.forString(s); // returns the enum value for a string");
            emit(" * Enum.forInt(i); // returns the enum value for an int");
            emit(" * </pre>");
            emit(" * Enumeration objects are immutable singleton objects that");
            emit(" * can be compared using == object equality. They have no");
            emit(" * public constructor. See the constants defined within this");
            emit(" * class for all the valid values.");
            emit(" */");
            emit("static final class Enum extends org.apache.xmlbeans.StringEnumAbstractBase");
            emit("{");
            indent();
            emit("/**");
            emit(" * Returns the enum value for a string, or null if none.");
            emit(" */");
            emit("public static Enum forString(java.lang.String s)");
            emit("    { return (Enum)table.forString(s); }");
            emit("/**");
            emit(" * Returns the enum value corresponding to an int, or null if none.");
            emit(" */");
            emit("public static Enum forInt(int i)");
            emit("    { return (Enum)table.forInt(i); }");
            emit("");
            emit("private Enum(java.lang.String s, int i)");
            emit("    { super(s, i); }");
            emit("");
            for (int i = 0; i < entries.length; i++)
            {
                String constName = "INT_" + entries[i].getEnumName();
                int intValue = entries[i].getIntValue();
                emit("static final int " + constName + " = " + intValue + ";");
            }
            emit("");
            emit("public static final org.apache.xmlbeans.StringEnumAbstractBase.Table table =");
            emit("    new org.apache.xmlbeans.StringEnumAbstractBase.Table");
            emit("(");
            indent();
            emit("new Enum[]");
            emit("{");
            indent();
            for (int i = 0; i < entries.length; i++)
            {
                String enumValue = entries[i].getString();
                String constName = "INT_" + entries[i].getEnumName();
                emit("new Enum(\"" + javaStringEscape(enumValue) + "\", " + constName + "),");
            }
            outdent();
            emit("}");
            outdent();
            emit(");");
            emit("private static final long serialVersionUID = 1L;");
            emit("private java.lang.Object readResolve() { return forInt(intValue()); } ");
            outdent();
            emit("}");
        }
    }

    String xmlTypeForProperty(SchemaProperty sProp)
    {
        SchemaType sType = sProp.javaBasedOnType();
        return findJavaType(sType).replace('$', '.');
    }

    static boolean xmlTypeForPropertyIsUnion(SchemaProperty sProp)
    {
        SchemaType sType = sProp.javaBasedOnType();
        return (sType.isSimpleType() && sType.getSimpleVariety() == SchemaType.UNION);
    }

    String javaTypeForProperty(SchemaProperty sProp)
    {
        // The type to use is the XML object....
        if (sProp.getJavaTypeCode() == SchemaProperty.XML_OBJECT)
        {
            SchemaType sType = sProp.javaBasedOnType();
            return findJavaType(sType).replace('$', '.');
        }

        switch (sProp.getJavaTypeCode())
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
            case SchemaProperty.JAVA_GDATE:
                return "org.apache.xmlbeans.GDate";
            case SchemaProperty.JAVA_GDURATION:
                return "org.apache.xmlbeans.GDuration";
            case SchemaProperty.JAVA_DATE:
                return "java.util.Date";
            case SchemaProperty.JAVA_QNAME:
                return "javax.xml.namespace.QName";
            case SchemaProperty.JAVA_LIST:
                return "java.util.List";
            case SchemaProperty.JAVA_CALENDAR:
                return "java.util.Calendar";

            case SchemaProperty.JAVA_ENUM:
                SchemaType sType = findBaseEnumType(sProp.javaBasedOnType());
                return findJavaType(sType).replace('$', '.') + ".Enum";

            case SchemaProperty.JAVA_OBJECT:
                return "java.lang.Object";

            default:
                assert(false);
                throw new IllegalStateException();
        }
    }

    void printPropertyGetters(SchemaType sType, QName qName, boolean isAttr,
                       String propertyName, int javaType,
                       String type, String xtype,
                       boolean nillable, boolean optional,
                       boolean several, boolean singleton)
       throws IOException
    {
        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");

        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);

        if (singleton)
        {
            printJavaDoc((several ? "Gets first " : "Gets the ") + propdesc + " for sType: " + sType.toString());
            emit(type + " get" + propertyName + "();");

            if (!xmltype)
            {
                printJavaDoc((several ? "Gets (as xml) first " : "Gets (as xml) the ") + propdesc);
                emit(xtype + " xget" + propertyName + "();");
            }

            if (nillable)
            {
                printJavaDoc((several ? "Tests for nil first " : "Tests for nil ") + propdesc);
                emit("boolean isNil" + propertyName + "();");
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "True if has at least one " : "True if has ") + propdesc);
            emit("boolean isSet" + propertyName + "();");
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            printJavaDoc("Gets array of all " + propdesc + "s");
            emit(type + "[] get" + arrayName + "();");

            printJavaDoc("Gets ith " + propdesc);
            emit(type + " get" + arrayName + "(int i);");

            if (!xmltype)
            {
                printJavaDoc("Gets (as xml) array of all " + propdesc + "s");
                emit(xtype + "[] xget" + arrayName + "();");

                printJavaDoc("Gets (as xml) ith " + propdesc);
                emit(xtype + " xget" + arrayName + "(int i);");
            }

            if (nillable)
            {
                printJavaDoc("Tests for nil ith " + propdesc);
                emit("boolean isNil" + arrayName + "(int i);");
            }

            printJavaDoc("Returns number of " + propdesc);
            emit("int sizeOf" + arrayName + "();");
        }
    }

    void printPropertySetters(QName qName, boolean isAttr,
                       String propertyName, int javaType, String type, String xtype,
                       boolean nillable, boolean optional,
                       boolean several, boolean singleton)
       throws IOException
    {
        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propertyName));
        if (safeVarName.equals("i"))
            safeVarName = "iValue";
        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);

        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");

        if (singleton)
        {
            printJavaDoc((several ? "Sets first " : "Sets the ") + propdesc);
            emit("void set" + propertyName + "(" + type + " " + safeVarName + ");");

            if (!xmltype)
            {
                printJavaDoc((several ? "Sets (as xml) first " : "Sets (as xml) the ") + propdesc);
                emit("void xset" + propertyName + "(" + xtype + " " + safeVarName + ");");
            }
            
            if (xmltype && !several)
            {
                printJavaDoc("Appends and returns a new empty " + propdesc);
                emit(xtype + " addNew" + propertyName + "();");
            }

            if (nillable)
            {
                printJavaDoc((several ? "Nils the first " : "Nils the ") + propdesc);
                emit("void setNil" + propertyName + "();");
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "Removes first " : "Unsets the ") + propdesc);
            emit("void unset" + propertyName + "();");
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            printJavaDoc("Sets array of all " + propdesc);
            emit("void set" + arrayName + "(" + type + "[] " + safeVarName + "Array);");

            printJavaDoc("Sets ith " + propdesc);
            emit("void set" + arrayName + "(int i, " + type + " " + safeVarName + ");");

            if (!xmltype)
            {
                printJavaDoc("Sets (as xml) array of all " + propdesc);
                emit("void xset" + arrayName + "(" + xtype + "[]" + safeVarName + "Array);");

                printJavaDoc("Sets (as xml) ith " + propdesc);
                emit("void xset" + arrayName + "(int i, " + xtype + " " + safeVarName + ");");
            }

            if (nillable)
            {
                printJavaDoc("Nils the ith " + propdesc);
                emit("void setNil" + arrayName + "(int i);");
            }

            if (!xmltype)
            {
                printJavaDoc("Inserts the value as the ith " + propdesc);
                emit("void insert" + propertyName + "(int i, " + type + " " + safeVarName + ");");

                printJavaDoc("Appends the value as the last " + propdesc);
                emit("void add" + propertyName + "(" + type + " " + safeVarName + ");");
            }

            if (xmltype)
            {
                printJavaDoc("Inserts and returns a new empty value (as xml) as the ith " + propdesc);
                emit(xtype + " insertNew" + propertyName + "(int i);");
    
                printJavaDoc("Appends and returns a new empty value (as xml) as the last " + propdesc);
                emit(xtype + " addNew" + propertyName + "();");
            }

            printJavaDoc("Removes the ith " + propdesc);
            emit("void remove" + propertyName + "(int i);");
        }
    }

    String getAtomicRestrictionType(SchemaType sType) {
        SchemaType pType = sType.getPrimitiveType();
        switch (pType.getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                return "org.apache.xmlbeans.impl.values.XmlAnySimpleTypeImpl";
            case SchemaType.BTC_BOOLEAN:
                return "org.apache.xmlbeans.impl.values.JavaBooleanHolderEx";
            case SchemaType.BTC_BASE_64_BINARY:
                return "org.apache.xmlbeans.impl.values.JavaBase64HolderEx";
            case SchemaType.BTC_HEX_BINARY:
                return "org.apache.xmlbeans.impl.values.JavaHexBinaryHolderEx";
            case SchemaType.BTC_ANY_URI:
                return "org.apache.xmlbeans.impl.values.JavaUriHolderEx";
            case SchemaType.BTC_QNAME:
                return "org.apache.xmlbeans.impl.values.JavaQNameHolderEx";
            case SchemaType.BTC_NOTATION:
                return "org.apache.xmlbeans.impl.values.JavaNotationHolderEx";
            case SchemaType.BTC_FLOAT:
                return "org.apache.xmlbeans.impl.values.JavaFloatHolderEx";
            case SchemaType.BTC_DOUBLE:
                return "org.apache.xmlbeans.impl.values.JavaDoubleHolderEx";
            case SchemaType.BTC_DECIMAL:
                switch (sType.getDecimalSize())
                {
                    default:
                        assert(false);
                    case SchemaType.SIZE_BIG_DECIMAL:
                        return "org.apache.xmlbeans.impl.values.JavaDecimalHolderEx";
                    case SchemaType.SIZE_BIG_INTEGER:
                        return "org.apache.xmlbeans.impl.values.JavaIntegerHolderEx";
                    case SchemaType.SIZE_LONG:
                        return "org.apache.xmlbeans.impl.values.JavaLongHolderEx";
                    case SchemaType.SIZE_INT:
                    case SchemaType.SIZE_SHORT:
                    case SchemaType.SIZE_BYTE:
                        return "org.apache.xmlbeans.impl.values.JavaIntHolderEx";
                }
            case SchemaType.BTC_STRING:
                if (sType.hasStringEnumValues())
                    return "org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx";
                else
                    return "org.apache.xmlbeans.impl.values.JavaStringHolderEx";

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return "org.apache.xmlbeans.impl.values.JavaGDateHolderEx";

            case SchemaType.BTC_DURATION:
                return "org.apache.xmlbeans.impl.values.JavaGDurationHolderEx";
            default:
                assert(false) : "unrecognized primitive type";
                return null;
        }
    }

    static SchemaType findBaseType(SchemaType sType)
    {
        while (sType.getFullJavaName() == null)
            sType = sType.getBaseType();
        return sType;
    }

    String getBaseClass(SchemaType sType) {
        SchemaType baseType = findBaseType(sType.getBaseType());

        switch (sType.getSimpleVariety())
        {
            case SchemaType.NOT_SIMPLE:
                // non-simple-content: inherit from base type impl
                if (!XmlObject.type.equals(baseType))
                    return baseType.getFullJavaImplName();
                return "org.apache.xmlbeans.impl.values.XmlComplexContentImpl";

            case SchemaType.ATOMIC:
                // We should only get called for restrictions
                assert(! sType.isBuiltinType());
                return getAtomicRestrictionType(sType);

            case SchemaType.LIST:
                return "org.apache.xmlbeans.impl.values.XmlListImpl";

            case SchemaType.UNION:
                return "org.apache.xmlbeans.impl.values.XmlUnionImpl";

            default:
                throw new IllegalStateException();
        }
    }

    void printConstructor(SchemaType sType, String shortName) throws IOException {
        emit("");
        emit("public " + shortName + "(org.apache.xmlbeans.SchemaType sType)");
        startBlock();
        emit("super(sType" + (sType.getSimpleVariety() == SchemaType.NOT_SIMPLE ?
                             "":
                             ", " + !sType.isSimpleType()) +
             ");");
        endBlock();

        if (sType.getSimpleVariety() != SchemaType.NOT_SIMPLE)
        {
            emit("");
            emit("protected " + shortName + "(org.apache.xmlbeans.SchemaType sType, boolean b)");
            startBlock();
            emit("super(sType, b);");
            endBlock();
        }
    }

    void startClass(SchemaType sType, boolean isInner) throws IOException
    {
        String shortName = sType.getShortJavaImplName();
        String baseClass = getBaseClass(sType);
        StringBuffer interfaces = new StringBuffer();
        interfaces.append(sType.getFullJavaName().replace('$', '.'));

        if (sType.getSimpleVariety() == SchemaType.UNION) {
            SchemaType[] memberTypes = sType.getUnionMemberTypes();
            for (int i = 0 ; i < memberTypes.length ; i++)
                interfaces.append(", " + memberTypes[i].getFullJavaName().replace('$', '.'));
        }

        emit("public " + ( isInner ? "static ": "" ) + "class " + shortName +
            " extends " + baseClass + " implements " + interfaces.toString());

        startBlock();
    }

    void makeAttributeDefaultValue(String jtargetType, SchemaProperty prop, String identifier) throws IOException
    {
        String fullName = jtargetType;
        if (fullName == null)
            fullName = prop.javaBasedOnType().getFullJavaName().replace('$', '.');

        emit("target = (" + fullName + ")get_default_attribute_value(" + identifier + ");");
    }

    void makeMissingValue(int javaType) throws IOException
    {
        switch (javaType)
        {
            case SchemaProperty.JAVA_BOOLEAN:
                emit("return false;"); break;

            case SchemaProperty.JAVA_FLOAT:
                emit("return 0.0f;"); break;

            case SchemaProperty.JAVA_DOUBLE:
                emit("return 0.0;"); break;

            case SchemaProperty.JAVA_BYTE:
            case SchemaProperty.JAVA_SHORT:
            case SchemaProperty.JAVA_INT:
                emit("return 0;"); break;

            case SchemaProperty.JAVA_LONG:
                emit("return 0L;"); break;

            default:
            case SchemaProperty.XML_OBJECT:
            case SchemaProperty.JAVA_BIG_DECIMAL:
            case SchemaProperty.JAVA_BIG_INTEGER:
            case SchemaProperty.JAVA_STRING:
            case SchemaProperty.JAVA_BYTE_ARRAY:
            case SchemaProperty.JAVA_GDATE:
            case SchemaProperty.JAVA_GDURATION:
            case SchemaProperty.JAVA_DATE:
            case SchemaProperty.JAVA_QNAME:
            case SchemaProperty.JAVA_LIST:
            case SchemaProperty.JAVA_CALENDAR:
            case SchemaProperty.JAVA_ENUM:
            case SchemaProperty.JAVA_OBJECT:
                emit("return null;"); break;
        }
    }

    void printJGetArrayValue(int javaType, String type) throws IOException {
        switch (javaType)
        {
            case SchemaProperty.XML_OBJECT:
                emit(type + "[] result = new " + type + "[targetList.size()];");
                emit("targetList.toArray(result);");
                break;

            case SchemaProperty.JAVA_ENUM:
                emit(type + "[] result = new " + type + "[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = (" + type + ")((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getEnumValue();");
                break;
                
            case SchemaProperty.JAVA_BOOLEAN:
                emit("boolean[] result = new boolean[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBooleanValue();");
                break;

            case SchemaProperty.JAVA_FLOAT:
                emit("float[] result = new float[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getFloatValue();");
                break;

            case SchemaProperty.JAVA_DOUBLE:
                emit("double[] result = new double[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getDoubleValue();");
                break;

            case SchemaProperty.JAVA_BYTE:
                emit("byte[] result = new byte[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteValue();");
                break;

            case SchemaProperty.JAVA_SHORT:
                emit("short[] result = new short[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getShortValue();");
                break;

            case SchemaProperty.JAVA_INT:
                emit("int[] result = new int[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getIntValue();");
                break;

            case SchemaProperty.JAVA_LONG:
                emit("long[] result = new long[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getLongValue();");
                break;

            case SchemaProperty.JAVA_BIG_DECIMAL:
                emit("java.math.BigDecimal[] result = new java.math.BigDecimal[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBigDecimalValue();");
                break;

            case SchemaProperty.JAVA_BIG_INTEGER:
                emit("java.math.BigInteger[] result = new java.math.BigInteger[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBigIntegerValue();");
                break;

            case SchemaProperty.JAVA_STRING:
                emit("java.lang.String[] result = new java.lang.String[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();");
                break;

            case SchemaProperty.JAVA_BYTE_ARRAY:
                emit("byte[][] result = new byte[targetList.size()][];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();");
                break;

            case SchemaProperty.JAVA_CALENDAR:
                emit("java.util.Calendar[] result = new java.util.Calendar[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getCalendarValue();");
                break;

            case SchemaProperty.JAVA_DATE:
                emit("java.util.Date[] result = new java.util.Date[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getDateValue();");
                break;

            case SchemaProperty.JAVA_GDATE:
                emit("org.apache.xmlbeans.GDate[] result = new org.apache.xmlbeans.GDate[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getGDateValue();");
                break;

            case SchemaProperty.JAVA_GDURATION:
                emit("org.apache.xmlbeans.GDuration[] result = new org.apache.xmlbeans.GDuration[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getGDurationValue();");
                break;

            case SchemaProperty.JAVA_QNAME:
                emit("javax.xml.namespace.QName[] result = new javax.xml.namespace.QName[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getQNameValue();");
                break;

            case SchemaProperty.JAVA_LIST:
                emit("java.util.List[] result = new java.util.List[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getListValue();");
                break;

            case SchemaProperty.JAVA_OBJECT:
                emit("java.lang.Object[] result = new java.lang.Object[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getObjectValue();");
                break;

        }
        emit("return result;");
    }
    void printJGetValue(int javaType, String type) throws IOException {
        switch (javaType)
        {
        case SchemaProperty.XML_OBJECT:
            emit("return target;"); break;

        case SchemaProperty.JAVA_BOOLEAN:
            emit("return target.getBooleanValue();"); break;

        case SchemaProperty.JAVA_FLOAT:
            emit("return target.getFloatValue();"); break;

        case SchemaProperty.JAVA_DOUBLE:
            emit("return target.getDoubleValue();"); break;

        case SchemaProperty.JAVA_BYTE:
            emit("return target.getByteValue();"); break;

        case SchemaProperty.JAVA_SHORT:
            emit("return target.getShortValue();"); break;

        case SchemaProperty.JAVA_INT:
            emit("return target.getIntValue();"); break;

        case SchemaProperty.JAVA_LONG:
            emit("return target.getLongValue();"); break;

        case SchemaProperty.JAVA_BIG_DECIMAL:
            emit("return target.getBigDecimalValue();"); break;

        case SchemaProperty.JAVA_BIG_INTEGER:
            emit("return target.getBigIntegerValue();"); break;

        case SchemaProperty.JAVA_STRING:
            emit("return target.getStringValue();"); break;

        case SchemaProperty.JAVA_BYTE_ARRAY:
            emit("return target.getByteArrayValue();"); break;

        case SchemaProperty.JAVA_GDATE:
            emit("return target.getGDateValue();"); break;

        case SchemaProperty.JAVA_GDURATION:
            emit("return target.getGDurationValue();"); break;

        case SchemaProperty.JAVA_CALENDAR:
            emit("return target.getCalendarValue();"); break;

        case SchemaProperty.JAVA_DATE:
            emit("return target.getDateValue();"); break;

        case SchemaProperty.JAVA_QNAME:
            emit("return target.getQNameValue();"); break;

        case SchemaProperty.JAVA_LIST:
            emit("return target.getListValue();"); break;

        case SchemaProperty.JAVA_ENUM:
            emit("return (" + type + ")target.getEnumValue();"); break;

        case SchemaProperty.JAVA_OBJECT:
            emit("return target.getObjectValue();"); break;
        }
    }

    String jsetMethod(int javaType) throws IOException
    {
        switch (javaType)
        {
            case SchemaProperty.XML_OBJECT:
                return "target.set";

            case SchemaProperty.JAVA_BOOLEAN:
                return "target.setBooleanValue";

            case SchemaProperty.JAVA_FLOAT:
                return "target.setFloatValue";

            case SchemaProperty.JAVA_DOUBLE:
                return "target.setDoubleValue";

            case SchemaProperty.JAVA_BYTE:
                return "target.setByteValue";

            case SchemaProperty.JAVA_SHORT:
                return "target.setShortValue";

            case SchemaProperty.JAVA_INT:
                return "target.setIntValue";

            case SchemaProperty.JAVA_LONG:
                return "target.setLongValue";

            case SchemaProperty.JAVA_BIG_DECIMAL:
                return "target.setBigDecimalValue";

            case SchemaProperty.JAVA_BIG_INTEGER:
                return "target.setBigIntegerValue";

            case SchemaProperty.JAVA_STRING:
                return "target.setStringValue";

            case SchemaProperty.JAVA_BYTE_ARRAY:
                return "target.setByteArrayValue";

            case SchemaProperty.JAVA_GDATE:
                return "target.setGDateValue";

            case SchemaProperty.JAVA_GDURATION:
                return "target.setGDurationValue";

            case SchemaProperty.JAVA_CALENDAR:
                return "target.setCalendarValue";

            case SchemaProperty.JAVA_DATE:
                return "target.setDateValue";

            case SchemaProperty.JAVA_QNAME:
                return "target.setQNameValue";

            case SchemaProperty.JAVA_LIST:
                return "target.setListValue";

            case SchemaProperty.JAVA_ENUM:
                return "target.setEnumValue";

            case SchemaProperty.JAVA_OBJECT:
                return "target.setObjectValue";

            default:
                throw new IllegalStateException();
        }
    }

    String getIdentifier(Map qNameMap, QName qName) {
        return ((String[])qNameMap.get(qName))[0];
    }

    String getSetIdentifier(Map qNameMap, QName qName) {
        String[] identifiers = (String[])qNameMap.get(qName);
        return identifiers[1] == null ? identifiers[0] : identifiers[1];
    }

    Map printStaticFields(SchemaProperty[] properties) throws IOException {
        final Map results = new HashMap();

        emit("");
        for (int i = 0; i < properties.length; i++)
        {
            final String[] identifiers = new String[2];
            final SchemaProperty prop = properties[i];
            final QName name = prop.getName();
            results.put(name, identifiers);
            final String javaName = prop.getJavaPropertyName();
            identifiers[0] = (javaName + "$" + (i * 2)).toUpperCase();
            final String uriString =  "\"" + name.getNamespaceURI() + "\"";

            emit("private static final javax.xml.namespace.QName " + identifiers[0] +
                 " = " );
            indent();
            emit("new javax.xml.namespace.QName(" +
                 uriString + ", \"" + name.getLocalPart() + "\");");
            outdent();

            if (properties[i].acceptedNames() != null)
            {
                final QName[] qnames = properties[i].acceptedNames();

                if (qnames.length > 1)
                {
                    identifiers[1] = (javaName + "$" + (i*2+1)).toUpperCase();

                    emit("private static final org.apache.xmlbeans.QNameSet " + identifiers[1] +
                        " = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { " );
                    indent();
                    for (int j = 0 ; j < qnames.length ; j++)
                    {
                        emit("new javax.xml.namespace.QName(\"" + qnames[j].getNamespaceURI() +
                            "\", \"" + qnames[j].getLocalPart() + "\"),");
                    }

                    outdent();

                    emit("});");
                }
            }
        }
        emit("");
        return results;
    }

    void emitImplementationPreamble() throws IOException
    {
        emit("synchronized (monitor())");
        emit("{");
        indent();
        emit("check_orphaned();");
    }
    
    void emitImplementationPostamble() throws IOException
    {
        outdent();
        emit("}");
    }
    
    void emitDeclareTarget(boolean declareTarget, String xtype)
            throws IOException
    {
        if (declareTarget)
            emit(xtype + " target = null;");
    }

    void emitAddTarget(String identifier, boolean isAttr, boolean declareTarget, String xtype)
        throws IOException
    {
        if (isAttr)
            emit("target = (" + xtype + ")get_store().add_attribute_user(" + identifier + ");");
        else
            emit("target = (" + xtype + ")get_store().add_element_user(" + identifier + ");");
    }

    void emitPre(SchemaType sType, int opType, String identifier, boolean isAttr) throws IOException
    {
        emitPre(sType, opType, identifier, isAttr, "-1");
    }

    void emitPre(SchemaType sType, int opType, String identifier, boolean isAttr, String index) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl==null)
            return;

        ExtensionHolder extHolder = sImpl.getExtensionHolder();
        if (extHolder==null)
            return;

        PrePostExtension ext = extHolder.getPrePostExtensionsFor(sType.getFullJavaName());
        if (ext!=null)
        {
            if (ext.hasPreCall())
            {
                emit("if ( " + ext.getPreCall(opType, identifier, isAttr, index) + " )");
                startBlock();
            }
        }
    }

    void emitPost(SchemaType sType, int opType, String identifier, boolean isAttr) throws IOException
    {
        emitPost(sType, opType, identifier, isAttr, "-1");
    }
    
    void emitPost(SchemaType sType, int opType, String identifier, boolean isAttr, String index) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl==null)
            return;

        ExtensionHolder extHolder = sImpl.getExtensionHolder();
        if (extHolder==null)
            return;

        PrePostExtension ext = extHolder.getPrePostExtensionsFor(sType.getFullJavaName());
        if (ext!=null)
        {
            if (ext.hasPreCall())
            {
                endBlock();
            }

            if (ext.hasPostCall())
                emit(ext.getPostCall(opType, identifier, isAttr, index));
        }
    }

    private static final int NOTHING = 1;
    private static final int ADD_NEW_VALUE = 3;
    private static final int THROW_EXCEPTION = 4;

    void emitGetTarget(String setIdentifier, 
                       String identifier,
                       boolean isAttr, 
                       String index, 
                       int nullBehaviour,
                       String xtype)
        throws IOException
    {
        assert setIdentifier != null && identifier != null;

        emit(xtype + " target = null;");

        if (isAttr)
            emit("target = (" + xtype + ")get_store().find_attribute_user(" + identifier + ");");
        else
            emit("target = (" + xtype + ")get_store().find_element_user(" + setIdentifier + ", " + index + ");");

        if (nullBehaviour == NOTHING)
            return;

        emit("if (target == null)");

        startBlock();

        switch (nullBehaviour)
        {
            case ADD_NEW_VALUE:
                // target already emited, no need for emitDeclareTarget(false, xtype);
                emitAddTarget(identifier, isAttr, false, xtype);
                break;

            case THROW_EXCEPTION:
                emit("throw new IndexOutOfBoundsException();");
                break;

            case NOTHING:
                break;

            default:
                assert false : "Bad behaviour type: " + nullBehaviour;
        }

        endBlock();
    }

    void printGetterImpls(
        SchemaProperty prop, QName qName, boolean isAttr, String propertyName,
        int javaType, String type, String xtype, boolean nillable,
        boolean optional, boolean several, boolean singleton,
        boolean isunion,
        String identifier, String setIdentifier )
            throws IOException
    {
        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");
        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);
        String jtargetType = (isunion || !xmltype) ? "org.apache.xmlbeans.SimpleValue" : xtype;

        if (singleton)
        {
            // Value getProp()
            printJavaDoc((several ? "Gets first " : "Gets the ") + propdesc);
            emit("public " + type + " get" + propertyName + "()");
            startBlock();
            emitImplementationPreamble();

            emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, jtargetType);

            if (isAttr && (prop.hasDefault() == SchemaProperty.CONSISTENTLY ||
                    prop.hasFixed() == SchemaProperty.CONSISTENTLY))
            {
                emit("if (target == null)");
                startBlock();
                makeAttributeDefaultValue(jtargetType, prop, identifier);
                endBlock();
            }
            emit("if (target == null)");
            startBlock();
            makeMissingValue(javaType);
            endBlock();


            printJGetValue(javaType, type);
            
            emitImplementationPostamble();

            endBlock();

            if (!xmltype)
            {
                // Value xgetProp()
                printJavaDoc((several ? "Gets (as xml) first " : "Gets (as xml) the ") + propdesc);
                emit("public " + xtype + " xget" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, xtype);

                if (isAttr && (prop.hasDefault() == SchemaProperty.CONSISTENTLY ||
                        prop.hasFixed() == SchemaProperty.CONSISTENTLY))
                {
                    emit("if (target == null)");
                    startBlock();
                    makeAttributeDefaultValue(xtype, prop, identifier);
                    endBlock();
                }

                emit("return target;");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                // boolean isNilProp()
                printJavaDoc((several ? "Tests for nil first " : "Tests for nil ") + propdesc);
                emit("public boolean isNil" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, xtype);

                emit("if (target == null) return false;");
                emit("return target.isNil();");
                emitImplementationPostamble();
                endBlock();
            }
        }

        if (optional)
        {
            // boolean isSetProp()
            printJavaDoc((several ? "True if has at least one " : "True if has ") + propdesc);
            emit("public boolean isSet" + propertyName + "()");

            startBlock();
            emitImplementationPreamble();

            if (isAttr)
                emit("return get_store().find_attribute_user(" + identifier +") != null;");
            else
                emit("return get_store().count_elements(" + setIdentifier + ") != 0;");

            emitImplementationPostamble();
            endBlock();
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            // Value[] getProp()
            printJavaDoc("Gets array of all " + propdesc + "s");
            emit("public " + type + "[] get" + arrayName + "()");
            startBlock();
            emitImplementationPreamble();

            emit("java.util.List targetList = new java.util.ArrayList();");
            emit("get_store().find_all_element_users(" + setIdentifier + ", targetList);");

            printJGetArrayValue(javaType, type);

            emitImplementationPostamble();
            endBlock();

            // Value getProp(int i)
            printJavaDoc("Gets ith " + propdesc);
            emit("public " + type + " get" + arrayName + "(int i)");
            startBlock();
            emitImplementationPreamble();

            emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, jtargetType);
            printJGetValue(javaType, type);

            emitImplementationPostamble();
            endBlock();

            if (!xmltype)
            {
                // Value[] xgetProp()
                printJavaDoc("Gets (as xml) array of all " + propdesc + "s");
                emit("public " + xtype + "[] xget" + arrayName + "()");
                startBlock();
                emitImplementationPreamble();
                emit("java.util.List targetList = new java.util.ArrayList();");
                emit("get_store().find_all_element_users(" + setIdentifier + ", targetList);");
                emit(xtype + "[] result = new " + xtype + "[targetList.size()];");
                emit("targetList.toArray(result);");
                emit("return result;");
                emitImplementationPostamble();
                endBlock();

                // Value xgetProp(int i)
                printJavaDoc("Gets (as xml) ith " + propdesc);
                emit("public " + xtype + " xget" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("return (" + xtype + ")target;");
                emitImplementationPostamble();
                endBlock();

            }

            if (nillable)
            {
                // boolean isNil(int i);
                printJavaDoc("Tests for nil ith " + propdesc);
                emit("public boolean isNil" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("return target.isNil();");
                emitImplementationPostamble();
                endBlock();
            }

            // int countProp();
            printJavaDoc("Returns number of " + propdesc);
            emit("public int sizeOf" + arrayName + "()");
            startBlock();
            emitImplementationPreamble();
            emit("return get_store().count_elements(" + setIdentifier +");");
            emitImplementationPostamble();
            endBlock();
        }
    }

    void printSetterImpls(QName qName, boolean isAttr,
                       String propertyName, int javaType, String type, String xtype,
                       boolean nillable, boolean optional,
                       boolean several, boolean singleton,
                       boolean isunion,
                       String identifier, String setIdentifier, SchemaType sType )
            throws IOException
    {
        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propertyName));
        if (safeVarName.equals("i"))
            safeVarName = "iValue";
        else if (safeVarName.equals("target"))
            safeVarName = "targetValue";

        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);
        boolean isobj = (javaType == SchemaProperty.JAVA_OBJECT);
        boolean isSubstGroup = identifier != setIdentifier;
        String jSet = jsetMethod(javaType);
        String jtargetType = (isunion || !xmltype) ? "org.apache.xmlbeans.SimpleValue" : xtype;

        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");

        if (singleton)
        {
            // void setProp(Value v);
            printJavaDoc((several ? "Sets first " : "Sets the ") + propdesc);
            emit("public void set" + propertyName + "(" + type + " " + safeVarName + ")");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
            emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, jtargetType);
            emit(jSet + "(" + safeVarName + ");");
            emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
            emitImplementationPostamble();
            endBlock();

            if (!xmltype)
            {
                // void xsetProp(Value v)
                printJavaDoc((several ? "Sets (as xml) first " : "Sets (as xml) the ") + propdesc);
                emit("public void xset" + propertyName + "(" + xtype + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, xtype);
                emit("target.set(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitImplementationPostamble();
                endBlock();

            }
            
            if (xmltype && !several)
            {
                // Value addNewProp()
                printJavaDoc("Appends and returns a new empty " + propdesc);
                emit("public " + xtype + " addNew" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, xtype);
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitAddTarget(identifier, isAttr, true, xtype);
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emit("return target;");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                printJavaDoc((several ? "Nils the first " : "Nils the ") + propdesc);
                emit("public void setNil" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, xtype);
                emit("target.setNil();");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitImplementationPostamble();
                endBlock();
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "Removes first " : "Unsets the ") + propdesc);
            emit("public void unset" + propertyName + "()");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, several ? "0" : "-1");
            if (isAttr)
                emit("get_store().remove_attribute(" + identifier + ");");
            else
                emit("get_store().remove_element(" + setIdentifier + ", 0);");
            emitPost(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, several ? "0" : "-1");
            emitImplementationPostamble();
            endBlock();
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            // JSET_INDEX
            printJavaDoc("Sets array of all " + propdesc);
            emit("public void set" + arrayName + "(" + type + "[] " + safeVarName + "Array)");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);

            if (isobj)
            {
                if (!isSubstGroup)
                    emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                else
                    emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
            }
            else
            {
                if (!isSubstGroup)
                    emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                else
                    emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
            }
            emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
            emitImplementationPostamble();
            endBlock();

            printJavaDoc("Sets ith " + propdesc);
            emit("public void set" + arrayName + "(int i, " + type + " " + safeVarName + ")");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
            emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, jtargetType);
            emit(jSet + "(" + safeVarName + ");");
            emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
            emitImplementationPostamble();
            endBlock();

            if (!xmltype)
            {
                printJavaDoc("Sets (as xml) array of all " + propdesc);
                emit("public void xset" + arrayName + "(" + xtype + "[]" + safeVarName + "Array)");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );

                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                emitImplementationPostamble();
                endBlock();

                printJavaDoc("Sets (as xml) ith " + propdesc);
                emit("public void xset" + arrayName + "(int i, " + xtype + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("target.set(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                printJavaDoc("Nils the ith " + propdesc);
                emit("public void setNil" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("target.setNil();");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();
            }

            if (!xmltype)
            {
                printJavaDoc("Inserts the value as the ith " + propdesc);
                emit("public void insert" + propertyName + "(int i, " + type + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emit(jtargetType + " target = ");
                indent();
                if (!isSubstGroup)
                    emit("(" + jtargetType + ")get_store().insert_element_user(" + identifier + ", i);");
                else // This is a subst group case
                    emit ("(" + jtargetType +")get_store().insert_element_user(" + setIdentifier + ", " +
                            identifier + ", i);");
                outdent();
                emit(jSet + "(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();

                printJavaDoc("Appends the value as the last " + propdesc);
                emit("public void add" + propertyName + "(" + type + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, jtargetType);
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitAddTarget(identifier, isAttr, true, jtargetType);
                emit(jSet + "(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitImplementationPostamble();
                endBlock();
            }

            if (xmltype)
            {
                printJavaDoc("Inserts and returns a new empty value (as xml) as the ith " + propdesc);
                emit("public " + xtype + " insertNew" + propertyName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, xtype);
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emit("target = (" + xtype + ")get_store().insert_element_user(" + identifier + ", i);");
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emit("return target;");
                emitImplementationPostamble();
                endBlock();
    
                printJavaDoc("Appends and returns a new empty value (as xml) as the last " + propdesc);
                emit("public " + xtype + " addNew" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, xtype);
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitAddTarget(identifier, isAttr, true, xtype);
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emit("return target;");
                emitImplementationPostamble();
                endBlock();
            }

            printJavaDoc("Removes the ith " + propdesc);
            emit("public void remove" + propertyName + "(int i)");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, "i");
            emit("get_store().remove_element(" + setIdentifier + ", i);");
            emitPost(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, "i");
            emitImplementationPostamble();
            endBlock();
        }
    }

    static void getTypeName(Class c, StringBuffer sb) {
        int arrayCount = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            arrayCount++;
        }

        sb.append(c.getName());

        for (int i = 0 ; i < arrayCount; i++)
            sb.append("[]");

    }

    void printInnerTypeImpl(
        SchemaType sType, SchemaTypeSystem system, boolean isInner ) throws IOException
    {
        String shortName = sType.getShortJavaImplName();

        printInnerTypeJavaDoc(sType);

        startClass(sType, isInner);

        printConstructor(sType, shortName);

        printExtensionImplMethods(sType);

        if (!sType.isSimpleType())
        {
            SchemaProperty[] properties;
            
            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT)
            {
                // simple content types impls derive directly from "holder" impls
                // in order to handle the case (for ints or string enums e.g.) where
                // there is a simple type restriction.  So property getters need to
                // be implemented "from scratch" for each derived complex type
                
                properties = sType.getProperties();
            }
            else
            {
                // complex content type implementations derive from base type impls
                // so derived property impls can be reused
                
                properties = getDerivedProperties(sType);
                
            }
            
            Map qNameMap = printStaticFields(properties);

            for (int i = 0; i < properties.length; i++)
            {
                SchemaProperty prop = properties[i];

                QName name = prop.getName();
                String xmlType = xmlTypeForProperty( prop );
                
                printGetterImpls(
                    prop,
                    name,
                    prop.isAttribute(),
                    prop.getJavaPropertyName(),
                    prop.getJavaTypeCode(),
                    javaTypeForProperty(prop),
                    xmlType,
                    prop.hasNillable() != SchemaProperty.NEVER,
                    prop.extendsJavaOption(),
                    prop.extendsJavaArray(),
                    prop.extendsJavaSingleton(),
                    xmlTypeForPropertyIsUnion(prop),
                    getIdentifier(qNameMap, name),
                    getSetIdentifier(qNameMap, name)
                );

                if (!prop.isReadOnly())
                {
                    printSetterImpls(
                        name,
                        prop.isAttribute(),
                        prop.getJavaPropertyName(),
                        prop.getJavaTypeCode(),
                        javaTypeForProperty(prop),
                        xmlType,
                        prop.hasNillable() != SchemaProperty.NEVER,
                        prop.extendsJavaOption(),
                        prop.extendsJavaArray(),
                        prop.extendsJavaSingleton(),
                        xmlTypeForPropertyIsUnion(prop),
                        getIdentifier(qNameMap, name),
                        getSetIdentifier(qNameMap, name),
                        sType
                    );
                }
            }
        }

        printNestedTypeImpls(sType, system);

        endBlock();
    }

    // We have to special case SchemaType.getFullJavaName if this type is a redefinition.
    // In this case, the base type getFullJavaName will be null, and we need to use the fullJavaName
    // from the redefined type.
    private String getFullJavaName(SchemaType sType) {

        SchemaTypeImpl sTypeI = (SchemaTypeImpl) sType;
        String ret = sTypeI.getFullJavaName();

        while (sTypeI.isRedefinition()) {
          ret = sTypeI.getFullJavaName();
          sTypeI = (SchemaTypeImpl) sTypeI.getBaseType();
        }

        return ret;
    }
    private SchemaProperty[] getDerivedProperties(SchemaType sType) {
        // We have to see if this is redefined, because if it is we have
        // to include all properties associated to its supertypes
        QName name = sType.getName();
        if (name != null && name.equals(sType.getBaseType().getName())) {
            SchemaType sType2 = sType.getBaseType();
            // Walk all the redefined types and record any properties
            // not present in sType, because the redefined types do not
            // have a generated class to represent them
            SchemaProperty[] props = sType.getDerivedProperties();
            Map propsByName = new LinkedHashMap();
            for (int i = 0; i < props.length; i++)
                propsByName.put(props[i].getName(), props[i]);
            while (sType2 != null && name.equals(sType2.getName())) {
                props = sType2.getDerivedProperties();
                for (int i = 0; i < props.length; i++)
                    if (!propsByName.containsKey(props[i].getName()))
                        propsByName.put(props[i].getName(), props[i]);
                sType2 = sType2.getBaseType();
            }
            return (SchemaProperty[]) propsByName.values().toArray(new SchemaProperty[0]);
        } else
            return sType.getDerivedProperties();
    }

    private void printExtensionImplMethods(SchemaType sType) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl==null)
            return;

        ExtensionHolder extHolder = sImpl.getExtensionHolder();
        if (extHolder==null)
            return;

        List exts = extHolder.getInterfaceExtensionsFor(sType.getFullJavaName());
        for (Iterator i = exts.iterator(); i.hasNext(); )
        {
            InterfaceExtension ext = (InterfaceExtension)i.next();
            for( int j = 0; j<ext.getInterfaceMethodCount(); j++)
            {
                printJavaDoc("Implementation method for interface " + ext.getInterfaceNameForJavaSource() );
                emit(ext.getInterfaceMethodDecl(j));
                startBlock();
                emit(ext.getInterfaceMethodImpl(j));
                endBlock();
            }
        }
    }

    void printNestedTypeImpls(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        SchemaType[] anonTypes = sType.getAnonymousTypes();
        for (int i = 0; i < anonTypes.length; i++)
        {
            if (anonTypes[i].isSkippedAnonymousType())
                printNestedTypeImpls(anonTypes[i], system);
            else
                printInnerTypeImpl(anonTypes[i], system, true);
        }
    }
}
