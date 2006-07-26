/*
 * XML Type:  addressType
 * Namespace: http://xmlbeans.apache.org/samples/xquery/employees
 * Java type: org.apache.xmlbeans.samples.xquery.employees.AddressType
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.xquery.employees;


/**
 * An XML addressType(@http://xmlbeans.apache.org/samples/xquery/employees).
 *
 * This is a complex type.
 */
public interface AddressType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AddressType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD4FB2ECA19277E984CA2AB92FEEBD267").resolveHandle("addresstype93ectype");
    
    /**
     * Gets the "street" element
     */
    java.lang.String getStreet();
    
    /**
     * Gets (as xml) the "street" element
     */
    org.apache.xmlbeans.XmlString xgetStreet();
    
    /**
     * Sets the "street" element
     */
    void setStreet(java.lang.String street);
    
    /**
     * Sets (as xml) the "street" element
     */
    void xsetStreet(org.apache.xmlbeans.XmlString street);
    
    /**
     * Gets the "city" element
     */
    java.lang.String getCity();
    
    /**
     * Gets (as xml) the "city" element
     */
    org.apache.xmlbeans.XmlNCName xgetCity();
    
    /**
     * Sets the "city" element
     */
    void setCity(java.lang.String city);
    
    /**
     * Sets (as xml) the "city" element
     */
    void xsetCity(org.apache.xmlbeans.XmlNCName city);
    
    /**
     * Gets the "state" element
     */
    java.lang.String getState();
    
    /**
     * Gets (as xml) the "state" element
     */
    org.apache.xmlbeans.XmlNCName xgetState();
    
    /**
     * Sets the "state" element
     */
    void setState(java.lang.String state);
    
    /**
     * Sets (as xml) the "state" element
     */
    void xsetState(org.apache.xmlbeans.XmlNCName state);
    
    /**
     * Gets the "zip" element
     */
    java.math.BigInteger getZip();
    
    /**
     * Gets (as xml) the "zip" element
     */
    org.apache.xmlbeans.XmlInteger xgetZip();
    
    /**
     * Sets the "zip" element
     */
    void setZip(java.math.BigInteger zip);
    
    /**
     * Sets (as xml) the "zip" element
     */
    void xsetZip(org.apache.xmlbeans.XmlInteger zip);
    
    /**
     * Gets the "location" attribute
     */
    java.lang.String getLocation();
    
    /**
     * Gets (as xml) the "location" attribute
     */
    org.apache.xmlbeans.XmlNCName xgetLocation();
    
    /**
     * Sets the "location" attribute
     */
    void setLocation(java.lang.String location);
    
    /**
     * Sets (as xml) the "location" attribute
     */
    void xsetLocation(org.apache.xmlbeans.XmlNCName location);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType newInstance() {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.xquery.employees.AddressType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.xquery.employees.AddressType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
