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

import javax.xml.namespace.QName;

/**
 * Represents builtin bindings.
 */
public abstract class BuiltinBindingLoader extends BaseBindingLoader
{

    // ========================================================================
    // Constants

    private static final String XSNS = "http://www.w3.org/2001/XMLSchema";
    private static final String SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/";

    // ========================================================================
    // Static methods

    public static BindingLoader getInstance()
    {
        return getBuiltinBindingLoader(false);
    }

    public static BindingLoader getBuiltinBindingLoader(boolean jaxRpc)
    {
        if (jaxRpc)
            return JaxRpcBuiltinBindingLoader.getInstance();
        else
            return DefaultBuiltinBindingLoader.getInstance();
    }

    // ========================================================================
    // Private methods
    private void addMapping(String xmlType,
                            String javaName,
                            boolean fromJavaDefault,
                            boolean fromXmlDefault)
    {
        final QName xml_name = new QName(XSNS, xmlType);
        addMapping(xml_name, javaName, fromJavaDefault, fromXmlDefault);
    }

    private void addSoapMapping(String xmlType,
                                String javaName,
                                boolean fromJavaDefault,
                                boolean fromXmlDefault)
    {
        final QName xml_name = new QName(SOAPENC, xmlType);
        addMapping(xml_name, javaName, fromJavaDefault, fromXmlDefault);
    }

    private void addMapping(QName xml_name,
                            String javaName,
                            boolean fromJavaDefault,
                            boolean fromXmlDefault)
    {
        XmlTypeName xn = XmlTypeName.forTypeNamed(xml_name);
        JavaTypeName jn = JavaTypeName.forString(javaName);
        BindingTypeName btName = BindingTypeName.forPair(jn, xn);
        BindingType bType = new BuiltinBindingType(btName);

        addBindingType(bType);
        if (fromJavaDefault) {
            if (bType.getName().getXmlName().getComponentType() == XmlTypeName.ELEMENT)
                addElementFor(bType.getName().getJavaName(), bType.getName());
            else
                addTypeFor(bType.getName().getJavaName(), bType.getName());
        }
        if (fromXmlDefault) {
            if (bType.getName().getJavaName().isXmlObject())
                addXmlObjectFor(bType.getName().getXmlName(), bType.getName());
            else
                addPojoFor(bType.getName().getXmlName(), bType.getName());
        }
    }

    protected void addPojoTwoWay(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, true);
    }

    protected void addPojoXml(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, false, true);
    }

    protected void addPojoJava(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, true, false);
    }

    protected void addPojo(String xmlType, String javaName)
    {
        addMapping(xmlType, javaName, false, false);
    }


    protected void addSoapPojoXml(String xmlType, String javaName)
    {
        addSoapMapping(xmlType, javaName, false, true);
    }

    protected void addSoapPojo(String xmlType, String javaName)
    {
        addSoapMapping(xmlType, javaName, false, false);
    }
}
