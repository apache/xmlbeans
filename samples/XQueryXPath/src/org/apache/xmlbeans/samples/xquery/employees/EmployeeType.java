/*
 * XML Type:  employeeType
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.EmployeeType
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees;


/**
 * An XML employeeType(@http://xmlbeans.apache.org/samples/xquery/employees).
 *
 * This is a complex type.
 */
public interface EmployeeType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EmployeeType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD4FB2ECA19277E984CA2AB92FEEBD267").resolveHandle("employeetype5e98type");
    
    /**
     * Gets the "name" element
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "name" element
     */
    org.apache.xmlbeans.XmlString xgetName();
    
    /**
     * Sets the "name" element
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "name" element
     */
    void xsetName(org.apache.xmlbeans.XmlString name);
    
    /**
     * Gets array of all "address" elements
     */
    org.apache.xmlbeans.samples.xquery.employees.AddressType[] getAddressArray();
    
    /**
     * Gets ith "address" element
     */
    org.apache.xmlbeans.samples.xquery.employees.AddressType getAddressArray(int i);
    
    /**
     * Returns number of "address" element
     */
    int sizeOfAddressArray();
    
    /**
     * Sets array of all "address" element
     */
    void setAddressArray(org.apache.xmlbeans.samples.xquery.employees.AddressType[] addressArray);
    
    /**
     * Sets ith "address" element
     */
    void setAddressArray(int i, org.apache.xmlbeans.samples.xquery.employees.AddressType address);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "address" element
     */
    org.apache.xmlbeans.samples.xquery.employees.AddressType insertNewAddress(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "address" element
     */
    org.apache.xmlbeans.samples.xquery.employees.AddressType addNewAddress();
    
    /**
     * Removes the ith "address" element
     */
    void removeAddress(int i);
    
    /**
     * Gets array of all "phone" elements
     */
    org.apache.xmlbeans.samples.xquery.employees.PhoneType[] getPhoneArray();
    
    /**
     * Gets ith "phone" element
     */
    org.apache.xmlbeans.samples.xquery.employees.PhoneType getPhoneArray(int i);
    
    /**
     * Returns number of "phone" element
     */
    int sizeOfPhoneArray();
    
    /**
     * Sets array of all "phone" element
     */
    void setPhoneArray(org.apache.xmlbeans.samples.xquery.employees.PhoneType[] phoneArray);
    
    /**
     * Sets ith "phone" element
     */
    void setPhoneArray(int i, org.apache.xmlbeans.samples.xquery.employees.PhoneType phone);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "phone" element
     */
    org.apache.xmlbeans.samples.xquery.employees.PhoneType insertNewPhone(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "phone" element
     */
    org.apache.xmlbeans.samples.xquery.employees.PhoneType addNewPhone();
    
    /**
     * Removes the ith "phone" element
     */
    void removePhone(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType newInstance() {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.EmployeeType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.EmployeeType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
