/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 2, 2003
 */
package org.apache.xmlbeans.impl.binding;

public abstract class BindingLoader
{
    public abstract BindingType getBindingType(JavaName jName, XmlName xName);

    public abstract BindingType getBindingTypeForXmlPojo(XmlName xName);

    public abstract BindingType getBindingTypeForXmlObj(XmlName xName);

    public abstract BindingType getBindingTypeForJava(JavaName jName);

}
