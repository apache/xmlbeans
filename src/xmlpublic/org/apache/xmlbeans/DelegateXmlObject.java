package org.apache.xmlbeans;

/**
 * See {@link FilterXmlObject}
 */
public interface DelegateXmlObject
{
    /**
     * This method is called to obtain the underlying XmlObject.
     * Implement this method to supply or compute the wrapped object.
     */
    XmlObject underlyingXmlObject();
}
