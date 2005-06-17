/*
 * XML Type:  employeeType
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.EmployeeType
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees.impl;
/**
 * An XML employeeType(@http://xmlbeans.apache.org/samples/xquery/employees).
 *
 * This is a complex type.
 */
public class EmployeeTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.xquery.employees.EmployeeType
{
    
    public EmployeeTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName NAME$0 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/xquery/employees", "name");
    private static final javax.xml.namespace.QName ADDRESS$2 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/xquery/employees", "address");
    private static final javax.xml.namespace.QName PHONE$4 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/xquery/employees", "phone");
    
    
    /**
     * Gets the "name" element
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "name" element
     */
    public org.apache.xmlbeans.XmlString xgetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(NAME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "name" element
     */
    public void setName(java.lang.String name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NAME$0);
            }
            target.setStringValue(name);
        }
    }
    
    /**
     * Sets (as xml) the "name" element
     */
    public void xsetName(org.apache.xmlbeans.XmlString name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(NAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(NAME$0);
            }
            target.set(name);
        }
    }
    
    /**
     * Gets array of all "address" elements
     */
    public org.apache.xmlbeans.samples.xquery.employees.AddressType[] getAddressArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ADDRESS$2, targetList);
            org.apache.xmlbeans.samples.xquery.employees.AddressType[] result = new org.apache.xmlbeans.samples.xquery.employees.AddressType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "address" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.AddressType getAddressArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.AddressType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.AddressType)get_store().find_element_user(ADDRESS$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "address" element
     */
    public int sizeOfAddressArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ADDRESS$2);
        }
    }
    
    /**
     * Sets array of all "address" element
     */
    public void setAddressArray(org.apache.xmlbeans.samples.xquery.employees.AddressType[] addressArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(addressArray, ADDRESS$2);
        }
    }
    
    /**
     * Sets ith "address" element
     */
    public void setAddressArray(int i, org.apache.xmlbeans.samples.xquery.employees.AddressType address)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.AddressType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.AddressType)get_store().find_element_user(ADDRESS$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(address);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "address" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.AddressType insertNewAddress(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.AddressType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.AddressType)get_store().insert_element_user(ADDRESS$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "address" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.AddressType addNewAddress()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.AddressType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.AddressType)get_store().add_element_user(ADDRESS$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "address" element
     */
    public void removeAddress(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ADDRESS$2, i);
        }
    }
    
    /**
     * Gets array of all "phone" elements
     */
    public org.apache.xmlbeans.samples.xquery.employees.PhoneType[] getPhoneArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PHONE$4, targetList);
            org.apache.xmlbeans.samples.xquery.employees.PhoneType[] result = new org.apache.xmlbeans.samples.xquery.employees.PhoneType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "phone" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.PhoneType getPhoneArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.PhoneType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.PhoneType)get_store().find_element_user(PHONE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "phone" element
     */
    public int sizeOfPhoneArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PHONE$4);
        }
    }
    
    /**
     * Sets array of all "phone" element
     */
    public void setPhoneArray(org.apache.xmlbeans.samples.xquery.employees.PhoneType[] phoneArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(phoneArray, PHONE$4);
        }
    }
    
    /**
     * Sets ith "phone" element
     */
    public void setPhoneArray(int i, org.apache.xmlbeans.samples.xquery.employees.PhoneType phone)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.PhoneType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.PhoneType)get_store().find_element_user(PHONE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(phone);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "phone" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.PhoneType insertNewPhone(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.PhoneType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.PhoneType)get_store().insert_element_user(PHONE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "phone" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.PhoneType addNewPhone()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.PhoneType target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.PhoneType)get_store().add_element_user(PHONE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "phone" element
     */
    public void removePhone(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PHONE$4, i);
        }
    }
}
