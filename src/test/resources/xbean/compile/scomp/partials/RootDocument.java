/*
 * An XML document type.
 * Localname: root
 * Namespace: partials
 * Java type: partials.RootDocument
 *
 * Automatically generated - do not modify.
 */
package partials;

import org.apache.xmlbeans.impl.schema.ElementFactory;
import org.apache.xmlbeans.impl.schema.AbstractDocumentFactory;
import org.apache.xmlbeans.impl.schema.DocumentFactory;
import org.apache.xmlbeans.impl.schema.SimpleTypeFactory;


/**
 * A document containing one root(@partials) element.
 *
 * This is a complex type.
 */
public interface RootDocument extends org.apache.xmlbeans.XmlObject
{
    DocumentFactory<partials.RootDocument> Factory = new DocumentFactory<>(org.apache.xmlbeans.metadata.system.Partials.TypeSystemHolder.typeSystem, "rootc8d7doctype");
    org.apache.xmlbeans.SchemaType type = Factory.getType();

// <GET>

    /**
     * Gets the "root" element
     */
    partials.RootDocument.Root getRoot();
// </GET>
// <SET>

    /**
     * Sets the "root" element
     */
    void setRoot(partials.RootDocument.Root root);
// </SET>
// <ADD_NEW>

    /**
     * Appends and returns a new empty "root" element
     */
    partials.RootDocument.Root addNewRoot();
// </ADD_NEW>

    /**
     * An XML root(@partials).
     *
     * This is a complex type.
     */
    public interface Root extends org.apache.xmlbeans.XmlObject
    {
        ElementFactory<partials.RootDocument.Root> Factory = new ElementFactory<>(org.apache.xmlbeans.metadata.system.Partials.TypeSystemHolder.typeSystem, "root8a55elemtype");
        org.apache.xmlbeans.SchemaType type = Factory.getType();

// <GET>

        /**
         * Gets the "single" element
         */
        java.math.BigDecimal getSingle();
// </GET>
// <XGET>

        /**
         * Gets (as xml) the "single" element
         */
        org.apache.xmlbeans.XmlDecimal xgetSingle();
// </XGET>
// <IS_NIL>

        /**
         * Tests for nil "single" element
         */
        boolean isNilSingle();
// </IS_NIL>
// <IS_SET>

        /**
         * True if has "single" element
         */
        boolean isSetSingle();
// </IS_SET>
// <SET>

        /**
         * Sets the "single" element
         */
        void setSingle(java.math.BigDecimal single);
// </SET>
// <XSET>

        /**
         * Sets (as xml) the "single" element
         */
        void xsetSingle(org.apache.xmlbeans.XmlDecimal single);
// </XSET>
// <SET_NIL>

        /**
         * Nils the "single" element
         */
        void setNilSingle();
// </SET_NIL>
// <UNSET>

        /**
         * Unsets the "single" element
         */
        void unsetSingle();
// </UNSET>
// <GET>

        /**
         * Gets the "complex" element
         */
        partials.XmlBeanchen getComplex();
// </GET>
// <IS_SET>

        /**
         * True if has "complex" element
         */
        boolean isSetComplex();
// </IS_SET>
// <SET>

        /**
         * Sets the "complex" element
         */
        void setComplex(partials.XmlBeanchen complex);
// </SET>
// <ADD_NEW>

        /**
         * Appends and returns a new empty "complex" element
         */
        partials.XmlBeanchen addNewComplex();
// </ADD_NEW>
// <UNSET>

        /**
         * Unsets the "complex" element
         */
        void unsetComplex();
// </UNSET>
// <GET_LIST>

        /**
         * Gets a List of "primitiveList" elements
         */
        java.util.List<java.math.BigDecimal> getPrimitiveListList();
// </GET_LIST>
// <GET_ARRAY>

        /**
         * Gets array of all "primitiveList" elements
         */
        java.math.BigDecimal[] getPrimitiveListArray();
// </GET_ARRAY>
// <GET_IDX>

        /**
         * Gets ith "primitiveList" element
         */
        java.math.BigDecimal getPrimitiveListArray(int i);
// </GET_IDX>
// <XGET_LIST>

        /**
         * Gets (as xml) a List of "primitiveList" elements
         */
        java.util.List<org.apache.xmlbeans.XmlDecimal> xgetPrimitiveListList();
// </XGET_LIST>
// <XGET_ARRAY>

        /**
         * Gets (as xml) array of all "primitiveList" elements
         */
        org.apache.xmlbeans.XmlDecimal[] xgetPrimitiveListArray();
// </XGET_ARRAY>
// <XGET_IDX>

        /**
         * Gets (as xml) ith "primitiveList" element
         */
        org.apache.xmlbeans.XmlDecimal xgetPrimitiveListArray(int i);
// </XGET_IDX>
// <IS_NIL_IDX>

        /**
         * Tests for nil ith "primitiveList" element
         */
        boolean isNilPrimitiveListArray(int i);
// </IS_NIL_IDX>
// <SIZE_OF_ARRAY>

        /**
         * Returns number of "primitiveList" element
         */
        int sizeOfPrimitiveListArray();
// </SIZE_OF_ARRAY>
// <SET_ARRAY>

        /**
         * Sets array of all "primitiveList" element
         */
        void setPrimitiveListArray(java.math.BigDecimal[] primitiveListArray);
// </SET_ARRAY>
// <SET_IDX>

        /**
         * Sets ith "primitiveList" element
         */
        void setPrimitiveListArray(int i, java.math.BigDecimal primitiveList);
// </SET_IDX>
// <XSET_ARRAY>

        /**
         * Sets (as xml) array of all "primitiveList" element
         */
        void xsetPrimitiveListArray(org.apache.xmlbeans.XmlDecimal[] primitiveListArray);
// </XSET_ARRAY>
// <XSET_IDX>

        /**
         * Sets (as xml) ith "primitiveList" element
         */
        void xsetPrimitiveListArray(int i, org.apache.xmlbeans.XmlDecimal primitiveList);
// </XSET_IDX>
// <SET_NIL_IDX>

        /**
         * Nils the ith "primitiveList" element
         */
        void setNilPrimitiveListArray(int i);
// </SET_NIL_IDX>
// <INSERT_IDX>

        /**
         * Inserts the value as the ith "primitiveList" element
         */
        void insertPrimitiveList(int i, java.math.BigDecimal primitiveList);
// </INSERT_IDX>
// <ADD>

        /**
         * Appends the value as the last "primitiveList" element
         */
        void addPrimitiveList(java.math.BigDecimal primitiveList);
// </ADD>
// <INSERT_NEW_IDX>

        /**
         * Inserts and returns a new empty value (as xml) as the ith "primitiveList" element
         */
        org.apache.xmlbeans.XmlDecimal insertNewPrimitiveList(int i);
// </INSERT_NEW_IDX>
// <ADD_NEW>

        /**
         * Appends and returns a new empty value (as xml) as the last "primitiveList" element
         */
        org.apache.xmlbeans.XmlDecimal addNewPrimitiveList();
// </ADD_NEW>
// <REMOVE_IDX>

        /**
         * Removes the ith "primitiveList" element
         */
        void removePrimitiveList(int i);
// </REMOVE_IDX>
// <GET_LIST>

        /**
         * Gets a List of "complexList" elements
         */
        java.util.List<partials.XmlBeanchen> getComplexListList();
// </GET_LIST>
// <GET_ARRAY>

        /**
         * Gets array of all "complexList" elements
         */
        partials.XmlBeanchen[] getComplexListArray();
// </GET_ARRAY>
// <GET_IDX>

        /**
         * Gets ith "complexList" element
         */
        partials.XmlBeanchen getComplexListArray(int i);
// </GET_IDX>
// <SIZE_OF_ARRAY>

        /**
         * Returns number of "complexList" element
         */
        int sizeOfComplexListArray();
// </SIZE_OF_ARRAY>
// <SET_ARRAY>

        /**
         * Sets array of all "complexList" element
         */
        void setComplexListArray(partials.XmlBeanchen[] complexListArray);
// </SET_ARRAY>
// <SET_IDX>

        /**
         * Sets ith "complexList" element
         */
        void setComplexListArray(int i, partials.XmlBeanchen complexList);
// </SET_IDX>
// <INSERT_NEW_IDX>

        /**
         * Inserts and returns a new empty value (as xml) as the ith "complexList" element
         */
        partials.XmlBeanchen insertNewComplexList(int i);
// </INSERT_NEW_IDX>
// <ADD_NEW>

        /**
         * Appends and returns a new empty value (as xml) as the last "complexList" element
         */
        partials.XmlBeanchen addNewComplexList();
// </ADD_NEW>
// <REMOVE_IDX>

        /**
         * Removes the ith "complexList" element
         */
        void removeComplexList(int i);
// </REMOVE_IDX>
    }
}
