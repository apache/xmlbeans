/*
 * XML Type:  phoneType
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.PhoneType
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees.impl;
/**
 * An XML phoneType(@http://xmlbeans.apache.org/samples/xquery/employees).
 *
 * This is an atomic type that is a restriction of org.apache.xmlbeans.XmlString.
 */
public class PhoneTypeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements org.apache.xmlbeans.samples.xquery.employees.PhoneType
{
    
    public PhoneTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected PhoneTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName LOCATION$0 = 
        new javax.xml.namespace.QName("", "location");
    
    
    /**
     * Gets the "location" attribute
     */
    public java.lang.String getLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LOCATION$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "location" attribute
     */
    public org.apache.xmlbeans.XmlNCName xgetLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNCName target = null;
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(LOCATION$0);
            return target;
        }
    }
    
    /**
     * Sets the "location" attribute
     */
    public void setLocation(java.lang.String location)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LOCATION$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LOCATION$0);
            }
            target.setStringValue(location);
        }
    }
    
    /**
     * Sets (as xml) the "location" attribute
     */
    public void xsetLocation(org.apache.xmlbeans.XmlNCName location)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNCName target = null;
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(LOCATION$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNCName)get_store().add_attribute_user(LOCATION$0);
            }
            target.set(location);
        }
    }
}
