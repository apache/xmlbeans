
package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingType;

public class BuiltinRuntimeBindingType
    extends RuntimeBindingType
{
    private final BuiltinBindingType builtinBindingType;

    public BuiltinRuntimeBindingType(BuiltinBindingType type)
        throws XmlException
    {
        super(type);
        builtinBindingType = type;
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader,
                           RuntimeTypeFactory rttFactory)
        throws XmlException
    {
    }


}
