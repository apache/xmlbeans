package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;

import javax.xml.namespace.QName;

final class AnyTypeRuntimeBindingType
    extends RuntimeBindingType
{
    static final QName ANY_TYPE_NAME =
        new QName(RuntimeBindingTypeTable.XSD_NS, "anyType");

    AnyTypeRuntimeBindingType(BuiltinBindingType type,
                              TypeConverter converter)
        throws XmlException
    {
        super(type, converter, converter);
        assert converter != null;
        assert ANY_TYPE_NAME.equals(type.getName().getXmlName().getQName());
    }

    void accept(RuntimeTypeVisitor visitor)
        throws XmlException
    {
        visitor.visit(this);
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
        throws XmlException
    {
    }

    boolean hasElementChildren()
    {
        return true;
    }


}
