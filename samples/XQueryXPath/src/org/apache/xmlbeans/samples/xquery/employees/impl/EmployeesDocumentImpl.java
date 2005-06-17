/*
 * An XML document type.
 * Localname: employees
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees.impl;
/**
 * A document containing one employees(@http://xmlbeans.apache.org/samples/xquery/employees) element.
 *
 * This is a complex type.
 */
public class EmployeesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument
{
    
    public EmployeesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EMPLOYEES$0 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/xquery/employees", "employees");
    
    
    /**
     * Gets the "employees" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees getEmployees()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees)get_store().find_element_user(EMPLOYEES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "employees" element
     */
    public void setEmployees(org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees employees)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees)get_store().find_element_user(EMPLOYEES$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees)get_store().add_element_user(EMPLOYEES$0);
            }
            target.set(employees);
        }
    }
    
    /**
     * Appends and returns a new empty "employees" element
     */
    public org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees addNewEmployees()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees target = null;
            target = (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees)get_store().add_element_user(EMPLOYEES$0);
            return target;
        }
    }
    /**
     * An XML employees(@http://xmlbeans.apache.org/samples/xquery/employees).
     *
     * This is a complex type.
     */
    public static class EmployeesImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees
    {
        
        public EmployeesImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName EMPLOYEE$0 = 
            new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/xquery/employees", "employee");
        
        
        /**
         * Gets array of all "employee" elements
         */
        public org.apache.xmlbeans.samples.xquery.employees.EmployeeType[] getEmployeeArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(EMPLOYEE$0, targetList);
                org.apache.xmlbeans.samples.xquery.employees.EmployeeType[] result = new org.apache.xmlbeans.samples.xquery.employees.EmployeeType[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "employee" element
         */
        public org.apache.xmlbeans.samples.xquery.employees.EmployeeType getEmployeeArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.xquery.employees.EmployeeType target = null;
                target = (org.apache.xmlbeans.samples.xquery.employees.EmployeeType)get_store().find_element_user(EMPLOYEE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "employee" element
         */
        public int sizeOfEmployeeArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(EMPLOYEE$0);
            }
        }
        
        /**
         * Sets array of all "employee" element
         */
        public void setEmployeeArray(org.apache.xmlbeans.samples.xquery.employees.EmployeeType[] employeeArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(employeeArray, EMPLOYEE$0);
            }
        }
        
        /**
         * Sets ith "employee" element
         */
        public void setEmployeeArray(int i, org.apache.xmlbeans.samples.xquery.employees.EmployeeType employee)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.xquery.employees.EmployeeType target = null;
                target = (org.apache.xmlbeans.samples.xquery.employees.EmployeeType)get_store().find_element_user(EMPLOYEE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(employee);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "employee" element
         */
        public org.apache.xmlbeans.samples.xquery.employees.EmployeeType insertNewEmployee(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.xquery.employees.EmployeeType target = null;
                target = (org.apache.xmlbeans.samples.xquery.employees.EmployeeType)get_store().insert_element_user(EMPLOYEE$0, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "employee" element
         */
        public org.apache.xmlbeans.samples.xquery.employees.EmployeeType addNewEmployee()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.xquery.employees.EmployeeType target = null;
                target = (org.apache.xmlbeans.samples.xquery.employees.EmployeeType)get_store().add_element_user(EMPLOYEE$0);
                return target;
            }
        }
        
        /**
         * Removes the ith "employee" element
         */
        public void removeEmployee(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(EMPLOYEE$0, i);
            }
        }
    }
}
