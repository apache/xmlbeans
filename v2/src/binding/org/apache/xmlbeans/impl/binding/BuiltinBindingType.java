/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 6, 2003
 */
package org.apache.xmlbeans.impl.binding;

public class BuiltinBindingType extends BindingType
{
    // note: only this one constructor; builtin binding types can't be loaded
    public BuiltinBindingType(JavaName jName, XmlName xName, boolean isXmlObj)
    {
        super(jName, xName, isXmlObj);
    }
}
