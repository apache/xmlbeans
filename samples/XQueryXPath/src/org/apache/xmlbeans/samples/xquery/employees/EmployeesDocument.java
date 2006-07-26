/*
 * An XML document type.
 * Localname: employees
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees;


/**
 * A document containing one employees(@http://xmlbeans.apache.org/samples/xquery/employees) element.
 *
 * This is a complex type.
 */
public interface EmployeesDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EmployeesDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD4FB2ECA19277E984CA2AB92FEEBD267").resolveHandle("employees8e53doctype");
    
    /**
     * Gets the "employees" element
     */
    org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees getEmployees();
    
    /**
     * Sets the "employees" element
     */
    void setEmployees(org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees employees);
    
    /**
     * Appends and returns a new empty "employees" element
     */
    org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees addNewEmployees();
    
    /**
     * An XML employees(@http://xmlbeans.apache.org/samples/xquery/employees).
     *
     * This is a complex type.
     */
    public interface Employees extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Employees.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD4FB2ECA19277E984CA2AB92FEEBD267").resolveHandle("employees9844elemtype");
        
        /**
         * Gets array of all "employee" elements
         */
        org.apache.xmlbeans.samples.xquery.employees.EmployeeType[] getEmployeeArray();
        
        /**
         * Gets ith "employee" element
         */
        org.apache.xmlbeans.samples.xquery.employees.EmployeeType getEmployeeArray(int i);
        
        /**
         * Returns number of "employee" element
         */
        int sizeOfEmployeeArray();
        
        /**
         * Sets array of all "employee" element
         */
        void setEmployeeArray(org.apache.xmlbeans.samples.xquery.employees.EmployeeType[] employeeArray);
        
        /**
         * Sets ith "employee" element
         */
        void setEmployeeArray(int i, org.apache.xmlbeans.samples.xquery.employees.EmployeeType employee);
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "employee" element
         */
        org.apache.xmlbeans.samples.xquery.employees.EmployeeType insertNewEmployee(int i);
        
        /**
         * Appends and returns a new empty value (as xml) as the last "employee" element
         */
        org.apache.xmlbeans.samples.xquery.employees.EmployeeType addNewEmployee();
        
        /**
         * Removes the ith "employee" element
         */
        void removeEmployee(int i);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees newInstance() {
              return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument.Employees) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument newInstance() {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeesDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
